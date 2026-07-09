# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Run

```bash
# Full build (tests are skipped by default in surefire config)
mvn clean install -DskipTests

# Build a single module and its dependencies
mvn -pl libs/boot-common -am install

# Run tests for a specific module
mvn -pl libs/boot-common test -DskipTests=false

# Run the application (from apps/boot-business)
mvn -pl apps/boot-business spring-boot:run

# Run with a specific profile
mvn -pl apps/boot-business spring-boot:run -Dspring-boot.run.profiles=dev
```

No Maven wrapper is present — system Maven 3.8+ is required. Java 17+.

## Architecture

This is a **multi-module Maven project** (`com.mhd:mhd-boot`, version `1.0.0`) — a Spring Boot 3.5.9 scaffolding/framework for enterprise applications.

### Module layout

```
mhd-boot/
├── pom.xml                    # Root aggregator/parent POM (dependencyManagement for all versions)
├── libs/                      # Reusable library modules
│   ├── boot-common            # Core: JsonUtils, XmlUtils, FileUtils, BusinessException, ErrorCodeEnum, BaseResponse/BasicResultVO
│   ├── boot-common-web        # Web layer: GlobalExceptionHandler, CORS, Undertow config, SpringUtils
│   ├── boot-common-mybatis    # MyBatis-Plus: MybatisPlusConfig (pagination, optimistic lock, snowflake ID), PageParam, PageResponse
│   ├── boot-common-doc        # SpringDoc OpenAPI integration
│   ├── boot-common-security   # JWT authentication utilities (JwtUtils)
│   ├── boot-common-sftp       # SFTP connection pool and file transfer
│   ├── boot-common-job        # Scheduled task / job framework (Quartz)
│   ├── boot-common-operatelog # AOP-based operation logging
│   ├── boot-common-idempotent # Idempotency support (annotation-driven)
│   └── boot-ai                # Spring AI integration (LlmProviderRegistry, StructuredOutputInvoker)
├── apps/                      # Application modules
│   └── boot-business          # Main Spring Boot app (entry point)
├── deploy/                    # Deployment: server.sh, docker/docker-compose.yml, RocketMQ scripts
├── front-end/                 # Frontend placeholder (empty)
└── docs/                      # Project documentation
```

### Application entry point

`BootApplication.java` (`com.mhd.boot.web`) — annotated with `@SpringBootApplication(scanBasePackages = {"com.mhd.boot.web","com.mhd.boot.ai"})`. Uses **Undertow** as the web server (not Tomcat). Also includes `BootServletInitializer` for optional WAR deployment.

### Technology stack (as actually configured)

| Concern | Technology |
|---|---|
| Framework | Spring Boot 3.5.9, Java 17 |
| Web server | Undertow |
| ORM | MyBatis-Plus 3.5.14 (NOT JPA — despite what some docs say) |
| Database | PostgreSQL (primary), MySQL connector included |
| Connection pool | Druid 1.2.27, Dynamic DS 4.3.1 (multi-datasource) |
| Cache / distributed | Redisson 3.51.0, Redis |
| Object storage | MinIO 8.5.9 |
| Messaging | RocketMQ 2.3.5, Redis Stream |
| AI | Spring AI 1.0.0 — DashScope (Alibaba Cloud) + OpenAI-compatible, PgVector |
| Job scheduling | XXL-Job 2.3.0, Quartz |
| Documentation | SpringDoc OpenAPI 2.8.14 |
| JSON | Jackson 3.1.3 (core), Jackson 2.21 (annotations) — mixed major versions |
| Utilities | Hutool 5.8.40, FastJSON2 2.0.43, Lombok, MapStruct |
| PDF | iText 8, Apache Tika |

## Key Conventions

### Response format
All API endpoints **must** return `BaseResponse<T>`. Use `BaseResultUtils` to build responses:

```java
// Success
return BaseResultUtils.success();
return BaseResultUtils.successOfData(data);
// Error
throw new BusinessException(ErrorCodeEnum.RESUME_NOT_FOUND, "简历不存在");
```

`GlobalExceptionHandler` (`boot-common-web`) catches unhandled exceptions and wraps them as `BaseResponse`. Do not return raw entities, Lists, or Maps from controllers. There is also `BasicResultVO` in the `pipeline` package — this is an alternative response wrapper used in specific pipeline contexts, not the primary API response format.

### Error codes
Use `ErrorCodeEnum` (implements `ProcessResult`) for all error codes. Domains:
- `1xxx` — General (SUCCESS=200, FAIL=-1)
- `2xxx` — Resume
- `3xxx` — Interview
- `4xxx` — Storage
- `5xxx` — Export
- `6xxx` — Knowledge base
- `7xxx` — AI service
- `8xxx` — Rate limit
- `9xxx` — Interview schedule
- `10xxx` — Voice
- `11xxx` — LLM Provider
- `Axxxx` — Client errors (bad params, no login, duplicate request)
- `Bxxxx` — System errors

