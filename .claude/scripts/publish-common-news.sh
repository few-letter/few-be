#!/bin/zsh

# Configuration
readonly API_URL="http://localhost:8080/api/v1/contents/exists"
readonly LOG_FILE="$HOME/logs/single-contents-publish.log"

echo "[INFO] Checking for publishable content..."

# 1. Local API Call
response=$(curl -s -X GET "${API_URL}" -H "Accept: application/json")

if [[ $? -ne 0 ]]; then
    echo "[ERROR] Failed to connect to the local API." >&2
    exit 1
fi

# 2. JSON Parsing
has_content=$(echo "${response}" | jq -r '.hasPublishableContent // false')

# 3. Guard Clause: false인 경우 바로 종료 (들여쓰기 방지)
if [[ "${has_content}" != "true" ]]; then
    echo "[INFO] No publishable content found. Exiting."
    exit 0
fi

/opt/homebrew/bin/claude -p "인스타그램에 신규 카드뉴스 컨텐츠 발행해줘.

다음 Skills를 적극 참고하세요: single-contents-publish skill

<Skills 필수 참고사항>
- 발행할 컨텐츠는 mysql MCP 사용해서 gen 테이블에서 조회할 것. 조회시 contents_type이 0인 것으로 조회해야 하며, 어제부터 생성된 데이터들 중 published_via_skills_yn != 'Y'인 것을 대상으로해야 함." \
  --dangerously-skip-permissions \
  >> "${LOG_FILE}" 2>&1
