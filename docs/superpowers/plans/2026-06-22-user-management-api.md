# User Management API Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a complete User Management REST API with CRUD, search/filter, pagination, validation, error handling, OpenAPI docs, and Flyway migrations.

**Architecture:** Clean Architecture with module-first organization. The `user` module contains domain, application, and internal (infrastructure + presentation) layers. Spring Modulith enforces module boundaries. Domain and application layers are public; infrastructure and presentation are private.

**Tech Stack:** Java 26, Spring Boot 4.1.0, Spring Data JPA, MySQL, Spring Modulith, SpringDoc OpenAPI, Flyway, H2 (test), Bean Validation

---

## File Structure

### Files to Create

| File | Package | Responsibility |
|------|---------|----------------|
| `src/main/java/nst/wms/user/domain/User.java` | `nst.wms.user.domain` | Domain entity |
| `src/main/java/nst/wms/user/domain/UserNotFoundException.java` | `nst.wms.user.domain` | Domain exception |
| `src/main/java/nst/wms/user/application/UserService.java` | `nst.wms.user.application` | Input port interface |
| `src/main/java/nst/wms/user/application/UserServiceImpl.java` | `nst.wms.user.application` | Use case implementation |
| `src/main/java/nst/wms/user/internal/infrastructure/UserRepository.java` | `nst.wms.user.internal.infrastructure` | Output port interface |
| `src/main/java/nst/wms/user/internal/infrastructure/UserJpaEntity.java` | `nst.wms.user.internal.infrastructure` | JPA persistence model |
| `src/main/java/nst/wms/user/internal/infrastructure/UserRepositoryAdapter.java` | `nst.wms.user.internal.infrastructure` | Implements UserRepository |
| `src/main/java/nst/wms/user/internal/infrastructure/UserJpaRepository.java` | `nst.wms.user.internal.infrastructure` | Spring Data JPA interface |
| `src/main/java/nst/wms/user/internal/infrastructure/UserSpecification.java` | `nst.wms.user.internal.infrastructure` | Builds JPA Specifications from filters |
| `src/main/java/nst/wms/user/internal/presentation/UserController.java` | `nst.wms.user.internal.presentation` | REST endpoints |
| `src/main/java/nst/wms/user/internal/presentation/UserExceptionHandler.java` | `nst.wms.user.internal.presentation` | Maps domain exceptions → HTTP |
| `src/main/java/nst/wms/user/internal/presentation/dto/CreateUserRequest.java` | `nst.wms.user.internal.presentation.dto` | POST request body |
| `src/main/java/nst/wms/user/internal/presentation/dto/UpdateUserRequest.java` | `nst.wms.user.internal.presentation.dto` | PUT request body |
| `src/main/java/nst/wms/user/internal/presentation/dto/UserResponse.java` | `nst.wms.user.internal.presentation.dto` | Single user response |
| `src/main/java/nst/wms/user/internal/presentation/dto/UserSummary.java` | `nst.wms.user.internal.presentation.dto` | Lightweight list item |
| `src/main/java/nst/wms/user/internal/presentation/dto/PageResponse.java` | `nst.wms.user.internal.presentation.dto` | Generic paginated wrapper |
| `src/main/java/nst/wms/user/internal/presentation/dto/UserFilter.java` | `nst.wms.user.internal.presentation.dto` | Search/filter params |
| `src/main/java/nst/wms/common/api/ErrorResponse.java` | `nst.wms.common.api` | Structured error response |
| `src/main/resources/db/migration/V1__create_user_table.sql` | — | Flyway migration |

### Files to Modify

| File | Change |
|------|--------|
| `pom.xml` | Add SpringDoc, Flyway, H2, Validation dependencies |
| `src/main/resources/application.properties` | Add MySQL config, JPA settings, Flyway config |

### Test Files to Create

| File | Package |
|------|---------|
| `src/test/java/nst/wms/user/application/UserServiceTest.java` | `nst.wms.user.application` |
| `src/test/java/nst/wms/user/internal/infrastructure/UserSpecificationTest.java` | `nst.wms.user.internal.infrastructure` |
| `src/test/java/nst/wms/user/internal/presentation/UserApiTest.java` | `nst.wms.user.internal.presentation` |
| `src/test/resources/application-test.properties` | — |

