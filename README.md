<p align="center">
  <img src="https://github.com/user-attachments/assets/c970feac-c5a5-40f1-959d-3223be8d539d" alt="few-logo" width="150" height="150" />
</p>

<p align="center">
  <a href="https://github.com/few-letter/few-be/actions/workflows/ecs-cd.yml">
    <img src="https://github.com/few-letter/few-be/actions/workflows/ecs-cd.yml/badge.svg" alt="deploy-prd" />
  </a>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Kotlin-1.9.24-7F52FF?logo=kotlin&logoColor=white" alt="Kotlin" />
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?logo=springboot&logoColor=white" alt="Spring Boot" />
  <img src="https://img.shields.io/badge/JDK-21-ED8B00?logo=openjdk&logoColor=white" alt="JDK 21" />
  <img src="https://img.shields.io/badge/Claude%20Code-Skills-D97757?logo=anthropic&logoColor=white" alt="Claude Code" />
</p>

---

## 🚀 Getting Started

### 1. 로컬 인프라 실행

MySQL · Adminer 컨테이너를 띄웁니다.

```bash
cd scripts && ./local-develop-env-reset
```

| 서비스 | 주소 | 비고 |
| :-- | :-- | :-- |
| MySQL 8 | `localhost:13306` | `root` / `root` |
| Adminer | http://localhost:18080 | DB 웹 콘솔 |

### 2. 애플리케이션 실행

```bash
./gradlew :api:bootRun --args='--spring.profiles.active=local'
```

---

## 🤖 Claude Code 콘텐츠 발행 설정

콘텐츠 발행은 애플리케이션이 [`.claude/scripts/publish-contents-common.sh`](.claude/scripts/publish-contents-common.sh)를 실행하고,
이 스크립트가 **비대화형 Claude Code**로 프로젝트 Skill(`.claude/skills/`)을 수행하는 방식으로 동작합니다.

### 필요한 MCP

| MCP | 용도 |
| :-- | :-- |
| 🗄️ [**MySQL MCP**](https://github.com/benborla/mcp-server-mysql) | `gen` 테이블 조회 · 저장 · 발행 플래그 마킹 |
| 🌐 [**Aside MCP**](https://aside.com/blog/developers) | 인스타그램 · 네이버 블로그 브라우저 자동화 |
| 🔥 [**Firecrawl MCP**](https://github.com/firecrawl/firecrawl-mcp-server) | Investing.com 기사 크롤링 |

> [!IMPORTANT]
> Skill이 MCP 툴을 이름으로 호출하므로 서버 이름을 아래처럼 등록해야 합니다.
> `mcp_server_mysql` · `aside` · `firecrawl`

### 비대화형 Claude 인증

스크립트는 JVM/cron 같은 비대화형 환경에서 실행되므로 macOS 키체인 로그인을 사용할 수 없습니다.
장기 토큰을 발급해 env 파일에 등록해 주세요.

**① 토큰 발급**

```bash
claude setup-token
```

**② `{PROJECT_ROOT}/.claude/non-interactive-claude-config.env` 작성**

```bash
export CLAUDE_CODE_OAUTH_TOKEN=<발급받은 토큰>

# omniroute config (Optional)
export ANTHROPIC_BASE_URL="http://localhost:20128"
export ANTHROPIC_AUTH_TOKEN="<OMNIRoute Auth Token>"
```

> [!NOTE]
> 실행 로그는 프로젝트 루트의 `logs/single-contents-publish.log`에 쌓입니다. (git 추적 제외)
