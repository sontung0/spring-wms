# User Management REST API — Design Spec

## Overview

A REST API for managing users in the WMS system, following Clean Architecture with module-first organization. Includes full CRUD operations, search/filter with pagination, Bean Validation, structured error responses, OpenAPI documentation, and Flyway data migrations.

## Tech Stack

- **Java 26**
- **Spring Boot 4.1.0**
- **Spring Data JPA** with MySQL
- **Spring Modulith** for module boundaries
- **SpringDoc OpenAPI** for API documentation
- **Flyway** for database migrations
- **H2** for test database

---

## Package Structure (Module-First, Clean Architecture)

```
src/main/java/nst/wms/
├── WmsApplication.java
│
├── user/                              ← User module (self-contained)
│   ├── domain/                        ← PUBLIC: core domain
│   │   ├── User.java                  ← Domain entity (no framework deps)
│   │   └── UserNotFoundException.java ← Domain exception
│   │
│   ├── application/                   ← PUBLIC: use cases
│   │   ├── UserService.java           ← Input port interface
│   │   └── UserServiceImpl.java       ← Implementation
│   │
│   └── internal/                      ← PRIVATE: Modulith convention
│       ├── infrastructure/
│       │   ├── UserRepository.java         ← Output port interface
│       │   ├── UserJpaEntity.java          ← JPA persistence model
│       │   ├── UserRepositoryAdapter.java  ← Implements UserRepository
│       │   ├── UserJpaRepository.java      ← Spring Data JPA interface
│       │   └── UserSpecification.java      ← Builds JPA Specifications from filters
│       │
│       └── presentation/
│           ├── UserController.java         ← REST endpoints
│           ├── UserExceptionHandler.java   ← Maps domain exceptions → HTTP
│           └── dto/
│               ├── CreateUserRequest.java  ← POST request body
│               ├── UpdateUserRequest.java  ← PUT request body
│               ├── UserResponse.java       ← Single user response
│               ├── UserSummary.java        ← Lightweight list item
│               ├── PageResponse.java       ← Generic paginated wrapper
│               └── UserFilter.java         ← Search/filter params
│
└── common/                            ← PUBLIC: shared across modules
    └── api/
        └── ErrorResponse.java         ← Structured error response format
```

### Public vs. Internal

| Layer | Access | Reason |
|-------|--------|--------|
| `domain/` | PUBLIC | Other modules need `User` entity and exceptions |
| `application/` | PUBLIC | Other modules may invoke use cases |
| `internal/` | PRIVATE | Implementation details (JPA, REST, adapters) |
| `common/` | PUBLIC | Shared across all modules |

---

## Domain Model

### User Entity

| Field | Type | Description |
|-------|------|-------------|
| `id` | `Long` | Auto-generated primary key |
| `name` | `String` | User name (max 255 chars) |
| `createdAt` | `LocalDateTime` | Set automatically on creation |
| `updatedAt` | `LocalDateTime` | Set automatically on update |

### UserNotFoundException

Thrown when a user is looked up by ID but doesn't exist. Lives in the domain layer — it's a business rule, not an infrastructure concern.

---

## API Endpoints

| Method | Path | Description | Request Body | Response |
|--------|------|-------------|--------------|----------|
| `POST` | `/api/users` | Create a new user | `CreateUserRequest` | `UserResponse` (201) |
| `GET` | `/api/users` | List/search users with pagination | Query params | `PageResponse<UserSummary>` |
| `GET` | `/api/users/{id}` | Get user by ID | — | `UserResponse` |
| `PUT` | `/api/users/{id}` | Update user | `UpdateUserRequest` | `UserResponse` |
| `DELETE` | `/api/users/{id}` | Delete user | — | 204 No Content |

### List/Search Endpoint

```
GET /api/users?name=John&page=0&size=20&sort=name,asc
```

| Param | Default | Description |
|-------|---------|-------------|
| `name` | — | Optional: search by name (case-insensitive, partial match) |
| `page` | `0` | Page number (0-indexed) |
| `size` | `20` | Items per page |
| `sort` | `createdAt,desc` | Sort field and direction |

---

## Application Layer (Input Ports)

### UserService Interface

```java
public interface UserService {
    User create(User user);
    User findById(Long id);
    Page<User> search(UserFilter filter, Pageable pageable);
    User update(Long id, User user);
    void deleteById(Long id);
}
```

### UserServiceImpl

