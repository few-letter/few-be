#!/usr/bin/env bash
# PreToolUse hook (Bash matcher).
# Enforces: commit message must start with feat:/fix:/refactor:/config:/test:.
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

if [[ ! "$command_str" =~ git[[:space:]]+commit ]]; then
  exit 0
fi

message="$(printf '%s' "$command_str" | grep -oE -- '-m[[:space:]]*"[^"]*"|-m[[:space:]]*'"'"'[^'"'"']*'"'"'|--message[= ][^ ]*' | head -1 || true)"

if [[ -z "$message" ]]; then
  # No inline -m/--message value (e.g. editor-based commit) - cannot validate synchronously.
  exit 0
fi

message="${message#--message}"
message="${message#-m}"
message="${message#=}"
message="$(printf '%s' "$message" | sed -e 's/^[[:space:]]*//' -e 's/^["'"'"']//' -e 's/["'"'"']$//')"

if [[ ! "$message" =~ ^(feat|fix|refactor|config|test):[[:space:]] ]]; then
  deny "커밋 메시지는 'feat:', 'fix:', 'refactor:', 'config:', 'test:' 중 하나로 시작해야 합니다. (예: feat: 설명) 현재: '$message'"
fi

exit 0
