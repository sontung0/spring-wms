# Spring Modulith Module Exposure — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Eliminate `MODULITH_TYPE_REF_VIOLATION` by removing the `internal/` wrapper layer and annotating module boundaries with `@NamedInterface` / `@ApplicationModule(type = OPEN)` per the approved design spec.

**Architecture:** The `user` module currently has an `internal/` wrapper that is redundant—Spring Modulith treats all packages as private by default. We remove `internal/`, moving `presentation/` and `infrastructure/` up to `user/`, and use `@NamedInterface` annotations on `domain/` and `application/` to selectively expose them. The `common` module gets `@ApplicationModule(type = OPEN)` so its types are accessible across all modules.

**Tech Stack:** Java 26, Spring Boot 4.1.0, Spring Modulith 2.1.0, Maven

**Key constraint:** All source files referencing the old `user.internal.*` packages must have their imports updated in lockstep. Tests must be updated and pass before committing.

---

### File Structure Change Summary

**Before:**
```
src/main/java/nst/wms/user/
├── domain/         (User.java, UserNotFoundException.java)
├── application/    (UserService.java, UserServiceImpl.java)
└── internal/
    ├── infrastructure/   (5 files)
    └── presentation/
        └── dto/          (6 files)
```

**After:**
```
src/main/java/nst/wms/user/
├── domain/              ← @NamedInterface
│   ├── package-info.java    ← NEW
│   ├── User.java
│   └── UserNotFoundException.java
├── application/         ← @NamedInterface
│   ├── package-info.java    ← NEW
│   ├── UserService.java
│   └── UserServiceImpl.java
├── infrastructure/     ← private
│   ├── UserRepository.java
│   ├── UserJpaEntity.java
│   ├── UserRepositoryAdapter.java
│   ├── UserJpaRepository.java
│   └── UserSpecification.java
└── presentation/       ← private
    ├── UserController.java
    ├── UserExceptionHandler.java
    └── dto/
        ├── CreateUserRequest.java
        ├── UpdateUserRequest.java
        ├── UserResponse.java
        ├── UserSummary.java
        ├── PageResponse.java
        └── UserFilter.java

src/main/java/nst/wms/common/
├── package-info.java       ← NEW: @ApplicationModule(type = OPEN)
└── api/
    └── ErrorResponse.java
```

---

### Task 1: Create `common/package-info.java` — OPEN module

**Files:**
- Create: `src/main/java/nst/wms/common/package-info.java`

- [ ] **Step 1: Create the file**

```java
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package nst.wms.common;
```

- [ ] **Step 2: Compile to verify**

```bash
./mvnw compile -q
```
Expected: No errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/nst/wms/common/package-info.java
git commit -m "feat: mark common module as OPEN for cross-module access"
```

---

### Task 2: Create `user/domain/package-info.java` — @NamedInterface

**Files:**
- Create: `src/main/java/nst/wms/user/domain/package-info.java`

- [ ] **Step 1: Create the file**

```java
@org.springframework.modulith.NamedInterface
package nst.wms.user.domain;
```

- [ ] **Step 2: Compile to verify**

```bash
./mvnw compile -q
```
Expected: No errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/nst/wms/user/domain/package-info.java
git commit -m "feat: expose user.domain as named interface"
```

---

### Task 3: Create `user/application/package-info.java` — @NamedInterface

**Files:**
- Create: `src/main/java/nst/wms/user/application/package-info.java`

- [ ] **Step 1: Create the file**

```java
@org.springframework.modulith.NamedInterface
package nst.wms.user.application;
```

- [ ] **Step 2: Compile to verify**

```bash
./mvnw compile -q
```
Expected: No errors.

- [ ] **Step 3: Commit**

```bash
git add src/main/java/nst/wms/user/application/package-info.java
git commit -m "feat: expose user.application as named interface"
```

---

### Task 4: Move infrastructure sources out of `internal/`