Implements `UserService`. Handles business logic:
- Sets `createdAt`/`updatedAt` timestamps
- Delegates data access to `UserRepository`
- Throws `UserNotFoundException` when user not found

---

## DTOs

### Request DTOs

**CreateUserRequest:**
```java
public class CreateUserRequest {
    @NotBlank
    @Size(max = 255)
    private String name;
}
```

**UpdateUserRequest:**
```java
public class UpdateUserRequest {
    @NotBlank
    @Size(max = 255)
    private String name;
}
```

### Response DTOs

**UserResponse** (single user — POST, GET /{id}, PUT):
```java
public class UserResponse {
    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
```

**UserSummary** (list item — lighter payload):
```java
public class UserSummary {
    private Long id;
    private String name;
}
```

**PageResponse** (generic paginated wrapper):
```java
public class PageResponse<T> {
    private List<T> data;
    private int page;
    private int size;
    private long count;
    private int pages;
}
```

**UserFilter** (search/filter params):
```java
public class UserFilter {
    private String name;
    // Future filters added here
}
```

---

## Filtering (Specification Pattern)

### UserSpecification

Builds JPA Specifications dynamically from `UserFilter`. Uses instance methods for testability and extensibility.

```java
@Component
public class UserSpecification {

    public Specification<UserJpaEntity> fromFilter(UserFilter filter) {
        Specification<UserJpaEntity> spec = Specification.where(null);

        if (filter.getName() != null) {
            spec = spec.and(hasName(filter.getName()));
        }

        return spec;
    }

    private Specification<UserJpaEntity> hasName(String name) {
        return (root, query, cb) ->
            cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%");
    }
}
```

### Adding New Filters

1. Add field to `UserFilter`
2. Add specification method to `UserSpecification`
3. Wire it in `fromFilter()` with a null check

No new repository methods or service methods needed.

---

## Validation

### Rules

| Field | Rule | Error Message |
|-------|------|---------------|
| `name` (Create) | `@NotBlank` | "Name must not be blank" |
| `name` (Create) | `@Size(max = 255)` | "Name must not exceed 255 characters" |
| `name` (Update) | `@NotBlank` | "Name must not be blank" |
| `name` (Update) | `@Size(max = 255)` | "Name must not exceed 255 characters" |

### Flow

1. `@Valid` on controller method parameter triggers Bean Validation
2. Validation failure throws `MethodArgumentNotValidException`
3. `UserExceptionHandler` catches it and returns 400 with `ErrorResponse`

---

## Error Handling

### ErrorResponse Format

```java
public class ErrorResponse {
    private String error;              // Error code (exception name without "Exception")
    private String message;            // Human-readable description
    private List<FieldError> errors;   // Optional: field-level validation details

    public static class FieldError {
        private String field;
        private String message;
    }
}
```

### Error Code Convention

Error code = Exception class name with `Exception` suffix removed:

| Exception Class | Error Code |
|----------------|------------|
| `UserNotFoundException` | `UserNotFound` |
| `ValidationFailedException` | `ValidationFailed` |
| `UnexpectedException` | `Unexpected` |

### Error Examples

**Validation failure (400):**
```json
{
  "error": "ValidationFailed",
  "message": "Validation failed",
  "errors": [
    { "field": "name", "message": "Name must not be blank" }
  ]
}
```

**User not found (404):**
```json
{
  "error": "UserNotFound",
  "message": "User not found with id: 42"
}
```

**Unexpected error (500):**
```json
{
  "error": "Unexpected",
  "message": "An unexpected error occurred"
}
```

### UserExceptionHandler

```java
@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(...) { ... }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(...) { ... }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(...) { ... }
}
```

---

## Data Flow

### Create User

```
HTTP POST /api/users  { "name": "John" }
        │
        ▼
UserController
  1. Receive CreateUserRequest
  2. Map to User (domain entity)
  3. Call UserService.create(user)
  4. Map result to UserResponse
  5. Return 201 + response
        │
        ▼
UserServiceImpl
  1. Set createdAt, updatedAt
  2. Call repository.save(user)
  3. Return saved user
        │
        ▼
UserRepositoryAdapter
  1. Map User → UserJpaEntity
  2. Call jpaRepository.save()
  3. Map back to User
        │
        ▼
MySQL
```

### Search/List Users

