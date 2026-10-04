---
name: investing-dot-com-crawling
description: Investing.com RSS(경제 분석 / 인기 뉴스)를 크롤링해 headline·summary·썸네일을 생성하고 gen 테이블에 저장한 뒤, instagram-contents-publish 스킬로 인스타그램 발행까지 이어서 수행. "investing 크롤링", "economic-analysis 크롤링", "hot-news 크롤링", contentsType 4 또는 5 요청 시 사용.
---

# Investing.com Crawling → 카드뉴스 발행

Investing.com RSS 피드를 크롤링해 `gen` 테이블에 신규 콘텐츠를 적재하고, 곧바로 `instagram-contents-publish` 스킬을 호출해 인스타그램 카드뉴스로 발행까지 마무리한다.

## 필요한 MCP / 스킬
- **MySQL MCP** (`mcp__mcp_server_mysql__mysql_query`) — 중복 확인 조회 / `gen` INSERT
- **Firecrawl** (`mcp__firecrawl__firecrawl_scrape`) — **본문 크롤링 전용.** investing.com 기사 페이지는 Cloudflare 봇 차단이 있어 일반 curl/WebFetch는 403. Firecrawl과 Aside 둘 다로 같은 기사를 읽어본 결과, Firecrawl(markdown)은 광고·내비게이션이 걷힌 본문 위주 텍스트로 나오는 반면 Aside(`snapshot` 접근성 트리)는 헤더/내비/광고 iframe/시세 위젯까지 뒤섞인 매우 장황한 트리라 본문만 골라내기 번거로움 → **본문은 항상 Firecrawl만 사용**하고 Aside로 다시 읽지 않는다.
- **Aside MCP** (`mcp__aside__repl`) — **썸네일 이미지 다운로드 전용.** content-media.investing.com 이미지도 Cloudflare에 막혀 직접 curl로 못 받으므로, 실제 브라우저 세션으로 접속해 로컬에 저장할 때만 사용한다 (Firecrawl은 임의 CDN 이미지의 원본 바이트를 그대로 받는 기능이 없음).
- **instagram-contents-publish 스킬** — 4단계에서 그대로 호출 (내부 로직은 건드리지 않되, 로컬 경로 썸네일 지원을 위한 소폭 수정이 함께 적용되어 있음)

## 게시 완료 로그
`references/published-log.md` — 이 스킬로 발행까지 성공한 기사 URL을 기록해두는 로컬 파일. **크롤링 시작 전 이 로그에 있는 URL은 다시 크롤링 대상으로 삼지 않는다** (2단계 참고). `gen` 테이블 조회(DB 기준)와 별개로 두는 2차 안전장치이며, 5단계에서 발행이 실제로 성공한 건만 이 로그에 추가한다.

## 대상 선정 (contentsType)
프롬프트에서 지정한 `contentsType` 값에 따라 크롤링 대상 RSS가 결정된다. 값이 없으면 사용자에게 4/5 중 무엇인지 확인한다.

| contentsType | 이름 | RSS URL | 비고 |
|---|---|---|---|
| 4 | economic-analysis | https://www.investing.com/rss/286.rss | RSS 항목에 이미지 없음 → 기사 본문에서 썸네일 후보를 찾는다 |
| 5 | hot-news | https://www.investing.com/rss/news_285.rss | RSS `<enclosure url="...">` 에 썸네일 URL이 바로 제공됨 |

(참고: `instagram-contents-publish/Contents_Type Code Mapping.md` 에도 동일하게 정의되어 있음)

## 전체 절차

### 1. RSS 목록 가져오기
```
curl -s -A "Mozilla/5.0" "<위 표의 RSS URL>"
```
- RSS 자체는 Cloudflare 차단이 없어 curl로 바로 받힌다. (기사 본문 페이지·이미지 CDN과는 다름)
- `<item>` 별로 `title`, `link`, `pubDate` 를 파싱한다. hot-news(5)는 `<enclosure url="...">` 도 함께 파싱해 썸네일 후보 URL로 보관한다.
- `pubDate` 기준 최신순으로 정렬한다.

### 2. 신규 항목 선별 (중복 제거, 최신 3건)
1. **로컬 로그 1차 필터**: `references/published-log.md` 표에 이미 있는 URL은 후보에서 제외한다. (DB 조회 없이 바로 걸러지는 가장 빠른 1차 방어선)
2. **DB 2차 필터**:
   ```sql
   SELECT url FROM gen WHERE url IN (${1단계에서 파싱한 link 목록 중 1을 통과한 것});
   ```
   이미 `gen` 테이블에 존재하는 `url` (아직 발행 전이라 로그에는 없지만 이미 크롤링해 INSERT까지는 된 경우 포함) 도 후보에서 제외한다.
