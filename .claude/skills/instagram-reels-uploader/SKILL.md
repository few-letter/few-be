---
name: instagram-reels-uploader
description: 네이버페이 머니스토리(story.pay.naver.com/popular)의 신규 글을 크롤링·요약하고, mirra MCP로 reels 영상을 만들어 Aside MCP로 인스타그램 릴스로 업로드. "릴스 업로드", "instagram reels uploader" 요청 또는 run-instagram-reels-uploader.sh 에서 사용.
---

# instagram-reels-uploader

신규 머니스토리 글 1건 → 요약 + 해시태그 → mirra 릴스 영상 → 로컬 다운로드 → 인스타그램 릴스 업로드.

## 필요한 MCP
- **mirra MCP** (`mcp__mirra__*`) — 링크로 콘텐츠 만들기(영상 제작)
- **Aside MCP** (`mcp__aside__repl`) — 웹 서치 / instagram.com 자동화 (이미 로그인되어 있다고 가정, 아니면 중단·보고)
- 추가 디렉토리 접근은 허용됨.
- ⚠️ 도구가 deferred 상태일 수 있다. 키워드 검색(`mirra` 등)으로 찾지 말고 **ToolSearch 에 `select:` 로 이름을 직접 지정**해 한 번에 로드할 것:
  `select:mcp__mirra__url_to_content,mcp__mirra__media_upload,mcp__mirra__video_create_draft,mcp__mirra__video_get_job,mcp__mirra__video_get,mcp__mirra__video_finalize,mcp__mirra__video_render,mcp__aside__repl`
  그래도 스키마가 안 나오면 그때 "MCP 미연결"로 보고한다.

## 1. 대상 글 결정
- 크롤링 기록: `references/last-crawled.txt` (한 줄 = 한 글, `content ID`; 가장 최근 크롤링 글 ID는 파일 마지막 줄).
- 목록: `https://story.pay.naver.com/popular` 의 `a[href^="/content/"]` 링크. ID 형식 `3835_3_C1`, 앞 숫자가 클수록 최신.
- 호출 측 프롬프트가 대상 ID 를 지정하면 그 글을 사용. 아니면 기록의 마지막 ID 보다 앞 숫자가 **큰** 글 중 가장 큰 1건을 선택. 없으면 아무것도 하지 않고 종료.
- 글 URL: `https://story.pay.naver.com/content/<ID>`. 제목·본문은 해당 페이지에서 읽는다(필요 시 Aside로 접속).

## 2. 요약 / 해시태그
- 본문을 한국어로 요약(캡션용, 4~6문장). 주요 키워드 해시태그 **정확히 5개**.

## 3. 영상 제작 (mirra MCP)
영상에는 **서로 다른 이미지 5~8장**이 들어가 보는 재미가 있어야 한다.

### 3-1. 이미지 수집 (최대 8장, 최소 5장 목표)
작업 디렉토리 `{PROJECT_ROOT}/tmp/instagram-reels-uploader/<ID>-<YYYYMMDD-HHMMSS>/images/` 에 저장한다.
1. **글 본문 이미지 우선**: 글 페이지 HTML의 `<img src>` 중 `moneystory-phinf.pstatic.net`(썸네일·본문 삽입 이미지)과 차트 이미지(`financial-vn.pstatic.net/chart/...`)만 후보로 삼는다. 로고·아이콘·캐릭터 등 UI 이미지(`finance-fe-static.pstatic.net/...`, `?type=f44_44` 등 작은 썸네일)는 제외.
   - 다운로드: `curl -sL -e https://story.pay.naver.com/ -o <파일> <url>` (pstatic 핫링크 방지 우회). 확장자는 실제 포맷에 맞게(`file` 로 확인).
2. **부족하면 웹 검색으로 보충**: 5장 미만이면 Aside MCP로 이미지 검색(예: Google 이미지)해 글의 핵심 키워드(지역·기업·지표 등)별로 고해상도·워터마크/자막/UI 없는 이미지를 받아 채운다. 장당 다른 키워드로 검색해 서로 겹치지 않게 한다.
3. 받은 이미지를 실제로 열어 확인해, 글 내용과 무관하거나 깨졌거나 저해상도(짧은 변 < 600px)이거나 중복인 것은 버린다. 최종 5장 미만이면 있는 만큼만 진행(중단하지 않음).
4. 각 이미지를 `mcp__mirra__media_upload`(`filePath` = 절대경로) 로 올려 공개 URL을 얻는다.

