#!/bin/zsh

# 머니스토리 신규 글이 있을 때만 claude -p 로 instagram-reels-uploader 스킬 수행 (cron: 06:00, 16:00)
readonly PROJECT_DIR="${0:A:h:h:h}"
readonly LOG_FILE="${PROJECT_DIR}/logs/instagram-reels-uploader.log"
readonly LAST_FILE="${PROJECT_DIR}/.claude/skills/instagram-reels-uploader/references/last-crawled.txt"

mkdir -p "${LOG_FILE:h}"
log() { echo "[$(date '+%Y-%m-%d %H:%M:%S')] $*" | tee -a "${LOG_FILE}" >&2; }

# cron 은 PATH 가 최소이므로 claude/node 경로 보강 (publish-contents-common.sh 와 동일)
if [ -d "$HOME/.nvm/versions/node" ]; then
  node_bin=$(/bin/ls -d "$HOME"/.nvm/versions/node/*/bin 2>/dev/null | sort -V | tail -1)
  [ -n "$node_bin" ] && export PATH="$node_bin:$PATH"
fi
[ -d /opt/homebrew/bin ] && export PATH="/opt/homebrew/bin:$PATH"
[ -d "$HOME/.local/bin" ] && export PATH="$HOME/.local/bin:$PATH"

readonly CLAUDE_CONFIG_FILE="${PROJECT_DIR}/.claude/non-interactive-claude-config.env"
[ -f "${CLAUDE_CONFIG_FILE}" ] && source "${CLAUDE_CONFIG_FILE}"
[[ -z "${CLAUDE_CODE_OAUTH_TOKEN}" ]] && { log "[ERROR] CLAUDE_CODE_OAUTH_TOKEN 미설정: ${CLAUDE_CONFIG_FILE}"; exit 1; }
export CLAUDE_CODE_OAUTH_TOKEN
CLAUDE_MODEL_OPTS=()
if [[ -n "${ANTHROPIC_BASE_URL}" ]]; then export ANTHROPIC_BASE_URL; CLAUDE_MODEL_OPTS=(--model auto); fi
command -v claude >/dev/null || { log "[ERROR] claude 를 찾을 수 없음. PATH=$PATH"; exit 1; }

# 1. 신규 글 확인: 인기글 목록 중 마지막 크롤링 ID 의 앞 숫자보다 큰 것 중 최대값
last_id=$(tail -n 1 "${LAST_FILE}" 2>/dev/null)
last_num=${last_id%%_*}
html=$(curl -fsSL --max-time 30 -A "Mozilla/5.0" https://story.pay.naver.com/popular) \
  || { log "[ERROR] 인기글 페이지 조회 실패"; exit 1; }
target=$(print -r -- "$html" | grep -o 'href="/content/[0-9A-Za-z_]*"' | sed 's/.*content\/\(.*\)"/\1/' | sort -u \
  | awk -F_ -v last="${last_num:-0}" '$1+0 > last+0 {print $1, $0}' | sort -n | tail -n 1 | cut -d' ' -f2)

if [[ -z "${target}" ]]; then
  log "[INFO] 신규 글 없음 (last=${last_id:-none})"
  exit 0
fi
log "[INFO] 신규 글 발견: ${target}"

# 2. 스킬 수행 (스킬이 프로젝트 .claude/skills 에 있으므로 프로젝트 루트에서 실행)
cd "${PROJECT_DIR}" || { log "[ERROR] cd 실패: ${PROJECT_DIR}"; exit 1; }
claude "${CLAUDE_MODEL_OPTS[@]}" -p "instagram-reels-uploader skill로 머니스토리 신규 글을 인스타그램 릴스로 업로드해줘.

<필수 참고사항>
- 대상 글 ID = ${target} (https://story.pay.naver.com/content/${target})
- 인스타그램 운영용 ig-* 스킬을 함께 활용할 것 (스킬 내 절차·출력 형식은 instagram-reels-uploader 가 우선):
  - ig-reel: 영상 제작 프롬프트(video_create_draft 의 prompt/additionalInstructions)를 짤 때 첫 3초 훅 후보를 만들고 점수화해 가장 강한 훅을 반영
  - ig-caption: 4단계 캡션의 첫 줄('... more' 전 125자 이내에 핵심)과 본문을 린트. 단, 캡션 형식(요약 → @few.ai.letter 안내 문구 → 해시태그 정확히 5개)은 instagram-reels-uploader 4단계를 그대로 유지
  - ig-human: 캡션을 업로드하기 전에 em dash·AI 투 단어를 제거하고 탐지 점수를 확인" \
  --dangerously-skip-permissions < /dev/null 2>&1 \
  | while IFS= read -r line; do echo "[$(date '+%Y-%m-%d %H:%M:%S')] ${line}"; done | tee -a "${LOG_FILE}"

rc=${pipestatus[1]}
[[ ${rc} -ne 0 ]] && { log "[ERROR] claude 실패 (exit=${rc})"; exit ${rc}; }
log "[INFO] 완료"
