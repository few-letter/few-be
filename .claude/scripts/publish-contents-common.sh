#!/bin/zsh

# Configuration
# 스크립트 위치(.claude/scripts) 기준 프로젝트 루트의 logs/ 에 기록. 스크립트 내에서 cd $HOME 하므로 절대경로로 고정
readonly LOG_FILE="${0:A:h:h:h}/logs/single-contents-publish.log"

# 발행 대상 contents_type. 호출 측(TriggerContentsPublishSkillsUseCase)에서 첫 번째 인자로 전달한다.
# 미전달 시 기본값 0(local-news).
readonly CONTENTS_TYPE="${1:-0}"

# 로그 디렉토리 보장 + 모든 로그는 LOG_FILE과 stderr에 함께, 항상 현재 시간 prefix
mkdir -p "$(dirname "${LOG_FILE}")"

# 이번 실행 시작 시점의 로그 파일 크기(byte). 실패 시 이 지점 이후(=이번 실행분) 로그만 사유로 첨부한다.
readonly LOG_START_OFFSET=$(stat -f %z "${LOG_FILE}" 2>/dev/null || echo 0)

log() {
    # 로그 파일뿐 아니라 stderr에도 출력하여, 이 스크립트를 호출하는 쪽(JVM ProcessBuilder 등)이
    # 캡처하는 stdout/stderr 만으로도 실패 원인을 파악할 수 있도록 한다.
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "${LOG_FILE}" >&2
}

# 실패 시에만 Discord 웹훅으로 실패 사유를 알린다.
# 웹훅 URL 은 커밋되면 안 되므로 호출 측(TriggerContentsPublishSkillsUseCase)이 urls.webhook.discord 값을
# DISCORD_WEBHOOK_URL 환경변수로 주입한다. 미설정(cron 직접 실행 등)이면 알림 없이 로그만 남긴다.
notify_discord() {
    [[ -z "${DISCORD_WEBHOOK_URL}" ]] && return 0

    # Discord content 최대 2000자 제한. 실제 에러는 로그 끝에 있으므로 사유는 뒤쪽 1800자를 남긴다.
    local reason="$1"
    (( ${#reason} > 1800 )) && reason="…${reason[-1800,-1]}"
    local msg="🚨 콘텐츠 발행 스크립트 실패 (contentsType=${CONTENTS_TYPE}, host=$(hostname -s))
${reason}"
    # jq 의존 없이 JSON 문자열 이스케이프 (\ " 개행 탭, 그 외 제어문자 제거)
    msg="${msg//\\/\\\\}"
    msg="${msg//\"/\\\"}"
    msg="${msg//$'\t'/\\t}"
    msg="${msg//$'\n'/\\n}"
    msg=$(print -rn -- "${msg}" | LC_ALL=C tr -d '\000-\010\013-\037')

    curl -sS --max-time 10 -o /dev/null -H "Content-Type: application/json" \
        -d "{\"content\":\"${msg}\"}" "${DISCORD_WEBHOOK_URL}" \
        || log "[WARN] Discord 웹훅 호출 실패"
}

# 실패 로그 기록 + Discord 알림 + 종료
fail() {
    local code="$1" reason="$2"
    log "[ERROR] ${reason}"
    notify_discord "${reason}"
    exit "${code}"
}

# Omniroute 및 비대화형 쉘 설정
# Claude 로그인에 실패한 경우 `claude setup-token`으로 토큰 발급 후 설정 파일에 저징 필요
if [ -f ~/.claude/non-interactive-claude-config.env ]; then
  source ~/.claude/non-interactive-claude-config.env
fi

if [ -z "$CLAUDE_CODE_OAUTH_TOKEN" ]; then
  fail 1 "CLAUDE_CODE_OAUTH_TOKEN이 설정되어 있지 않습니다. ~/.claude/non-interactive-claude-config.env 파일을 확인하세요."
fi

# env 파일에 export 가 없어도 자식 프로세스(claude)가 상속받도록 명시적으로 export
export CLAUDE_CODE_OAUTH_TOKEN

# Omniroute 게이트웨이(ANTHROPIC_BASE_URL) 사용 시에만 --model auto 지정.
# grep 이 아니라 source 된 실제 값으로 판단하므로 주석 처리된 줄은 자연히 미설정으로 간주된다.
CLAUDE_MODEL_OPTS=()
if [[ -n "${ANTHROPIC_BASE_URL}" ]]; then
  export ANTHROPIC_BASE_URL
  CLAUDE_MODEL_OPTS=(--model auto)
fi

# .zshrc 가 없거나 nvm 초기화를 하지 않는 환경을 대비한 fallback.
# node/npx PATH 가 없으면 npx 기반 stdio MCP 서버(mysql, gemini-image)가 spawn 되지 못하고
# "not connected" 에러가 발생하므로, node bin 디렉토리와 homebrew bin 을 PATH 앞에 추가한다.
if [ -d "$HOME/.nvm/versions/node" ]; then
  node_bin=$(/bin/ls -d "$HOME"/.nvm/versions/node/*/bin 2>/dev/null | sort -V | tail -1)
  [ -n "$node_bin" ] && export PATH="$node_bin:$PATH"
fi
[ -d /opt/homebrew/bin ] && export PATH="/opt/homebrew/bin:$PATH"
# claude 공식 설치 스크립트(native installer) 기본 경로. npm 전역 설치는 위 nvm node bin 으로 커버된다.
[ -d "$HOME/.local/bin" ] && export PATH="$HOME/.local/bin:$PATH"

if ! command -v npx >/dev/null 2>&1; then
  fail 1 "npx 를 PATH 에서 찾을 수 없습니다. npx 기반 MCP 서버(mysql)가 연결되지 않습니다. PATH=$PATH"
fi

if ! command -v claude >/dev/null 2>&1; then
  fail 1 "claude 를 PATH 에서 찾을 수 없습니다. Claude Code 설치 여부를 확인하세요. PATH=$PATH"
fi

# 발행 skill이 프로젝트 .claude/skills 에 있으므로 claude 실행 전 프로젝트 루트로 이동
# (사용하는 MCP 서버는 user scope 라 디렉토리와 무관하게 연결됨)
readonly PROJECT_DIR="${0:A:h:h:h}"
cd "${PROJECT_DIR}" || {
    fail 1 "cd 프로젝트 디렉토리 실패: ${PROJECT_DIR}"
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
    fail 1 "알 수 없는 CONTENTS_TYPE=${CONTENTS_TYPE}"
    ;;
esac

claude "${CLAUDE_MODEL_OPTS[@]}" -p "${PROMPT}" \
  --dangerously-skip-permissions < /dev/null 2>&1 \
  | while IFS= read -r line; do
      echo "[$(date '+%Y-%m-%d %H:%M:%S')] ${line}"
    done | tee -a "${LOG_FILE}"

# 파이프라인 첫 번째 명령(claude)의 종료 코드 확인 (zsh: 1-indexed)
claude_exit=${pipestatus[1]}
if [[ ${claude_exit} -ne 0 ]]; then
    # 이번 실행 시작 이후 기록된 로그(claude 출력 포함)를 실패 사유로 첨부
    fail "${claude_exit}" "claude 명령 실패 (exit=${claude_exit}). 이번 실행 로그:
$(tail -c +$((LOG_START_OFFSET + 1)) "${LOG_FILE}")"
fi

log "[INFO] Publish command finished."