---

## Task 1: Project Dependencies & Configuration

**Files:**
- Modify: `pom.xml`
- Modify: `src/main/resources/application.properties`
- Create: `src/test/resources/application-test.properties`

- [ ] **Step 1: Add dependencies to pom.xml**

Add the following dependencies inside the `<dependencies>` block:

```xml
<!-- Validation -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-validation</artifactId>
</dependency>

<!-- Flyway -->
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-core</artifactId>
</dependency>
<dependency>
    <groupId>org.flywaydb</groupId>
    <artifactId>flyway-mysql</artifactId>
</dependency>

<!-- SpringDoc OpenAPI -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.8.6</version>
</dependency>

<!-- H2 for tests -->
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **Step 2: Update application.properties**

Replace the contents of `src/main/resources/application.properties` with:

```properties
spring.application.name=wms

# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/wms
spring.datasource.username=root
spring.datasource.password=
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# JPA
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.open-in-view=false

# Flyway
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.sql-migration-prefix=
```

- [ ] **Step 3: Create test configuration**

Create `src/test/resources/application-test.properties`:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.jpa.hibernate.ddl-auto=create-drop
spring.flyway.enabled=false
```

- [ ] **Step 4: Verify project compiles**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add pom.xml src/main/resources/application.properties src/test/resources/application-test.properties
git commit -m "chore: add dependencies and configuration for user management API"
```

---

## Task 2: Domain Layer — User Entity & Exception

**Files:**
- Create: `src/main/java/nst/wms/user/domain/User.java`
- Create: `src/main/java/nst/wms/user/domain/UserNotFoundException.java`

- [ ] **Step 1: Create User entity**

Create `src/main/java/nst/wms/user/domain/User.java`:

```java
package nst.wms.user.domain;

import java.time.LocalDateTime;

public class User {

    private Long id;
    private String name;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public User() {
    }

    public User(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

- [ ] **Step 2: Create UserNotFoundException**

Create `src/main/java/nst/wms/user/domain/UserNotFoundException.java`:

```java
package nst.wms.user.domain;

public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(Long id) {
        super("User not found with id: " + id);
    }
}
```

- [ ] **Step 3: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add src/main/java/nst/wms/user/domain/
git commit -m "feat(user): add User domain entity and UserNotFoundException"
```

---

## Task 3: Application Layer — UserService Interface & Implementation

**Files:**
- Create: `src/main/java/nst/wms/user/application/UserService.java`
- Create: `src/main/java/nst/wms/user/application/UserServiceImpl.java`
- Create: `src/main/java/nst/wms/user/internal/infrastructure/UserRepository.java`
- Create: `src/test/java/nst/wms/user/application/UserServiceTest.java`

- [ ] **Step 1: Write the failing test for UserService**

Create `src/test/java/nst/wms/user/application/UserServiceTest.java`:

```java
package nst.wms.user.application;

import nst.wms.user.domain.User;
import nst.wms.user.domain.UserNotFoundException;
import nst.wms.user.internal.infrastructure.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void create_shouldSetTimestampsAndCallRepository() {
        User user = new User();
        user.setName("John");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User saved = invocation.getArgument(0);
            saved.setId(1L);
            return saved;
        });