**Files:**
- Create (with updated content): `src/main/java/nst/wms/user/infrastructure/UserRepository.java`
- Create: `src/main/java/nst/wms/user/infrastructure/UserJpaEntity.java`
- Create: `src/main/java/nst/wms/user/infrastructure/UserRepositoryAdapter.java`
- Create: `src/main/java/nst/wms/user/infrastructure/UserJpaRepository.java`
- Create: `src/main/java/nst/wms/user/infrastructure/UserSpecification.java`
- Delete: `src/main/java/nst/wms/user/internal/infrastructure/`

Each file changes package from `nst.wms.user.internal.infrastructure` to `nst.wms.user.infrastructure`. Files referencing `UserFilter` also update the import from `nst.wms.user.internal.presentation.dto.UserFilter` to `nst.wms.user.presentation.dto.UserFilter`.

<details>
<summary>Updated file contents (click to expand)</summary>

**UserRepository.java** — package + import change:
```java
package nst.wms.user.infrastructure;

import nst.wms.user.domain.User;
import nst.wms.user.presentation.dto.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserRepository {
    User save(User user);
    java.util.Optional<User> findById(Long id);
    Page<User> search(UserFilter filter, Pageable pageable);
    void deleteById(Long id);
    boolean existsById(Long id);
}
```

**UserJpaEntity.java** — package change only:
```java
package nst.wms.user.infrastructure;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    public UserJpaEntity() {
    }

    public UserJpaEntity(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

**UserRepositoryAdapter.java** — package + import change:
```java
package nst.wms.user.infrastructure;