```
GET /api/users?name=John&page=0&size=20
        │
        ▼
UserController
  1. Parse UserFilter from query params
  2. Parse Pageable from query params
  3. Call UserService.search(filter, pageable)
        │
        ▼
UserServiceImpl
  1. Call userRepository.findAll(filter, pageable)
        │
        ▼
UserRepositoryAdapter
  1. Call userSpecification.fromFilter(filter)
  2. Call jpaRepository.findAll(spec, pageable)
  3. Map Page<UserJpaEntity> → Page<User>
        │
        ▼
UserController
  1. Map Page<User> → PageResponse<UserSummary>
  2. Return JSON
```

---

## Data Migration (Flyway)

### Dependencies

```xml
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>
```

### Configuration

```properties
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.sql-migration-prefix=
```

### Migration File Naming

```
<timestamp>__<description>.sql
```

Example: `20260622103000123456__create_user_table.sql`

### V1 Migration

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);
```

---

## Testing Strategy

### Unit Tests

**UserServiceTest** — Mock repository, test business logic:
- `create` should set timestamps and call repository
- `findById` should throw `UserNotFoundException` when not found
- `deleteById` should call repository delete

**UserSpecificationTest** — Test specification building:
- `fromFilter` with name should return matching spec
- `fromFilter` with null filters should return empty spec

### API Tests

Full end-to-end HTTP tests using `@SpringBootTest` + `TestRestTemplate`:

- `POST /api/users` — should return 201
- `POST /api/users` with blank name — should return 400
- `GET /api/users/{id}` — should return 200
- `GET /api/users/{id}` when not found — should return 404
- `GET /api/users` — should return paginated response
- `GET /api/users?name=John` — should filter results
- `PUT /api/users/{id}` — should return 200
- `DELETE /api/users/{id}` — should return 204

### Test Configuration

```properties
# application-test.properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
```

---

## OpenAPI Documentation

### Dependency

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.6</version>
</dependency>
```

### Access Points

| URL | Description |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Interactive Swagger UI |
| `http://localhost:8080/v3/api-docs` | Raw OpenAPI JSON |

### Annotations

- `@Tag` on controller for module grouping
- `@Operation` on each endpoint for summary/description
- `@ApiResponses` for response codes and error formats
- `@Schema` on DTOs for field descriptions and examples
- `@ParameterObject` for filter and pageable params

---

## Files to Create

| File | Package |
|------|---------|
| `User.java` | `nst.wms.user.domain` |
| `UserNotFoundException.java` | `nst.wms.user.domain` |
| `UserService.java` | `nst.wms.user.application` |
| `UserServiceImpl.java` | `nst.wms.user.application` |
| `UserRepository.java` | `nst.wms.user.internal.infrastructure` |
| `UserJpaEntity.java` | `nst.wms.user.internal.infrastructure` |
| `UserRepositoryAdapter.java` | `nst.wms.user.internal.infrastructure` |
| `UserJpaRepository.java` | `nst.wms.user.internal.infrastructure` |
| `UserSpecification.java` | `nst.wms.user.internal.infrastructure` |
| `UserController.java` | `nst.wms.user.internal.presentation` |
| `UserExceptionHandler.java` | `nst.wms.user.internal.presentation` |
| `CreateUserRequest.java` | `nst.wms.user.internal.presentation.dto` |
| `UpdateUserRequest.java` | `nst.wms.user.internal.presentation.dto` |
| `UserResponse.java` | `nst.wms.user.internal.presentation.dto` |
| `UserSummary.java` | `nst.wms.user.internal.presentation.dto` |
| `PageResponse.java` | `nst.wms.user.internal.presentation.dto` |
| `UserFilter.java` | `nst.wms.user.internal.presentation.dto` |
| `ErrorResponse.java` | `nst.wms.common.api` |

### Files to Modify

| File | Change |
|------|--------|
| `pom.xml` | Add SpringDoc, Flyway dependencies |
| `application.properties` | Add MySQL config, JPA settings, Flyway config |

### Test Files to Create

| File | Package |
|------|---------|
| `UserServiceTest.java` | `nst.wms.user.application` |
| `UserSpecificationTest.java` | `nst.wms.user.internal.infrastructure` |
| `UserApiTest.java` | `nst.wms.user.internal.presentation` |
| `application-test.properties` | `src/test/resources` |

### Migration Files to Create

| File | Location |
|------|----------|
| `<timestamp>__create_user_table.sql` | `src/main/resources/db/migration/` |
