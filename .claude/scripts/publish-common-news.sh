#!/bin/zsh

# Configuration
readonly API_URL="http://localhost:8080/api/v1/contents/exists"

echo "[INFO] Checking for publishable content..."

# 1. Local API Call
response=$(curl -s -X GET "${API_URL}" -H "Accept: application/json")

if [[ $? -ne 0 ]]; then
    echo "[ERROR] Failed to connect to the local API." >&2
    exit 1
fi

# 2. JSON Parsing (jq 활용)
has_content=$(echo "${response}" | jq -r '.hasPublishableContent // false')

# 3. Flow Trigger Decision
if [[ "${has_content}" == "true" ]]; then
    echo "[SUCCESS] Publishable content exists. Starting the main workflow..."

    # ----------------------------------------------------
    # TODO: 실행할 다음 플로우 로직 작성 위치
    # ----------------------------------------------------

else
    echo "[INFO] No publishable content to publish. Exiting."
fi

/opt/homebrew/bin/claude -p "인스타그램에 신규 카드뉴스 컨텐츠 발행해줘.

다음 Skills를 적극 참고하세요: single-contents-publish skill

<Skills 필수 참고사항>
- 발행할 컨텐츠는 mysql MCP 사용해서 gen 테이블에서 조회할 것. 조회시 contents_type이 0인 것으로 조회해야 하며, 어제부터 생성된 데이터들 중 published_via_skills_yn != 'Y'인 것을 대상으로해야 함." \
  --dangerously-skip-permissions \
  >> ~/logs/single-contents-publish.log 2>&1
