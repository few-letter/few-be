#!/bin/zsh

# Configuration
readonly LOG_FILE="$HOME/logs/single-contents-publish.log"

# 발행 대상 contents_type. 호출 측(TriggerContentsPublishSkillsUseCase)에서 첫 번째 인자로 전달한다.
# 미전달 시 기본값 0(local-news).
readonly CONTENTS_TYPE="${1:-0}"

# 로그 디렉토리 보장 + 모든 로그는 LOG_FILE과 stderr에 함께, 항상 현재 시간 prefix
mkdir -p "$(dirname "${LOG_FILE}")"

log() {
    # 로그 파일뿐 아니라 stderr에도 출력하여, 이 스크립트를 호출하는 쪽(JVM ProcessBuilder 등)이
    # 캡처하는 stdout/stderr 만으로도 실패 원인을 파악할 수 있도록 한다.
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "${LOG_FILE}" >&2
}

# Omniroute 및 비대화형 쉘 설정
# Claude 로그인에 실패한 경우 `claude setup-token`으로 토큰 발급 후 설정 파일에 저징 필요
if [ -f ~/.claude/non-interactive-claude-config.env ]; then
  source ~/.claude/non-interactive-claude-config.env
fi

if [ -z "$CLAUDE_CODE_OAUTH_TOKEN" ]; then
  log "CLAUDE_CODE_OAUTH_TOKEN이 설정되어 있지 않습니다. ~/.claude/non-interactive-claude-config.env 파일을 확인하세요."
  log "실행 완료 (인증 토큰 없음)\n"
  exit 1
fi

# env 파일에 export 가 없어도 자식 프로세스(claude)가 상속받도록 명시적으로 export
export CLAUDE_CODE_OAUTH_TOKEN

# .zshrc 가 없거나 nvm 초기화를 하지 않는 환경을 대비한 fallback.
# node/npx PATH 가 없으면 npx 기반 stdio MCP 서버(mysql, gemini-image)가 spawn 되지 못하고
# "not connected" 에러가 발생하므로, node bin 디렉토리와 homebrew bin 을 PATH 앞에 추가한다.
if [ -d "$HOME/.nvm/versions/node" ]; then
  node_bin=$(/bin/ls -d "$HOME"/.nvm/versions/node/*/bin 2>/dev/null | sort -V | tail -1)
  [ -n "$node_bin" ] && export PATH="$node_bin:$PATH"
fi
[ -d /opt/homebrew/bin ] && export PATH="/opt/homebrew/bin:$PATH"

if ! command -v npx >/dev/null 2>&1; then
  log "[ERROR] npx 를 PATH 에서 찾을 수 없습니다. npx 기반 MCP 서버(mysql)가 연결되지 않습니다. PATH=$PATH"
  exit 1
fi

# 발행 skill이 프로젝트 .claude/skills 에 있으므로 claude 실행 전 프로젝트 루트로 이동
# (사용하는 MCP 서버는 user scope 라 디렉토리와 무관하게 연결됨)
readonly PROJECT_DIR="${0:A:h:h:h}"
cd "${PROJECT_DIR}" || {
    log "[ERROR] cd 프로젝트 디렉토리 실패: ${PROJECT_DIR}"
    exit 1
}

# contents_type별로 사용할 skill이 다르므로 bash에서 분기해 프롬프트를 타입별로 짧고 명확하게 구성
case "${CONTENTS_TYPE}" in
  1|2)
    PROMPT="instagram-contents-publish skill로 인스타그램에 신규 카드뉴스 컨텐츠 발행해줘.

<필수 참고사항>
- mysql에서 조회 시 추가 조건: contents_type = ${CONTENTS_TYPE}
- 해당 contents_type은 skill 명세 파일의 '{프롬프트에서 제안한 값}' 부분에 추가 쿼리 조건으로 들어가야 함"
    ;;
  4|5)
    PROMPT="investing-dot-com-crawling skill로 investing.com을 크롤링하고, 이어서 instagram-contents-publish skill로 인스타그램에 신규 카드뉴스 컨텐츠 발행해줘.

<필수 참고사항>
- contentsType = ${CONTENTS_TYPE}
- instagram-contents-publish skill에서 mysql 조회 시 추가 조건: contents_type = ${CONTENTS_TYPE}
- 해당 contents_type은 skill 명세 파일의 '{프롬프트에서 제안한 값}' 부분에 추가 쿼리 조건으로 들어가야 함"
    ;;
  *)
    log "[ERROR] 알 수 없는 CONTENTS_TYPE=${CONTENTS_TYPE}"
    exit 1
    ;;
esac

/opt/homebrew/bin/claude --model auto -p "${PROMPT}" \
  --dangerously-skip-permissions < /dev/null 2>&1 \
  | while IFS= read -r line; do
      echo "[$(date '+%Y-%m-%d %H:%M:%S')] ${line}"
    done | tee -a "${LOG_FILE}"

# 파이프라인 첫 번째 명령(claude)의 종료 코드 확인 (zsh: 1-indexed)
claude_exit=${pipestatus[1]}
if [[ ${claude_exit} -ne 0 ]]; then
    log "[ERROR] claude 명령 실패 (exit=${claude_exit}). 원인은 위 claude 출력 로그를 확인하세요."
    exit "${claude_exit}"
fi

log "[INFO] Publish command finished."
