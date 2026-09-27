#!/usr/bin/env bash
# PreToolUse hook (Bash matcher).
# Enforces: branch naming format, branching from dev, and issue-must-exist-first.
# See .claude/hooks/README.md for details and known limitations.
set -euo pipefail

input="$(cat)"
command_str="$(printf '%s' "$input" | jq -r '.tool_input.command // empty')"

deny() {
  local reason="$1"
  jq -n --arg reason "$reason" '{
    hookSpecificOutput: {
      hookEventName: "PreToolUse",
      permissionDecision: "deny",
      permissionDecisionReason: $reason
    }
  }'
  exit 0
}

# Only inspect commands that create a new branch; anything else passes through untouched.
branch_name=""
if [[ "$command_str" =~ git[[:space:]]+checkout[[:space:]]+-b[[:space:]]+\"?([^[:space:]\"]+) ]]; then
  branch_name="${BASH_REMATCH[1]}"
elif [[ "$command_str" =~ git[[:space:]]+switch[[:space:]]+-c[[:space:]]+\"?([^[:space:]\"]+) ]]; then
  branch_name="${BASH_REMATCH[1]}"
elif [[ "$command_str" =~ git[[:space:]]+branch[[:space:]]+\"?([^[:space:]\"-][^[:space:]\"]*) ]]; then
  branch_name="${BASH_REMATCH[1]}"
fi

if [[ -z "$branch_name" ]]; then
  exit 0
fi

# 1) Naming format: {fix|feat|refactor|config}/#{issue_number}
if [[ ! "$branch_name" =~ ^(fix|feat|refactor|config)/\#[0-9]+$ ]]; then
  deny "브랜치명 '$branch_name'이(가) 규칙을 위반했습니다. '{fix|feat|refactor|config}/#이슈번호' 형태여야 합니다. (예: fix/#1234)"
fi

issue_number="${branch_name##*#}"

# 2) Must branch off dev
current_branch="$(git rev-parse --abbrev-ref HEAD 2>/dev/null || echo "")"
if [[ -n "$current_branch" && "$current_branch" != "dev" ]]; then
  deny "브랜치는 반드시 dev 브랜치에서 생성해야 합니다. 현재 브랜치: '$current_branch'"
fi

# 3) GitHub issue must already exist (best-effort; fail-open if gh is unavailable/unauthenticated)
if command -v gh >/dev/null 2>&1 && gh auth status >/dev/null 2>&1; then
  if ! gh issue view "$issue_number" >/dev/null 2>&1; then
    deny "GitHub 이슈 #$issue_number 를 찾을 수 없습니다. 개발 전 이슈를 먼저 생성하세요."
  fi
fi

exit 0