        User result = userService.create(user);

        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());
        assertEquals("John", result.getName());
        verify(userRepository).save(user);
    }

    @Test
    void findById_shouldReturnUserWhenFound() {
        User user = new User(1L, "John", LocalDateTime.now(), LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        User result = userService.findById(1L);

        assertEquals("John", result.getName());
    }

    @Test
    void findById_shouldThrowWhenNotFound() {
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () -> userService.findById(99L));
    }

    @Test
    void deleteById_shouldCallRepositoryDelete() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteById(1L);

        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteById_shouldThrowWhenNotFound() {
        when(userRepository.existsById(99L)).thenReturn(false);

        assertThrows(UserNotFoundException.class, () -> userService.deleteById(99L));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -pl . -Dtest=nst.wms.user.application.UserServiceTest -q`
Expected: FAIL — compilation error (classes don't exist yet)

- [ ] **Step 3: Create UserRepository interface**

Create `src/main/java/nst/wms/user/internal/infrastructure/UserRepository.java`:

```java
package nst.wms.user.internal.infrastructure;

import nst.wms.user.domain.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

public interface UserRepository {
    User save(User user);
    java.util.Optional<User> findById(Long id);
    Page<User> findAll(Specification<UserJpaEntity> spec, Pageable pageable);
    void deleteById(Long id);
    boolean existsById(Long id);
}
```

- [ ] **Step 4: Create UserService interface**

Create `src/main/java/nst/wms/user/application/UserService.java`:

```java
package nst.wms.user.application;

import nst.wms.user.domain.User;
import nst.wms.user.internal.presentation.dto.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    User create(User user);
    User findById(Long id);
    Page<User> search(UserFilter filter, Pageable pageable);
    User update(Long id, User user);
    void deleteById(Long id);
}
```

- [ ] **Step 5: Create UserServiceImpl**

Create `src/main/java/nst/wms/user/application/UserServiceImpl.java`:

```java
package nst.wms.user.application;

import nst.wms.user.domain.User;
import nst.wms.user.domain.UserNotFoundException;
import nst.wms.user.internal.infrastructure.UserRepository;
import nst.wms.user.internal.presentation.dto.UserFilter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User create(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        return userRepository.save(user);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    @Override
    public Page<User> search(UserFilter filter, Pageable pageable) {
        return userRepository.search(filter, pageable);
    }

    @Override
    public User update(Long id, User user) {
        User existing = findById(id);
        existing.setName(user.getName());
        existing.setUpdatedAt(LocalDateTime.now());
        return userRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        userRepository.deleteById(id);
    }
}
```

- [ ] **Step 6: Update UserRepository interface to match UserServiceImpl**

Replace `src/main/java/nst/wms/user/internal/infrastructure/UserRepository.java` with:

```java
package nst.wms.user.internal.infrastructure;

import nst.wms.user.domain.User;
import nst.wms.user.internal.presentation.dto.UserFilter;
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

- [ ] **Step 7: Run tests to verify they pass**

Run: `./mvnw test -pl . -Dtest=nst.wms.user.application.UserServiceTest -q`
Expected: PASS (all 5 tests)

- [ ] **Step 8: Commit**

```bash
git add src/main/java/nst/wms/user/application/ src/main/java/nst/wms/user/internal/infrastructure/UserRepository.java src/test/java/nst/wms/user/application/UserServiceTest.java
git commit -m "feat(user): add UserService interface, implementation, and unit tests"
```

---

## Task 4: Infrastructure Layer — JPA Entities & Repository

**Files:**
- Create: `src/main/java/nst/wms/user/internal/infrastructure/UserJpaEntity.java`
- Create: `src/main/java/nst/wms/user/internal/infrastructure/UserJpaRepository.java`
- Create: `src/main/java/nst/wms/user/internal/infrastructure/UserRepositoryAdapter.java`
- Create: `src/main/java/nst/wms/user/internal/infrastructure/UserSpecification.java`
- Create: `src/test/java/nst/wms/user/internal/infrastructure/UserSpecificationTest.java`

- [ ] **Step 1: Write the failing test for UserSpecification**

Create `src/test/java/nst/wms/user/internal/infrastructure/UserSpecificationTest.java`:

```java
package nst.wms.user.internal.infrastructure;

import nst.wms.user.internal.presentation.dto.UserFilter;
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

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -pl . -Dtest=nst.wms.user.internal.infrastructure.UserSpecificationTest -q`
Expected: FAIL — compilation error

- [ ] **Step 3: Create UserJpaEntity**

Create `src/main/java/nst/wms/user/internal/infrastructure/UserJpaEntity.java`:

```java
package nst.wms.user.internal.infrastructure;

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

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

- [ ] **Step 4: Create UserJpaRepository**

Create `src/main/java/nst/wms/user/internal/infrastructure/UserJpaRepository.java`:

```java
package nst.wms.user.internal.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long>, JpaSpecificationExecutor<UserJpaEntity> {
}
```

- [ ] **Step 5: Create UserSpecification**

Create `src/main/java/nst/wms/user/internal/infrastructure/UserSpecification.java`:

```java
package nst.wms.user.internal.infrastructure;