### Exceptions
Always throw `BusinessException(ErrorCodeEnum.XXX, "message")` — never `throw new RuntimeException(...)`. Never silently swallow exceptions.

### Layering
Controller → Service → Mapper (MyBatis-Plus). Controllers handle routing and validation only (no business logic). Services own transaction boundaries (`@Transactional`). Never call external APIs/LLM/S3 inside a transactional method. Prefer constructor injection over `@Autowired`.

### Naming
Follow Alibaba Java Development Handbook: classes PascalCase, methods/variables camelCase, constants UPPER_SNAKE_CASE. Suffixes: `XxxEntity`, `XxxDTO`, `XxxRequest`, `XxxResponse`. Prefer `record` for immutable DTOs. No wildcard imports.

### Logging
SLF4J + Logback (`@Slf4j`). Use structured logging: `log.info("key={}, value={}", k, v)`. Always pass the exception object as the last parameter: `log.error("msg", e)` — never `log.error(e.getMessage())`. Exclude conflicting SLF4J bindings from dependencies (e.g., `slf4j-simple` from DashScope SDK).

### Database
Use parameterized queries or `LambdaQueryWrapper` — never string concatenation for SQL. All Redis keys must be defined in constants, never hardcoded.

### Configuration
Use `@ConfigurationProperties` for type-safe config. Sensitive values (passwords, keys) go in environment variables or `.env`, never hardcoded. Profile-specific config in `application-{profile}.yml`.

## Key Classes

| Class | Location | Purpose |
|---|---|---|
| `BaseResultUtils` | `libs/boot-common/.../respnsedata/` | Static factory for `BaseResponse<T>` — the primary way to build API responses |
| `BaseResponse<T>` | `libs/boot-common/.../respnsedata/` | Standard API response wrapper (code, data, msg) |
| `BasicResultVO<T>` | `libs/boot-common/.../pipeline/` | Alternative response wrapper for pipeline contexts |
| `ErrorCodeEnum` | `libs/boot-common/.../enums/` | Centralized error code enum |
| `BusinessException` | `libs/boot-common/.../exception/` | Business exception with error code |
| `GlobalExceptionHandler` | `libs/boot-common-web/.../handler/` | `@RestControllerAdvice` that maps exceptions to `BaseResponse` |
| `JsonUtils` | `libs/boot-common/.../utils/json/` | Jackson JSON serialization/deserialization |
| `MybatisPlusConfig` | `libs/boot-common-mybatis/.../config/` | Auto-configures pagination, optimistic locking, snowflake ID |
| `PageParam` / `PageResponse` | `libs/boot-common-mybatis/.../domain/` | Pagination request/response DTOs |
| `MybatisPlusUtils` | `libs/boot-common-mybatis/.../util/` | Page builder and sort utilities |
| `JwtUtils` | `libs/boot-common-security/.../core/util/` | JWT token utilities |
| `LlmProviderRegistry` | `libs/boot-ai/.../llm/` | Multi-provider LLM registry and lookup |
| `RateLimit` / `RateLimitAspect` | `apps/boot-business/.../common/` | Annotation-driven rate limiting (AOP + Redis Lua) |

## Infrastructure (Docker Compose)

`deploy/docker/docker-compose.yml` provides the full backing services for local development:

```bash
cd deploy/docker && docker-compose up -d
```

Services: PostgreSQL 16 + pgvector, Redis 7, MinIO (S3-compatible, bucket `mhd-boot` auto-created), RocketMQ.

The dev profile (`application-dev.yml`) expects these on their default local ports.

## Important Notes

- **Doc/code mismatch**: `docs/DEVELOPMENT_GUIDELINES_MHD_BOOT.md` references Spring Boot 4.x, Java 21, and Spring Data JPA. The actual codebase uses Spring Boot 3.5.9, Java 17, and MyBatis-Plus. Follow the code, not the aspirational docs.
- **Tests are skipped by default** (`maven-surefire-plugin` has `<skip>true</skip>` in the root POM). Use `-DskipTests=false` to run them.
- **Jackson version split**: The project uses Jackson 3.1.3 (core via Spring Boot 3) but Jackson 2.21 annotations. Avoid introducing additional Jackson 2.x core dependencies.
- **Aliyun Maven mirror**: The root POM is configured to use `maven.aliyun.com` as the repository — builds may fail from networks that cannot reach it.
- **No CI/CD configured**: No GitHub Actions workflows or Jenkinsfiles exist yet.
