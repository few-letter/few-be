# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Architecture

This is a Spring Boot Kotlin multi-module project using clean architecture principles:

### Module Structure
- **api/**: Main Spring Boot application module that wires together domain modules
- **domain/**: Business logic modules
  - **generator/**: Content generation domain (scraping, GPT processing, scheduling)
  - **provider/**: Subscription management domain(You should only write code that is freely scalable, consisting of stateless logic.)
- **library/**: Shared infrastructure modules
  - **common/**: Common configurations and utilities
  - **email/**: Email sending functionality (AWS SES, templates)
  - **security/**: JWT authentication and authorization
  - **storage/**: File storage (S3 integration for images/documents)  
  - **web/**: Web layer configurations, exception handling, CORS

### Architectural Patterns
- **Clean Architecture**: Each domain module follows Controller -> UseCase -> Service(Optional) -> Repository pattern
    - Controller: If there is a request body, a DTO class must be created in controller/request. The controller calls the usecase's execute method and, if there is a return value, returns the usecase out class. If a response DTO is required, a class must be created in controller/response.
    - UseCase: The class name begins with a verb, and each UseCase class must have an execute method.
    - Service: Only use if you are accessing a JPA entity that is different from the current one. 
    - Repository: Even if you use the methods provided by default in JPA (ex. save), you must specify them in the repository interface.

### Key Technologies
- **Kotlin** with Spring Boot 3.2.5
- **JPA** with custom converters for complex types
- **Coroutines** for async processing
- **OpenAI GPT** integration for content generation
- **Jsoup** for web scraping
- **AWS SES** for email delivery

### Code Quality
```bash
# Check code style
./gradlew ktlintCheck

# Format code (auto-runs via pre-commit hook)
./gradlew ktlintFormat
```

### Testing
Uses Kotest framework. No tests currently exist in the codebase.

### Profiles
- **local**: Local development (uses application-*-local.yml files)
- **prd**: Production (uses application-*-prd.yml files)

### Module-Specific Configs
Each domain/library module has its own application config files following the pattern:
`application-{module-name}-{profile}.yml`

### UseCase Pattern
Business logic is encapsulated in UseCase classes with input/output DTOs:
```kotlin
// Example structure found in domain modules
BrowseContentsUseCase(input: BrowseContentsUseCaseIn): BrowseContentsUseCaseOut
```

### Transactional Annotations
Custom transactional annotations per module (e.g., `@GeneratorTransactional`, `@ProviderTransactional`).