- 최신순으로 남은 후보 중 **최대 3건**만 이번 실행 대상으로 선택한다.
- 신규 대상이 0건이면 크롤링·발행 모두 건너뛰고 종료 보고.
- 이후 3~4단계를 **선택된 항목마다 순회**하며 수행한다. 한 건이 끝난 뒤 다음 건으로 넘어간다.

### 3. 기사별 크롤링 → headline/summary/썸네일 생성

#### 3-1. 본문 크롤링
- **Firecrawl만 사용한다** (`mcp__firecrawl__firecrawl_scrape`, `formats: ["markdown"]`, `onlyMainContent: true`). Aside로 기사 본문을 다시 읽지 않는다 — 위 "필요한 MCP" 섹션에 적어둔 대로 Aside의 접근성 트리는 노이즈가 훨씬 많아 본문 파악에 비효율적이라는 게 실측 결과다.
- 결과 markdown에는 실제 기사 본문 외에 광고, "다음 투자, 어떤 종목을 선택해야 할까요?"(ProPicks 홍보), "많이 본 기사"/"트렌딩 주식"/시세표, 댓글, 리스크 고지 등 **본문과 무관한 꼬리 섹션**이 함께 딸려온다. 실제 기사 제목~마지막 문단까지만 본문으로 취급하고 그 뒤는 무시한다.

#### 3-2. headline 생성
- 20자 이상 30자 이내로 작성.
- 자연스러운 한국어 단답식 문장. 느낌표·물음표 금지, 마침표만 사용.
- 핵심 수치, 구체적 기업명, 시간·날짜, 장소명 등을 가능한 포함.
- 문장형 종결어미(~입니다, ~했다 등) 금지. **명사로 끝나는 구문**으로 작성 (예: '애플의 신제품 발표', '삼성 주가 급등').
- 주요 고유명사는 영어 표기 유지.
- (www.investing.com은 영어 사이트라 원문이 영어인 경우가 대부분. 이 경우 위 스타일 규칙에 맞춰 한국어로 번역·재구성한다.)

#### 3-3. summary 생성
- 반드시 230자 이내.
- 자연스러운 한국어 격식체(~했습니다, ~입니다 등으로 종결, 구어체 배제).
- 통계적·객관적이고 수치적으로 정확한 문장을 간결하게.
- 주요 고유명사는 영어 표기 유지.

#### 3-4. 썸네일 이미지 1개 선정
- **contentsType 5 (hot-news)**: 1단계에서 파싱한 `<enclosure url>` 값을 그대로 썸네일 소스로 사용한다.
- **contentsType 4 (economic-analysis)**: RSS에 이미지가 없으므로, 3-1에서 크롤링한 본문 markdown 안에 인라인으로 포함된 `content-media.investing.com` 이미지(차트·인포그래픽 등) 중 기사 내용을 잘 나타내는 것 1개를 고른다.
  - 기자 프로필 사진(`company_logo` 경로), 페이지 하단 "많이 본 기사"/"트렌딩 주식" 목록의 관련기사 썸네일, 광고성 이미지는 제외한다.
  - 본문 안에 쓸만한 이미지가 전혀 없으면 이 항목은 건너뛰고 다음 후보로 넘어간다 (2단계에서 최신 3건을 못 채웠다면 그 다음 순번 후보로 대체).
- **다운로드**: 위에서 고른 이미지 URL은 `content-media.investing.com` 도메인이며 Cloudflare가 걸려 있어 curl/WebFetch로 직접 받으면 403이 난다. `mcp__aside__repl` 로 실제 브라우저 세션에서 해당 URL(또는 이미지가 걸린 기사 페이지)에 접속해 이미지를 로컬에 저장한다.
  - 저장 위치: `~/Downloads/investing-dot-com-crawling/<economic-analysis|hot-news>/<YYYYMMDD-HHMMSS>-<기사 article ID>/thumbnail.<원본 확장자>`
  - 저장 직후 파일이 실제로 존재하고 크기가 0바이트가 아닌지 확인한다. 실패하면 이 후보는 건너뛰고 다음 후보로 대체한다.

