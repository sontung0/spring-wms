# API / Worker Module Split Design

**Date:** 2026-06-27
**Status:** Approved

## Overview

Split the current monolithic `app` module into three Maven modules: `app` (shared library), `api` (HTTP layer), and `worker` (background task scaffolding). This enables separate deployment of the API server and background worker processes, sharing the same domain logic and database.

## Architecture

```
┌─────────────────────────────────────────────────┐
│                   root pom.xml                   │
│           packaging: pom, modules:               │
│         app / api / worker / e2e                 │
└─────────────────────────────────────────────────┘
                          │
          ┌───────────────┼───────────────┐
          ▼               ▼               ▼
   ┌──────────┐    ┌──────────┐    ┌──────────┐
   │   app    │    │   api    │    │  worker  │
   │ plain jar│    │ boot jar │    │ boot jar │
   │          │    │          │    │          │
   │ domain   │◄───│ depends  │◄───│ depends  │
   │ services │    │ on app   │    │ on app   │
   │ repos    │    │          │    │          │
   │ entities │    │ web      │    │ (empty,  │
   │ OAuth    │    │ layer    │    │ future   │
   └──────────┘    │ only     │    │ tasks)   │
                   └──────────┘    └──────────┘
                        │                │
                   separate JVM     separate JVM
                   port 8080        no HTTP server
```

## Module Details

### Module: `app` (plain jar)

**ArtifactId:** `app` (unchanged)
**Packaging:** `jar` (unchanged)
**Boot plugin:** Removed (no longer a runnable application)

**Kept dependencies:** actuator, restclient, spring-boot-starter-json, modulith-core, modulith-starter-test (test), data-jpa, mysql-connector-j, cache, caffeine, flyway-core, flyway-mysql, flyway-starter, h2 (test), lombok

**Removed dependencies:** webmvc, webmvc-test, security, oauth2-resource-server, validation, springdoc, spring-security-test

**Contents:** All existing code that is NOT presentation/web: domain entities, application services, infrastructure adapters (OAuth providers, token service, repositories, JPA entities, specifications, caching), common error classes (BusinessException, NotFoundException, GlobalExceptionLoggingResolver), exceptions.

**Config:** `application.properties` stays in `app` — both `api` and `worker` inherit these shared settings (DB, Flyway, JPA, cache, auth secrets) via classpath.

### Module: `api` (Spring Boot jar, new)

**ArtifactId:** `wms-api`
**Parent:** `wms` root pom
**Packaging:** `jar`
**Boot plugin:** spring-boot-maven-plugin (repackage)
**Main class:** `nst.wms.ApiApplication`

**Dependencies:**
- `app` (compile)
- spring-boot-starter-webmvc
- spring-boot-starter-security
- spring-boot-starter-oauth2-resource-server
- spring-boot-starter-validation
- spring-boot-starter-actuator
- springdoc-openapi-starter-webmvc-ui (3.0.3)
- spring-boot-starter-webmvc-test (test)
- spring-security-test (test)
- lombok (optional)

**Contents:** Web-layer classes moved from `app`:
- `nst.wms.ApiApplication` (renamed from WmsApplication)
- `nst.wms.auth.presentation.*` — AuthController, AuthExceptionHandler, DTOs
- `nst.wms.auth.infrastructure.SecurityConfig` — SecurityConfig, OpenApiConfig
- `nst.wms.user.presentation.*` — UserController, DTOs
- `nst.wms.common.error.GlobalExceptionHandler` — centralized error handler
- `nst.wms.common.api.ErrorResponse` — error response DTO

**Config:** `api/src/main/resources/application.properties` with only web-specific overrides (server.port, Swagger paths, etc.). Shared config inherited from `app` on classpath.

### Module: `worker` (Spring Boot jar, new)

**ArtifactId:** `wms-worker`
**Parent:** `wms` root pom
**Packaging:** `jar`
**Boot plugin:** spring-boot-maven-plugin (repackage)
**Main class:** `nst.wms.worker.WorkerApplication`

**Dependencies:**
- `app` (compile)
- spring-boot-starter-actuator
- lombok (optional)

**Contents:**
- `nst.wms.worker.WorkerApplication` — bare `@SpringBootApplication`
- `worker/src/main/resources/application.properties` — minimal (`spring.application.name=wms-worker`)

No `@EnableAsync`, no `@EnableScheduling`, no `AsyncConfig` — these are added when actual background tasks are introduced.

### Module: `e2e` (dependency update)

**Change:** Switch dependency from `app` to `api`.

Currently `e2e` depends on `app` to run `@SpringBootTest` against the boot jar. After the split, the Spring Boot application class (`ApiApplication`) lives in `api`, so `e2e` must depend on `api` instead (which transitively brings `app`).

## File Movements

All files listed below move from `app/src/...` to `api/src/...`. Package names stay the same (`nst.wms.*`). No code changes — pure file relocation.

| Source (in `app`) | Destination (in `api`) |
|---|---|
| `WmsApplication.java` | `ApiApplication.java` |
| `auth/presentation/AuthController.java` | `auth/presentation/AuthController.java` |
| `auth/presentation/AuthExceptionHandler.java` | `auth/presentation/AuthExceptionHandler.java` |
| `auth/presentation/dto/*` (3 files) | `auth/presentation/dto/*` |
| `auth/infrastructure/SecurityConfig.java` | `auth/infrastructure/SecurityConfig.java` |
| `auth/infrastructure/OpenApiConfig.java` | `auth/infrastructure/OpenApiConfig.java` |
| `user/presentation/UserController.java` | `user/presentation/UserController.java` |
| `user/presentation/dto/*` (6 files) | `user/presentation/dto/*` |
| `common/error/GlobalExceptionHandler.java` | `common/error/GlobalExceptionHandler.java` |
| `common/api/ErrorResponse.java` | `common/api/ErrorResponse.java` |
| `resources/application.properties` | duplicate into `api` (with web overrides) |
| Test files for moved classes | corresponding `api/src/test/java/...` |

## Build Order

```
1. app    (plain jar — compiles first)
2. api    (depends on app)
3. worker (depends on app)
4. e2e    (depends on app + api)
```

`mvn clean install` from root builds everything in correct order.

## Deployment

- **api:** `java -jar api/target/wms-api-*-boot.jar` — port 8080, full HTTP
- **worker:** `java -jar worker/target/wms-worker-*-boot.jar` — no HTTP, connects to same DB