import nst.wms.user.internal.presentation.dto.UserFilter;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;

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

- [ ] **Step 6: Create UserRepositoryAdapter**

Create `src/main/java/nst/wms/user/internal/infrastructure/UserRepositoryAdapter.java`:

```java
package nst.wms.user.internal.infrastructure;

import nst.wms.user.domain.User;
import nst.wms.user.internal.presentation.dto.UserFilter;
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

- [ ] **Step 7: Run tests to verify they pass**

Run: `./mvnw test -pl . -Dtest=nst.wms.user.internal.infrastructure.UserSpecificationTest -q`
Expected: PASS (2 tests)

- [ ] **Step 8: Commit**

```bash
git add src/main/java/nst/wms/user/internal/infrastructure/ src/test/java/nst/wms/user/internal/infrastructure/UserSpecificationTest.java
git commit -m "feat(user): add JPA entities, repository adapter, and specification"
```

---

## Task 5: Presentation Layer — DTOs

**Files:**
- Create: `src/main/java/nst/wms/user/internal/presentation/dto/CreateUserRequest.java`
- Create: `src/main/java/nst/wms/user/internal/presentation/dto/UpdateUserRequest.java`
- Create: `src/main/java/nst/wms/user/internal/presentation/dto/UserResponse.java`
- Create: `src/main/java/nst/wms/user/internal/presentation/dto/UserSummary.java`
- Create: `src/main/java/nst/wms/user/internal/presentation/dto/PageResponse.java`
- Create: `src/main/java/nst/wms/user/internal/presentation/dto/UserFilter.java`
- Create: `src/main/java/nst/wms/common/api/ErrorResponse.java`

- [ ] **Step 1: Create CreateUserRequest**

Create `src/main/java/nst/wms/user/internal/presentation/dto/CreateUserRequest.java`:

```java
package nst.wms.user.internal.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for creating a user")
public class CreateUserRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "User name", example = "John Doe", maxLength = 255)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

- [ ] **Step 2: Create UpdateUserRequest**

Create `src/main/java/nst/wms/user/internal/presentation/dto/UpdateUserRequest.java`:

