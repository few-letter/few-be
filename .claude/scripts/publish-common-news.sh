#!/bin/zsh

# Configuration
readonly API_URL="http://localhost:8080/api/v1/contents/exists/publishable"
readonly LOG_FILE="$HOME/logs/single-contents-publish.log"

# 발행 대상 contents_type. 호출 측(TriggerContentsPublishSkillsUseCase)에서 첫 번째 인자로 전달한다.
# 미전달 시 기본값 0(local-news).
readonly CONTENTS_TYPE="${1:-0}"

# 로그 디렉토리 보장 + 모든 로그는 LOG_FILE 로만, 항상 현재 시간 prefix
mkdir -p "$(dirname "${LOG_FILE}")"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" >> "${LOG_FILE}"
}

# cron은 macOS 로그인 키체인에 접근할 수 없어 claude의 OAuth/키체인 인증이 실패한다.
# `claude setup-token`으로 발급한 장기 토큰을 이 파일에서 읽어와 대신 사용한다.
if [ -f ~/.claude/cron-token.env ]; then
  source ~/.claude/cron-token.env
fi

if [ -z "$CLAUDE_CODE_OAUTH_TOKEN" ]; then
  log "CLAUDE_CODE_OAUTH_TOKEN이 설정되어 있지 않습니다. ~/.claude/cron-token.env 파일을 확인하세요."
  log "실행 완료 (인증 토큰 없음)\n"
  exit 1
fi

# env 파일에 export 가 없어도 자식 프로세스(claude)가 상속받도록 명시적으로 export
export CLAUDE_CODE_OAUTH_TOKEN

# 이 스크립트는 JVM(ProcessBuilder)/cron 등 비대화형 셸에서 실행되므로 ~/.zshrc 가 로드되지 않는다.
# 그 결과 nvm 이 설정하는 node/npx PATH 가 없어 npx 기반 stdio MCP 서버
# (mysql, gemini-image)가 spawn 되지 못하고 "not connected" 에러가 발생한다.
# 여기서 node bin 디렉토리와 homebrew bin 을 PATH 앞에 명시적으로 추가한다.
if [ -d "$HOME/.nvm/versions/node" ]; then
  node_bin=$(/bin/ls -d "$HOME"/.nvm/versions/node/*/bin 2>/dev/null | sort -V | tail -1)
  [ -n "$node_bin" ] && export PATH="$node_bin:$PATH"
fi
[ -d /opt/homebrew/bin ] && export PATH="/opt/homebrew/bin:$PATH"

if ! command -v npx >/dev/null 2>&1; then
  log "[ERROR] npx 를 PATH 에서 찾을 수 없습니다. npx 기반 MCP 서버(mysql)가 연결되지 않습니다. PATH=$PATH"
  exit 1
fi

log "[INFO] Checking for publishable content... (contents_type=${CONTENTS_TYPE})"

# 1. Local API Call
response=$(curl -s -X GET "${API_URL}" -H "Accept: application/json")

if [[ $? -ne 0 ]]; then
    log "[ERROR] Failed to connect to the local API."
    exit 1
fi

# 2. JSON Parsing (응답은 { "data": { ... }, "message": ... } 형태로 감싸져 있음)
has_content=$(echo "${response}" | jq -r '.data.hasPublishableContent // false')
log "[INFO] hasPublishableContent=${has_content}"

# 3. Guard Clause: false인 경우 바로 종료 (들여쓰기 방지)
if [[ "${has_content}" != "true" ]]; then
    log "[INFO] No publishable content found. Exiting."
    exit 0
fi

# 4. Execute the publish command to Claude Code
log "[INFO] Publishable content found. Executing publish command..."

# MCP 설정이 홈 디렉토리 기준이므로 claude 실행 전 홈 디렉토리로 이동
cd "$HOME" || {
    log "[ERROR] cd \$HOME 실패: $HOME"
    exit 1
}

/opt/homebrew/bin/claude -p "인스타그램에 신규 카드뉴스 컨텐츠 발행해줘.

다음 Skills를 적극 참고하세요: single-contents-publish skill

<Skills 필수 참고사항>
- 발행할 컨텐츠를 mysql에서 조회시 추가 조건: contents_type이 ${CONTENTS_TYPE}인 것으로 조회해야 함
- 해당 contents_type은 skill 명세 파일의 '{프롬프트에서 제안한 값}' 부분에 추가 쿼리 조건으로 들어가야 함" \
  --dangerously-skip-permissions < /dev/null 2>&1 \
  | while IFS= read -r line; do
      echo "[$(date '+%Y-%m-%d %H:%M:%S')] ${line}"
    done >> "${LOG_FILE}"

# 파이프라인 첫 번째 명령(claude)의 종료 코드 확인 (zsh: 1-indexed)
claude_exit=${pipestatus[1]}
if [[ ${claude_exit} -ne 0 ]]; then
    log "[ERROR] claude 명령 실패 (exit=${claude_exit}). 'Not logged in' 이면 이 스크립트를 실행하는 사용자/환경에서 'claude' 로그인(/login) 또는 ANTHROPIC_API_KEY 설정이 필요합니다."
    exit "${claude_exit}"
fi

log "[INFO] Publish command finished."
