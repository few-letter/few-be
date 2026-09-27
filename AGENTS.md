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

## Development & Testing Rules

Architecture layering (Controller → UseCase → Service → Repository), testing policy, and other soft guidelines live in [`.claude/rules/`](.claude/rules/).

Workflow rules that must never be violated (branch naming, branching from `dev`, commit message format) are mechanically enforced via hooks — see [`.claude/hooks/README.md`](.claude/hooks/README.md).