### 3-2. 영상 생성
1. `mcp__mirra__url_to_content` 에 글 URL(`language: "Korean"`)을 넣어 "링크로 콘텐츠 만들기"를 실행한다.
2. `mcp__mirra__video_create_draft` 호출: `aspectRatio: "9:16"`, `language: "ko"`, `locale: "ko"`, `prompt`: 글 요약+URL(+"9:16 vertical reels"), `userMedia`: 3-1의 업로드 URL들(`[{url, kind: "image"}]`), `imageSearchSource: "user-media"`, `additionalInstructions`: "Use each uploaded image in a different scene, 5-8 short scenes, fast cuts, no repeated image." → `video_get_job` 폴링 → `video_get` 로 확인. 업로드 이미지가 0장이면 `userMedia`/`user-media` 를 빼고 `imageSearchSource: "internet"` 으로 진행.
3. 이 호출이 크레딧을 쓰는 것은 사전 허용된 사항으로 간주해 `video_finalize`(`expectedUpdatedAt`=video_get 값) → `video_get_job` 완료 대기 → `video_render` → `video_get` 에서 `renderStatus: completed` 까지 대기. 단 advanced 모드는 사용하지 않는다(확인 필요 시 중단·보고).
   - **비율은 반드시 9:16(세로 1080x1920)**: `aspectRatio: "9:16"` 을 명시하고 `video_get` 결과의 비율이 9:16 이 아니면 진행하지 말고 중단·보고.
4. 완성된 영상 URL을 `<작업 디렉토리>/reel.mp4` 로 `curl -L -o` 다운로드. 파일이 비어 있으면 중단·보고(기록 갱신 금지).
5. 다운로드 후 `mdls -name kMDItemPixelWidth -name kMDItemPixelHeight reel.mp4` 로 **1080x1920(9:16)** 인지 확인. 아니면 업로드하지 말고 중단·보고.

## 4. 캡션
```
{크롤링 게시글 요약}

더 많은 글로벌 경제 및 증시 소식과 속보는? @few.ai.letter

#{키워드1} #{키워드2} #{키워드3} #{키워드4} #{키워드5}
```

## 5. 인스타그램 릴스 업로드 (Aside MCP)
`instagram-contents-publish` 스킬 4단계의 Aside 기법을 따른다: 좌표/셀렉터를 새로 찾기 전에 그 문서와 메모리를 먼저 읽을 것.
- `svg[aria-label="새로운 게시물"]` → "게시물" 메뉴 → 다이얼로그의 `input[type=file]` 에 `setInputFiles`. "Path escapes session directory" 면 세션 디렉토리(`pwd`) 하위로 복사 후 사용(완료 후 삭제).
- 영상은 릴스로 인식되며 "다음" 클릭(`clickByText`, `div[role=button]` 텍스트 매칭)으로 진행.
- ⚠️ **크롭 화면에서 비율을 반드시 9:16으로 고정**: 크롭 아이콘(`svg[aria-label="자르기 선택"]`)을 좌표로 클릭해 메뉴를 열고 "9:16"(또는 "원본", 원본이 9:16일 때) 항목을 선택한다(`instagram-contents-publish` 5단계와 같이 아이콘 클릭 + 좌표 클릭을 한 번의 실행 안에서). 1:1 이 선택돼 있으면 영상이 잘리므로 업로드 금지.
- 발행 후 릴스 화면을 스크린샷으로 확인해 세로(9:16) 전체 화면으로 보이는지, 1:1 로 잘리지 않았는지 검증한다.
- 캡션 입력 후 값 검증(첫 줄·해시태그 5개), `Escape` 금지(자동완성은 `Tab`), 공유하기 클릭 후 다이얼로그가 닫힐 때까지 대기.
- 발행 후 프로필에서 릴스가 올라갔고 캡션이 노출되는지 확인.

## 6. 기록 / 정리
- 업로드 검증 통과 시에만 `references/last-crawled.txt` 에 `<ID>` 한 줄 추가. 실패 시 추가하지 말고 보고(다음 실행에서 재시도됨).
- 성공 후 임시 디렉토리 삭제.
