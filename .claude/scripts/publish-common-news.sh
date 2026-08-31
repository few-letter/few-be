#!/bin/zsh

# Configuration
readonly API_URL="http://localhost:8080/api/v1/contents/exists/publishable"
readonly LOG_FILE="$HOME/logs/single-contents-publish.log"

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

log "[INFO] Checking for publishable content..."

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
- 발행할 컨텐츠는 mysql MCP 사용해서 gen 테이블에서 조회할 것. 조회시 contents_type이 0인 것으로 조회해야 하며, 어제부터 생성된 데이터들 중 published_via_skills_yn != 'Y'인 것을 대상으로해야 함." \
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