### 4. 로컬 DB(`gen`)에 저장 (MySQL MCP)
```sql
INSERT INTO gen (
    created_at,
    category,
    headline,
    highlight_texts,
    summary,
    media_type,
    thumbnail_image_url,
    url,
    core_texts_json,
    contents_type,
    published_via_skills_yn
) VALUES (
    NOW(),
    16,
    '${3-2에서 생성한 headline}',
    '',
    '${3-3에서 생성한 summary}',
    60,
    '${3-4에서 저장한 썸네일 로컬 절대경로}',
    '${1단계 기사 link}',
    '{}',
    ${4 또는 5},
    NULL
);
```
- `thumbnail_image_url` 에는 **원격 URL이 아니라 3-4에서 로컬에 저장한 파일의 절대경로**를 넣는다. (다른 contents_type과 다른 부분이니 혼동하지 말 것)
- `category` 는 항상 `16`, `media_type` 은 항상 `60` 고정.
- 이 gen row의 `id` (INSERT 후 `LAST_INSERT_ID()` 또는 재조회로 확보)와 3-4의 썸네일 임시 디렉토리 경로를 다음 단계까지 계속 추적한다 (5단계 정리에 필요).

선택된 항목(최대 3건) 모두에 대해 3~4단계를 마치면 5단계로 넘어간다.

### 5. 인스타그램 발행 (instagram-contents-publish 스킬 호출)
- `Skill` 도구로 `instagram-contents-publish` 를 호출한다. 이번에 사용한 `contentsType` 값(4 또는 5)을 그대로 전달해, 방금 INSERT한 미발행 gen 건들이 발행 대상으로 조회되게 한다.
- 이 스킬은 `gen.thumbnail_image_url` 이 로컬 절대경로인 경우 다운로드 단계 없이 그 파일을 바로 카드 배경 이미지로 사용하도록 이미 대응되어 있다 (원격 URL인 다른 contents_type과는 분기 처리됨).
- 발행 스킬이 각 gen에 대해 성공적으로 `published_via_skills_yn = 'Y'` 로 마킹했는지 결과를 확인한다.

### 6. 게시 완료 로그 기록
- 5단계에서 발행이 **성공**(`published_via_skills_yn = 'Y'` 확인됨)한 gen 건마다, `references/published-log.md` 표에 한 줄씩 추가한다: `contentsType`, 기사 URL, headline, gen id, 게시일(`YYYY-MM-DD`).
- 발행이 **실패**한 gen은 로그에 추가하지 않는다 (다음 실행 때 DB 기준으로는 여전히 잡히되, 로그에는 없으므로 재시도 흐름과 충돌하지 않음).

### 7. 원본 썸네일 임시 파일 정리
- 4단계에서 추적한 gen id 별로, 5단계 발행이 **성공**한 건만 3-4에서 만든 썸네일 임시 디렉토리(`~/Downloads/investing-dot-com-crawling/.../<...>/`)를 `rm -rf` 로 삭제한다.
- 발행이 실패한 gen은 임시 디렉토리를 **삭제하지 않고 남겨** 재시도 가능하게 하며, 실패 사유를 사용자에게 보고한다. (해당 gen row 자체도 삭제하지 않는다 — `published_via_skills_yn` 이 NULL로 남아 다음 발행 실행 때 다시 대상이 된다.)

## 주의사항
- 한 번 실행 시 RSS 신규 항목은 **최대 3건**까지만 크롤링한다.
- 중복 판단 기준은 `gen.url` (기사 원문 링크) 완전일치. `references/published-log.md` (로컬 파일, 발행 완료 건만) → `gen` 테이블 (DB, 발행 여부 무관 전체) 순으로 2중 확인한다.
- **크롤링을 시작하기 전에 반드시 `references/published-log.md` 를 먼저 읽어 이미 게시된 URL 목록을 확보**하고 나서 RSS 항목과 대조한다.
- 기사 본문 페이지·이미지 CDN(`content-media.investing.com`) 은 Cloudflare 봇 차단이 걸려 있어 일반 curl/WebFetch로는 403이 난다. 본문은 firecrawl_scrape, 이미지는 Aside(실브라우저)로 받는다. RSS 자체는 차단이 없어 curl로 충분하다.
- headline/summary 생성 시 광고·ProPicks 홍보문구·관련기사 목록·시세표·댓글·리스크 고지 등 기사 본문이 아닌 부분은 참고하지 않는다.
- `thumbnail_image_url` 은 이 스킬에서는 항상 **로컬 절대경로**로 저장한다 (다른 contents_type의 원격 URL 저장 방식과 다름).
- `category=16`, `media_type=60` 은 항상 고정값.
- 크롤링만 하고 발행을 생략해 달라는 요청이 아닌 한, 4단계(DB 저장) 이후 반드시 5단계(instagram-contents-publish 호출)까지 이어서 수행한다.