import nst.wms.user.domain.User;
import nst.wms.user.presentation.dto.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserRepositoryAdapter implements UserRepository {

    private final UserJpaRepository jpaRepository;
    private final UserSpecification userSpecification;

    public UserRepositoryAdapter(UserJpaRepository jpaRepository, UserSpecification userSpecification) {
        this.jpaRepository = jpaRepository;
        this.userSpecification = userSpecification;
    }

    @Override
    public User save(User user) {
        UserJpaEntity entity = toJpaEntity(user);
        UserJpaEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public java.util.Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Page<User> search(UserFilter filter, Pageable pageable) {
        Specification<UserJpaEntity> spec = userSpecification.fromFilter(filter);
        return jpaRepository.findAll(spec, pageable).map(this::toDomain);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsById(Long id) {
        return jpaRepository.existsById(id);
    }

    private UserJpaEntity toJpaEntity(User user) {
        UserJpaEntity entity = new UserJpaEntity();
        entity.setId(user.getId());
        entity.setName(user.getName());
        entity.setCreatedAt(user.getCreatedAt());
        entity.setUpdatedAt(user.getUpdatedAt());
        return entity;
    }

    private User toDomain(UserJpaEntity entity) {
        return new User(entity.getId(), entity.getName(), entity.getCreatedAt(), entity.getUpdatedAt());
    }
}
```

**UserJpaRepository.java** — package change only:
```java
package nst.wms.user.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long>, JpaSpecificationExecutor<UserJpaEntity> {
}
```

**UserSpecification.java** — package + import change:
```java
package nst.wms.user.infrastructure;

import nst.wms.user.presentation.dto.UserFilter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

@Component
public class UserSpecification {

    public Specification<UserJpaEntity> fromFilter(UserFilter filter) {
        Specification<UserJpaEntity> spec = (root, query, cb) -> null;

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
</details>

- [ ] **Step 1: Create new directory and files**

Create the target files at `src/main/java/nst/wms/user/infrastructure/` with the updated content shown above.

- [ ] **Step 2: Delete old directory**

```bash
rm -rf src/main/java/nst/wms/user/internal/infrastructure
```

- [ ] **Step 3: Compile to verify**

```bash
./mvnw compile -q 2>&1
```
Expected: No errors (imports from `user.presentation.dto` won't resolve yet — that's fine, Task 5 fixes them).

- [ ] **Step 4: Commit**

```bash
git add src/main/java/nst/wms/user/infrastructure/
git add -A src/main/java/nst/wms/user/internal/infrastructure/
git commit -m "refactor: move infrastructure out of internal/ wrapper"
```

> **Note:** Step 3 may fail because `UserRepositoryAdapter` and `UserSpecification` import `UserFilter` from the old `internal.presentation.dto` path. The imports in the new files already reference `user.presentation.dto` (which doesn't exist until Task 5). If compilation fails, ignore it — Tasks 5-7 resolve all imports together. We can also create the presentation files first, but the tasks are small enough that the intermediate failure is fine.

---

### Task 5: Move presentation sources out of `internal/`

**Files:**
- Create (with updated package): `src/main/java/nst/wms/user/presentation/UserController.java`
- Create: `src/main/java/nst/wms/user/presentation/UserExceptionHandler.java`
- Create: `src/main/java/nst/wms/user/presentation/dto/CreateUserRequest.java`
- Create: `src/main/java/nst/wms/user/presentation/dto/UpdateUserRequest.java`
- Create: `src/main/java/nst/wms/user/presentation/dto/UserResponse.java`
- Create: `src/main/java/nst/wms/user/presentation/dto/UserSummary.java`
- Create: `src/main/java/nst/wms/user/presentation/dto/PageResponse.java`
- Create: `src/main/java/nst/wms/user/presentation/dto/UserFilter.java`
- Delete: `src/main/java/nst/wms/user/internal/presentation/`

Each file changes package from `nst.wms.user.internal.presentation` / `nst.wms.user.internal.presentation.dto` to `nst.wms.user.presentation` / `nst.wms.user.presentation.dto`. No import changes needed within the presentation layer (they only import from `common.api`, `user.domain`, `user.application` — none of which changed).

<details>
<summary>Updated file contents (click to expand)</summary>

**UserController.java** — package change only (`internal.presentation` → `presentation`):
```java
package nst.wms.user.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import nst.wms.common.api.ErrorResponse;
import nst.wms.user.application.UserService;
import nst.wms.user.domain.User;
import nst.wms.user.presentation.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management operations")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @Operation(summary = "Create a new user", description = "Creates a new user with the provided name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User created successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        User user = new User();
        user.setName(request.getName());

        User created = userService.create(user);

        UserResponse response = new UserResponse(
                created.getId(),
                created.getName(),
                created.getCreatedAt(),
                created.getUpdatedAt()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List/search users", description = "Returns paginated list of users with optional name filter")
    public ResponseEntity<PageResponse<UserSummary>> search(
            UserFilter filter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String[] sort) {

        Sort sortObj = Sort.by(sort[0]);
        if (sort.length > 1 && sort[1].equalsIgnoreCase("desc")) {
            sortObj = sortObj.descending();
        } else {
            sortObj = sortObj.ascending();
        }

        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<User> userPage = userService.search(filter, pageable);

        List<UserSummary> summaries = userPage.getContent().stream()
                .map(u -> new UserSummary(u.getId(), u.getName()))
                .collect(Collectors.toList());

        PageResponse<UserSummary> response = new PageResponse<>(
                summaries,
                userPage.getNumber(),
                userPage.getSize(),
                userPage.getTotalElements(),
                userPage.getTotalPages()
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get user by ID", description = "Returns a single user by their ID")
    public ResponseEntity<UserResponse> getById(@PathVariable Long id) {
        User user = userService.findById(id);

        UserResponse response = new UserResponse(
                user.getId(),
                user.getName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a user", description = "Updates the user's name")
    public ResponseEntity<UserResponse> update(@PathVariable Long id, @Valid @RequestBody UpdateUserRequest request) {
        User user = new User();
        user.setName(request.getName());

        User updated = userService.update(id, user);

        UserResponse response = new UserResponse(
                updated.getId(),
                updated.getName(),
                updated.getCreatedAt(),
                updated.getUpdatedAt()
        );

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a user", description = "Deletes a user by their ID")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

**UserExceptionHandler.java** — package change only:
```java
package nst.wms.user.presentation;

import nst.wms.common.api.ErrorResponse;
import nst.wms.user.domain.UserNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class UserExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());

        ErrorResponse response = new ErrorResponse("ValidationFailed", "Validation failed", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(UserNotFoundException ex) {
        ErrorResponse response = new ErrorResponse("UserNotFound", ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse response = new ErrorResponse("Unexpected", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

**DTO files** — package change only (`user.internal.presentation.dto` → `user.presentation.dto`):

*CreateUserRequest.java:*
```java
package nst.wms.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a user")
public class CreateUserRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "User name", example = "John Doe", maxLength = 255)
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

*UpdateUserRequest.java:*
```java
package nst.wms.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating a user")
public class UpdateUserRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "User name", example = "John Doe", maxLength = 255)
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

*UserResponse.java:*
```java
package nst.wms.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDateTime;

@Schema(description = "User response with full details")
public class UserResponse {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;

    public UserResponse() {}

    public UserResponse(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

*UserSummary.java:*
```java
package nst.wms.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lightweight user summary for list responses")
public class UserSummary {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    public UserSummary() {}

    public UserSummary(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```

*PageResponse.java:*
```java
package nst.wms.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Generic paginated response wrapper")
public class PageResponse<T> {

    @Schema(description = "List of items")
    private List<T> data;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int page;

    @Schema(description = "Items per page", example = "20")
    private int size;

    @Schema(description = "Total number of items", example = "100")
    private long count;

    @Schema(description = "Total number of pages", example = "5")
    private int pages;

    public PageResponse() {}

    public PageResponse(List<T> data, int page, int size, long count, int pages) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.count = count;
        this.pages = pages;
    }

    public List<T> getData() { return data; }
    public void setData(List<T> data) { this.data = data; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = size; }
    public long getCount() { return count; }
    public void setCount(long count) { this.count = count; }
    public int getPages() { return pages; }
    public void setPages(int pages) { this.pages = pages; }
}
```

*UserFilter.java:*
```java
package nst.wms.user.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Search and filter parameters for users")
public class UserFilter {

    @Schema(description = "Filter by name (case-insensitive, partial match)", example = "John")
    private String name;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
```
</details>

- [ ] **Step 1: Create new directories and files**

Create the target files at `src/main/java/nst/wms/user/presentation/` and `src/main/java/nst/wms/user/presentation/dto/` with the updated content.

- [ ] **Step 2: Delete old directory**

```bash
rm -rf src/main/java/nst/wms/user/internal/presentation
rm -rf src/main/java/nst/wms/user/internal  # remove empty internal/ if left
```

- [ ] **Step 3: Compile to verify**

```bash
./mvnw compile -q 2>&1
```
Expected: May still fail due to `UserService.java` and `UserServiceImpl.java` referencing old `internal` packages (fixed in Task 6).

- [ ] **Step 4: Commit**

```bash
git add src/main/java/nst/wms/user/presentation/
git add -A src/main/java/nst/wms/user/internal/
git commit -m "refactor: move presentation out of internal/ wrapper"
```

---

### Task 6: Update imports in `user.application` layer

**Files:**
- Modify: `src/main/java/nst/wms/user/application/UserService.java`
- Modify: `src/main/java/nst/wms/user/application/UserServiceImpl.java`

- [ ] **Step 1: Update `UserService.java` import**

Change `import nst.wms.user.internal.presentation.dto.UserFilter;` to:
```java
import nst.wms.user.presentation.dto.UserFilter;
```

- [ ] **Step 2: Update `UserServiceImpl.java` imports**

Change `import nst.wms.user.internal.infrastructure.UserRepository;` to:
```java
import nst.wms.user.infrastructure.UserRepository;
```

Change `import nst.wms.user.internal.presentation.dto.UserFilter;` to:
```java
import nst.wms.user.presentation.dto.UserFilter;
```

- [ ] **Step 3: Compile to verify**

```bash
./mvnw compile -q 2>&1
```
Expected: **BUILD SUCCESS** — no errors.

- [ ] **Step 4: Commit**

```bash
git add src/main/java/nst/wms/user/application/UserService.java src/main/java/nst/wms/user/application/UserServiceImpl.java
git commit -m "refactor: update imports to match new package structure"
```

---

### Task 7: Update test files to match new package structure

**Files:**
- Create (with updated content): `src/test/java/nst/wms/user/infrastructure/UserSpecificationTest.java`
- Delete: `src/test/java/nst/wms/user/internal/infrastructure/UserSpecificationTest.java`
- Create (with updated content): `src/test/java/nst/wms/user/presentation/UserApiTest.java`
- Delete: `src/test/java/nst/wms/user/internal/presentation/UserApiTest.java`
- Delete (if empty): `src/test/java/nst/wms/user/internal/`
- Modify: `src/test/java/nst/wms/user/application/UserServiceTest.java`

- [ ] **Step 1: Move `UserSpecificationTest.java`**

Create `src/test/java/nst/wms/user/infrastructure/UserSpecificationTest.java`:
```java
package nst.wms.user.infrastructure;

import nst.wms.user.presentation.dto.UserFilter;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.domain.Specification;

import static org.junit.jupiter.api.Assertions.*;

class UserSpecificationTest {

    private final UserSpecification userSpecification = new UserSpecification();

    @Test
    void fromFilter_withName_shouldReturnMatchingSpec() {
        UserFilter filter = new UserFilter();
        filter.setName("John");

        Specification<UserJpaEntity> spec = userSpecification.fromFilter(filter);

        assertNotNull(spec);
    }

    @Test
    void fromFilter_withNullFilters_shouldReturnEmptySpec() {
        UserFilter filter = new UserFilter();

        Specification<UserJpaEntity> spec = userSpecification.fromFilter(filter);

        assertNotNull(spec);
    }
}
```

Then delete the old file:
```bash
rm src/test/java/nst/wms/user/internal/infrastructure/UserSpecificationTest.java
```

- [ ] **Step 2: Move `UserApiTest.java`**

Create `src/test/java/nst/wms/user/presentation/UserApiTest.java`:
```java
package nst.wms.user.presentation;

import nst.wms.user.presentation.dto.CreateUserRequest;
import nst.wms.user.presentation.dto.UpdateUserRequest;
import nst.wms.user.presentation.dto.UserResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import tools.jackson.databind.ObjectMapper;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void createUser_shouldReturn201() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("John");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("John"))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void createUser_withBlankName_shouldReturn400() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("");

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("ValidationFailed"));
    }

    @Test
    void getUserById_shouldReturn200() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("Jane");
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UserResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), UserResponse.class);

        mockMvc.perform(get("/api/users/" + created.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Jane"));
    }

    @Test
    void getUserById_whenNotFound_shouldReturn404() throws Exception {
        mockMvc.perform(get("/api/users/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("UserNotFound"));
    }

    @Test
    void listUsers_shouldReturnPaginatedResponse() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("TestUser");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users").param("page", "0").param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(greaterThan(0)));
    }

    @Test
    void listUsers_withNameFilter_shouldFilterResults() throws Exception {
        CreateUserRequest req1 = new CreateUserRequest();
        req1.setName("Alice");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req1)))
                .andExpect(status().isCreated());

        CreateUserRequest req2 = new CreateUserRequest();
        req2.setName("Bob");
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req2)))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/users").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[?(@.name =~ /.*Alice.*/)]").exists())
                .andExpect(jsonPath("$.data[?(@.name =~ /.*Bob.*/)]").doesNotExist());
    }

    @Test
    void updateUser_shouldReturn200() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("OldName");
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UserResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), UserResponse.class);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("NewName");

        mockMvc.perform(put("/api/users/" + created.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("NewName"));
    }

    @Test
    void deleteUser_shouldReturn204() throws Exception {
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("ToDelete");
        MvcResult createResult = mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        UserResponse created = objectMapper.readValue(
                createResult.getResponse().getContentAsString(), UserResponse.class);

        mockMvc.perform(delete("/api/users/" + created.getId()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/" + created.getId()))
                .andExpect(status().isNotFound());
    }
}
```

Delete the old file:
```bash
rm src/test/java/nst/wms/user/internal/presentation/UserApiTest.java
rmdir src/test/java/nst/wms/user/internal/presentation/dto 2>/dev/null
rmdir src/test/java/nst/wms/user/internal/presentation 2>/dev/null
rmdir src/test/java/nst/wms/user/internal/infrastructure 2>/dev/null
rmdir src/test/java/nst/wms/user/internal 2>/dev/null
```

- [ ] **Step 3: Update `UserServiceTest.java` import**

In `src/test/java/nst/wms/user/application/UserServiceTest.java`:

Change:
```java
import nst.wms.user.internal.infrastructure.UserRepository;
```
To:
```java
import nst.wms.user.infrastructure.UserRepository;
```

- [ ] **Step 4: Run tests to verify**

```bash
./mvnw test -q 2>&1
```
Expected: All tests pass (5 in UserServiceTest, 2 in UserSpecificationTest, 7 in UserApiTest, 1 in WmsApplicationTests = 15 total).

- [ ] **Step 5: Commit**

```bash
git add src/test/java/nst/wms/user/infrastructure/
git add src/test/java/nst/wms/user/presentation/
git add src/test/java/nst/wms/user/application/UserServiceTest.java
git add -A src/test/java/nst/wms/user/internal/
git commit -m "refactor: update test packages and imports to match new structure"
```

---

### Task 8: Add Modulith test dependency to `pom.xml`

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add `spring-modulith-starter-test` dependency**

Insert after the `spring-modulith-starter-core` dependency block (around line where `spring-modulith-starter-core` is defined):

```xml
        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
```

The `spring-modulith-bom` already manages the version (2.1.0), so no `<version>` tag is needed.

- [ ] **Step 2: Validate the POM**

```bash
./mvnw validate -q 2>&1
```
Expected: No errors.

- [ ] **Step 3: Commit**

```bash
git add pom.xml
git commit -m "build: add spring-modulith-starter-test dependency"
```

---

### Task 9: Create module verification test

**Files:**
- Create: `src/test/java/nst/wms/ModulithVerificationTest.java`

- [ ] **Step 1: Create the test file**

```java
package nst.wms;

import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModulithVerificationTest {

    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(WmsApplication.class).verify();
    }
}
```

- [ ] **Step 2: Run the specific test**

```bash
./mvnw test -pl . -Dtest=nst.wms.ModulithVerificationTest -q 2>&1
```
Expected: PASS (or the test compiles and runs; if there are violations the test will fail).

- [ ] **Step 3: Commit**

```bash
git add src/test/java/nst/wms/ModulithVerificationTest.java
git commit -m "test: add ModulithVerificationTest to catch boundary violations"
```

---

### Task 10: Full test suite verification

- [ ] **Step 1: Run full test suite**

```bash
./mvnw test 2>&1 | tail -30
```
Expected: All 16 tests pass (15 existing + 1 ModulithVerificationTest). The `ModulithVerificationTest.verifyModuleStructure()` must pass with no `MODULITH_TYPE_REF_VIOLATION`.

- [ ] **Step 2: Verify application starts**

```bash
./mvnw spring-boot:run -q 2>&1 &
sleep 8
curl -s http://localhost:8080/api/users | head -5
kill %1 2>/dev/null
```
Expected: Application starts and API returns a valid response (no startup failures).

- [ ] **Step 3: Final commit if any changes were made during verification**

```bash
git add -A
git commit -m "chore: final adjustments after full verification"
```

---

## Self-Review Checklist

**Spec coverage:**
- ✅ Remove `internal/` wrapper layer (Tasks 4-5)
- ✅ Create `common/package-info.java` with `@ApplicationModule(type = OPEN)` (Task 1)
- ✅ Create `user/domain/package-info.java` with `@NamedInterface` (Task 2)
- ✅ Create `user/application/package-info.java` with `@NamedInterface` (Task 3)
- ✅ Add Modulith test dependency (Task 8)
- ✅ Create `ModulithVerificationTest` (Task 9)
- ✅ All existing tests still pass (Task 10)

**Placeholder scan:** No TODOs, TBDs, or filler. Every step has complete code.

**Type consistency:** All package references match across tasks — `user.presentation.dto.UserFilter`, `user.infrastructure.UserRepository`, etc.