```java
package nst.wms.user.internal.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request body for updating a user")
public class UpdateUserRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 255, message = "Name must not exceed 255 characters")
    @Schema(description = "User name", example = "John Doe", maxLength = 255)
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

- [ ] **Step 3: Create UserResponse**

Create `src/main/java/nst/wms/user/internal/presentation/dto/UserResponse.java`:

```java
package nst.wms.user.internal.presentation.dto;

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

    public UserResponse() {
    }

    public UserResponse(Long id, String name, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
```

- [ ] **Step 4: Create UserSummary**

Create `src/main/java/nst/wms/user/internal/presentation/dto/UserSummary.java`:

```java
package nst.wms.user.internal.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Lightweight user summary for list responses")
public class UserSummary {

    @Schema(description = "User ID", example = "1")
    private Long id;

    @Schema(description = "User name", example = "John Doe")
    private String name;

    public UserSummary() {
    }

    public UserSummary(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

- [ ] **Step 5: Create PageResponse**

Create `src/main/java/nst/wms/user/internal/presentation/dto/PageResponse.java`:

```java
package nst.wms.user.internal.presentation.dto;

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

    public PageResponse() {
    }

    public PageResponse(List<T> data, int page, int size, long count, int pages) {
        this.data = data;
        this.page = page;
        this.size = size;
        this.count = count;
        this.pages = pages;
    }

    public List<T> getData() {
        return data;
    }

    public void setData(List<T> data) {
        this.data = data;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public int getPages() {
        return pages;
    }

    public void setPages(int pages) {
        this.pages = pages;
    }
}
```

- [ ] **Step 6: Create UserFilter (presentation layer)**

Create `src/main/java/nst/wms/user/internal/presentation/dto/UserFilter.java`:

```java
package nst.wms.user.internal.presentation.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Search and filter parameters for users")
public class UserFilter {

    @Schema(description = "Filter by name (case-insensitive, partial match)", example = "John")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
```

- [ ] **Step 7: Create ErrorResponse**

Create `src/main/java/nst/wms/common/api/ErrorResponse.java`:

```java
package nst.wms.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Structured error response")
public class ErrorResponse {

    @Schema(description = "Error code (exception name without 'Exception' suffix)", example = "UserNotFound")
    private String error;

    @Schema(description = "Human-readable error message", example = "User not found with id: 42")
    private String message;

    @Schema(description = "Field-level validation errors (optional)")
    private List<FieldError> errors;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public ErrorResponse(String error, String message, List<FieldError> errors) {
        this.error = error;
        this.message = message;
        this.errors = errors;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldError> errors) {
        this.errors = errors;
    }

    @Schema(description = "Field-level validation error")
    public static class FieldError {

        @Schema(description = "Field name", example = "name")
        private String field;

        @Schema(description = "Error message", example = "Name must not be blank")
        private String message;

        public FieldError() {
        }

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
```

- [ ] **Step 8: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 9: Commit**

```bash
git add src/main/java/nst/wms/user/internal/presentation/dto/ src/main/java/nst/wms/common/api/
git commit -m "feat(user): add DTOs and ErrorResponse for presentation layer"
```

---

## Task 6: Presentation Layer — Controller & Exception Handler

**Files:**
- Create: `src/main/java/nst/wms/user/internal/presentation/UserController.java`
- Create: `src/main/java/nst/wms/user/internal/presentation/UserExceptionHandler.java`

- [ ] **Step 1: Create UserExceptionHandler**

Create `src/main/java/nst/wms/user/internal/presentation/UserExceptionHandler.java`:

```java
package nst.wms.user.internal.presentation;

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

- [ ] **Step 2: Create UserController**

Create `src/main/java/nst/wms/user/internal/presentation/UserController.java`:

```java
package nst.wms.user.internal.presentation;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import nst.wms.common.api.ErrorResponse;
import nst.wms.user.application.UserService;
import nst.wms.user.domain.User;
import nst.wms.user.internal.presentation.dto.*;
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Users retrieved successfully")
    })
    public ResponseEntity<PageResponse<UserSummary>> search(
            @ParameterObject UserFilter filter,
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User updated successfully"),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
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
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "User deleted successfully"),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        userService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
```

- [ ] **Step 3: Add missing import to UserController**

Add the missing `@Valid` import to `UserController.java`:

```java
import jakarta.validation.Valid;
```

- [ ] **Step 4: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 5: Commit**

```bash
git add src/main/java/nst/wms/user/internal/presentation/UserController.java src/main/java/nst/wms/user/internal/presentation/UserExceptionHandler.java
git commit -m "feat(user): add REST controller and exception handler"
```

---

## Task 7: Flyway Migration

**Files:**
- Create: `src/main/resources/db/migration/V1__create_user_table.sql`

- [ ] **Step 1: Create migration file**

Create `src/main/resources/db/migration/V1__create_user_table.sql`:

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    updated_at TIMESTAMP(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
);
```

- [ ] **Step 2: Commit**

```bash
git add src/main/resources/db/migration/V1__create_user_table.sql
git commit -m "feat(user): add Flyway migration for users table"
```

---

## Task 8: API Integration Tests

**Files:**
- Create: `src/test/java/nst/wms/user/internal/presentation/UserApiTest.java`

- [ ] **Step 1: Write the failing API test**

Create `src/test/java/nst/wms/user/internal/presentation/UserApiTest.java`:

```java
package nst.wms.user.internal.presentation;

import nst.wms.user.internal.presentation.dto.CreateUserRequest;
import nst.wms.user.internal.presentation.dto.UpdateUserRequest;
import nst.wms.user.internal.presentation.dto.PageResponse;
import nst.wms.user.internal.presentation.dto.UserResponse;
import nst.wms.user.internal.presentation.dto.UserSummary;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UserApiTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Test
    void createUser_shouldReturn201() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("John");

        ResponseEntity<UserResponse> response = restTemplate.postForEntity(
                "/api/users", request, UserResponse.class);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("John", response.getBody().getName());
        assertNotNull(response.getBody().getId());
        assertNotNull(response.getBody().getCreatedAt());
    }

    @Test
    void createUser_withBlankName_shouldReturn400() {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("");

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/users", request, String.class);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertTrue(response.getBody().contains("ValidationFailed"));
    }

    @Test
    void getUserById_shouldReturn200() {
        // Create a user first
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("Jane");
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                "/api/users", createRequest, UserResponse.class);
        Long id = createResponse.getBody().getId();

        ResponseEntity<UserResponse> response = restTemplate.getForEntity(
                "/api/users/" + id, UserResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Jane", response.getBody().getName());
    }

    @Test
    void getUserById_whenNotFound_shouldReturn404() {
        ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/users/999", String.class);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertTrue(response.getBody().contains("UserNotFound"));
    }

    @Test
    void listUsers_shouldReturnPaginatedResponse() {
        // Create a user first
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("TestUser");
        restTemplate.postForEntity("/api/users", createRequest, UserResponse.class);

        ResponseEntity<PageResponse<UserSummary>> response = restTemplate.exchange(
                "/api/users?page=0&size=10",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertNotNull(response.getBody().getData());
        assertTrue(response.getBody().getData().size() > 0);
    }

    @Test
    void listUsers_withNameFilter_shouldFilterResults() {
        // Create users
        CreateUserRequest req1 = new CreateUserRequest();
        req1.setName("Alice");
        restTemplate.postForEntity("/api/users", req1, UserResponse.class);

        CreateUserRequest req2 = new CreateUserRequest();
        req2.setName("Bob");
        restTemplate.postForEntity("/api/users", req2, UserResponse.class);

        ResponseEntity<PageResponse<UserSummary>> response = restTemplate.exchange(
                "/api/users?name=Alice",
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {});

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().getData().stream()
                .allMatch(u -> u.getName().contains("Alice")));
    }

    @Test
    void updateUser_shouldReturn200() {
        // Create a user first
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("OldName");
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                "/api/users", createRequest, UserResponse.class);
        Long id = createResponse.getBody().getId();

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("NewName");

        HttpEntity<UpdateUserRequest> entity = new HttpEntity<>(updateRequest);
        ResponseEntity<UserResponse> response = restTemplate.exchange(
                "/api/users/" + id, HttpMethod.PUT, entity, UserResponse.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("NewName", response.getBody().getName());
    }

    @Test
    void deleteUser_shouldReturn204() {
        // Create a user first
        CreateUserRequest createRequest = new CreateUserRequest();
        createRequest.setName("ToDelete");
        ResponseEntity<UserResponse> createResponse = restTemplate.postForEntity(
                "/api/users", createRequest, UserResponse.class);
        Long id = createResponse.getBody().getId();

        ResponseEntity<Void> response = restTemplate.exchange(
                "/api/users/" + id, HttpMethod.DELETE, null, Void.class);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        // Verify it's gone
        ResponseEntity<String> getResponse = restTemplate.getForEntity(
                "/api/users/" + id, String.class);
        assertEquals(HttpStatus.NOT_FOUND, getResponse.getStatusCode());
    }
}
```

- [ ] **Step 2: Run tests to verify they fail**

Run: `./mvnw test -pl . -Dtest=nst.wms.user.internal.presentation.UserApiTest -q`
Expected: FAIL — compilation error or test failure (no database)

- [ ] **Step 3: Run all tests to verify they pass**

Run: `./mvnw test -q`
Expected: PASS (all tests)

- [ ] **Step 4: Commit**

```bash
git add src/test/java/nst/wms/user/internal/presentation/UserApiTest.java
git commit -m "test(user): add API integration tests for user management"
```

---

## Task 9: Final Verification

- [ ] **Step 1: Run full test suite**

Run: `./mvnw test`
Expected: All tests pass

- [ ] **Step 2: Verify application starts**

Run: `./mvnw spring-boot:run`
Expected: Application starts without errors (requires MySQL running)

- [ ] **Step 3: Verify Swagger UI**

Open: `http://localhost:8080/swagger-ui.html`
Expected: Swagger UI loads with Users API documented

- [ ] **Step 4: Final commit**

```bash
git add -A
git commit -m "chore: complete user management API implementation"
```
