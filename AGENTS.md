# AI Agent guidance

This file provides guidance to AI Agents (ex. Claude Code) when working with code in this repository.

## Project Overview

`few-be` is a Spring Boot Kotlin multi-module project built with clean architecture principles. It powers a content-generation platform: scraping news/stock data, generating content via GPT, producing card-news images, and publishing to Instagram.

### Module Structure

- **api/**: Main Spring Boot application entry point — wires together all domain/library modules.
- **domain/generator/**: Content generation domain (scraping, GPT processing, scheduling, Instagram, image generation). Covers multiple content pipelines: Naver/CNBC news card-news, Naver stock-briefing, TimeEtf (timefolio), Alpha Vantage Nasdaq popular-stock news, and a Claude-Code-skill-driven Investing.com pipeline.
- **library/common/**: Common configurations, utilities, SpringDoc OpenAPI setup, and shared enums (`ContentsType`, `MediaType`).
- **library/email/**: Email sending (AWS SES + Thymeleaf templates).
- **library/security/**: JWT authentication and authorization (JJWT, Spring Security).
- **library/storage/**: File storage (AWS S3 integration).
- **library/web/**: Web layer configurations, exception handling, CORS.

### Key Technologies

- **Kotlin 1.9.24** with **Spring Boot 3.2.5**, JVM 21
- **JPA** (Hibernate) with custom converters, separate `EntityManagerFactory` per module
- **Coroutines** (`kotlinx-coroutines-core`, reactor extensions)
- **Spring AI** (`spring-ai-starter-model-openai`) for OpenAI ChatGPT integration
- **Alpha Vantage** News & Sentiment API client for Nasdaq popular-stock news
- **Jsoup** + **OkHttp** for web scraping, and a Claude Code skill for the Investing.com pipeline (not a `Scrapper` implementation — see `TriggerContentsPublishSkillsUseCase`)
- **Instagram Graph API** integration
- **AWS SES**/**S3** via Spring Cloud AWS
- **EHCache 3** for application-level caching, **JOOQ** for query building
- **SpringDoc OpenAPI** (Swagger UI), **Slack Webhook** for notifications

## Contents Publishing Pipeline Via Claude Code Skills

Some contents are published to Instagram by Claude Code via a non-interactive shell run, not by Kotlin code.

- **Flow**: `TriggerContentsPublishSkillsUseCase` runs [`.claude/scripts/publish-contents-common.sh`](.claude/scripts/publish-contents-common.sh) `<contentsTypeCode>` → script runs `claude -p` from the project root → claude executes a project skill.
  - Triggered by `TriggerContentsPublishSkillsEvent` after local/global gen scheduling (contentsType 1, 2), or by the `economic-analysis` / `hot-news` schedules (contentsType 4, 5).
- **Skills** ([`.claude/skills/`](.claude/skills/)):
  - `investing-dot-com-crawling`: crawls Investing.com RSS, saves to `gen` (contentsType 4, 5), then chains into `instagram-contents-publish`.
  - `instagram-contents-publish`: renders one card image per unpublished `gen` from `html_templates/main_page_template.html`, posts it to Instagram (and Naver blog), and marks the publish flag.
  - Required MCP servers: `mcp_server_mysql`, `aside`, `firecrawl` (skills call tools by these names).
- **Script behavior**:
  - Auth: sources `.claude/non-interactive-claude-config.env` (gitignored; `CLAUDE_CODE_OAUTH_TOKEN` from `claude setup-token`). `--model auto` is added only when `ANTHROPIC_BASE_URL` is set there.
  - Logs: `logs/single-contents-publish.log`. Skill temp files: `tmp/`. Both are gitignored.
  - On failure only, posts the reason to Discord via `DISCORD_WEBHOOK_URL` env, injected by the UseCase from `urls.webhook.discord`. Never hardcode the webhook URL.
  - Resolve all paths relative to the script location (`${0:A:h}`), never cwd. The JVM and cron run it from other directories.

## Development & Testing Rules

Architecture layering (Controller → UseCase → Service → Repository), testing policy, and other soft guidelines live in [`.claude/rules/`](.claude/rules/).

Workflow rules that must never be violated (branch naming, branching from `dev`, commit message format) are mechanically enforced via hooks — see [`.claude/hooks/README.md`](.claude/hooks/README.md).
