# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

This is a Spring Boot Kotlin multi-module project using clean architecture principles.

### Module Structure
- **api/**: Main Spring Boot application entry point — wires together all domain/library modules
- **domain/**: Business logic modules
  - **generator/**: Content generation domain (scraping, GPT processing, scheduling, Instagram, image generation)
- **library/**: Shared infrastructure modules
  - **common/**: Common configurations, utilities, and SpringDoc OpenAPI setup
  - **email/**: Email sending (AWS SES + Thymeleaf templates)
  - **security/**: JWT authentication and authorization (JJWT, Spring Security)
  - **storage/**: File storage (AWS S3 integration)
  - **web/**: Web layer configurations, exception handling, CORS

### Architectural Patterns

**Clean Architecture**: Each domain module follows `Controller → UseCase → Service (optional) → Repository`

- **Controller**: If there is a request body, create a DTO in `controller/request/`. The controller calls `usecase.execute()` and returns the usecase Out class. Response DTOs go in `controller/response/`.
- **UseCase**: Class name starts with a verb. Each UseCase must have an `execute()` method. Input DTOs go in `usecase/input/`, output DTOs in `usecase/out/`.
- **Service**: Only use when accessing JPA entities from a different domain/module.
- **Repository**: Even for default JPA methods (e.g., `save`), always declare them in the repository interface.

### Key Technologies
- **Kotlin 1.9.24** with **Spring Boot 3.2.5**, JVM 21
- **JPA** (Hibernate) with custom converters, separate EntityManagerFactory per module
- **Coroutines** (`kotlinx-coroutines-core`, `kotlinx-coroutines-slf4j`, reactor extensions)
- **OpenAI GPT** via Spring Cloud OpenFeign (custom encoder/decoder/interceptor in `core/gpt/`)
- **Jsoup 1.17.2** + OkHttp 4.12.0 for web scraping (`core/scrapper/`)
- **Instagram Graph API** integration (`core/instagram/`)
- **AWS SES** for email delivery, **AWS S3** / Spring Cloud AWS for storage
- **EHCache 3** for application-level caching
- **JOOQ 3.19.10** for query building
- **SpringDoc OpenAPI 2.5.0** (Swagger UI)
- **Slack Webhook** client for notifications

### Domain/Generator Package Structure

```text
domain/generator/src/main/kotlin/com/few/generator/
├── config/               # Module-level Spring config (JPA, DataSource, Cache, Feign, OkHttp)
│   ├── feign/            # OpenAI Feign customization (encoder, decoder, interceptor, thread-local)
│   ├── instagram/        # InstagramOkHttpConfig
│   ├── jpa/              # GeneratorJpaConfig, GeneratorDataSourceConfig, GeneratorCacheConfig
│   ├── properties/       # JsoupProperties
│   └── scrapper/         # ScrapperOkHttpFactory
├── controller/           # REST controllers (V1/V2 versioned)
├── core/
│   ├── gpt/              # OpenAI ChatGPT integration (completion, prompt, prompt/schema)
│   ├── instagram/        # Instagram API client
│   └── scrapper/         # Scrapper interface + cnbc/ and naver/ implementations
├── domain/               # JPA entities (Gen, GroupGen, Subscription, SubscriptionHis, RawContents, ...)
│   └── vo/               # Value objects
├── event/                # Spring ApplicationEvents, handlers, listeners
├── repository/           # Repository interfaces
├── service/              # Services (groupgen/, newsletter/, common generation)
├── support/              # aws/, common/, jpa/, utils/
└── usecase/              # UseCases (input/, out/ sub-packages)
```

### Event-Driven Architecture

The generator domain uses Spring ApplicationEvent for async processing:
- **Events**: `CardNewsImageGeneratedEvent`, `CardNewsS3UploadedEvent`, `ContentsSchedulingEvent`, `EnrollSubscriptionEvent`, `GenSchedulingCompletedEvent`, `InstagramUploadCompletedEvent`, `UnsubscribeEvent`
- **Handlers/Listeners**: corresponding handler and listener classes

### Scraping Architecture

- Abstract `Scrapper` interface with extractor pattern
- Implementations: `NaverScrapper` (NaverExtractor, NaverConstants), `CnbcScrapper` (CnbcExtractor, CnbcConstants)
- Jsoup + OkHttp (with Brotli compression support)

### Transactional Annotations

Custom `@GeneratorTransactional` annotation per domain module, pointing to the module-specific `TransactionManager` bean.

### Caching

- EHCache 3 managed via `GeneratorCacheConfig`
- Cache names defined in `CacheNames.kt`
- `SendCacheMetricsSchedulingUseCase` for monitoring cache metrics

### Code Quality

```bash
# Check code style
./gradlew ktlintCheck

# Format code (auto-runs via pre-commit hook)
./gradlew ktlintFormat

# Install pre-commit hook
./gradlew installGitHooks
```

Pre-commit hook (`scripts/pre-commit`) runs `ktlintFormat` on staged files and re-adds them.

SonarQube integration is configured for `sonarcloud.io` (organization: `few-letter`, project: `few-letter_few-be`).

### Testing

Uses **Kotest 5.8.0** (with Spring extensions) and **MockK 1.13.9**. JUnit 5 integration tests tagged `"integration"` are excluded from default test runs.

Existing test files:
- `domain/generator/src/test/kotlin/com/few/generator/cache/GenCacheTest.kt`
- `domain/generator/src/test/kotlin/com/few/generator/usecase/` (BrowseContentsUseCaseTest, InstagramUploadUseCaseTest, GenCardNewsImageGenerateSchedulingUseCaseTest, AbstractGenSchedulingUseCaseTest, AbstractGroupGenSchedulingUseCaseTest)
- `domain/generator/src/test/kotlin/com/few/generator/service/instagram/` (InstagramImageGeneratorTest, MainPageCardGeneratorTest, GenCardNewsImageGenerateLocalTest)
- `library/email/src/test/kotlin/com/few/email/provider/AwsSendEmailServiceProviderTest.kt`

**Rule**: If you modify code, update the corresponding test code. If no test exists for that code, skip.

### Profiles

- **local**: Local development — uses `application-*-local.yml` files
- **prd**: Production — uses `application-*-prd.yml` files

Profiles are grouped in `api/src/main/resources/application.yml`:
- `local` activates: `email-local`, `security-local`, `storage-local`, `web-local`, `generator-local`, `provider-local`
- `prd` activates: `email-prd`, `security-prd`, `storage-prd`, `web-prd`, `generator-prd`, `provider-prd`

### Module-Specific Configs

Each module has its own config files under `<module>/src/main/resources/`:
```
application-generator-{local|prd}.yaml
application-email-{local|prd}.yml
application-security-{local|prd}.yml
application-storage-{local|prd}.yml
application-web-{local|prd}.yml
```

### UseCase Pattern

```kotlin
// Input DTO in usecase/input/
data class BrowseContentsUseCaseIn(...)

// Output DTO in usecase/out/
data class BrowseContentsUseCaseOut(...)

// UseCase class with execute()
class BrowseContentsUseCase(...) {
    fun execute(input: BrowseContentsUseCaseIn): BrowseContentsUseCaseOut { ... }
}
```

### Deployment

- **Dockerfile** (`api/Dockerfile`): Amazon Corretto 21 on AL2023, CJK fonts for image generation, port 8080, JDWP on 5005, timezone Asia/Seoul
- **Alternative Dockerfiles**: `Dockerfile.dev.pinpoint`, `Dockerfile.prd.pinpoint` (APM tracing)
- **Local Docker Compose**: `resources/local-develop-environment/docker-compose.yml`
