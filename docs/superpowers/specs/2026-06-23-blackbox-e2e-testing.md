# Black-Box E2E Testing with Testcontainers — Design Spec

## Status

- **2026-06-23:** Approved.

## Overview

A separate `e2e/` Maven submodule that runs fully black-box end-to-end tests against a real MySQL database via Testcontainers. The E2E module has zero compile-time dependency on the app's Java source code — only the compiled JAR artifact, allowing `@SpringBootTest` to boot the application server. Tests communicate exclusively over HTTP using RestAssured.

## Tech Stack

- **Maven Multi-Module** (hybrid — root POM is `packaging=jar` with `<modules>`)
- **JUnit 5** via `spring-boot-starter-test`
- **Testcontainers** (`mysql` module, `junit-jupiter`, `spring-boot-testcontainers`)
- **RestAssured** for fluent HTTP assertions
- **Spring Boot 4.1.0** `@SpringBootTest(webEnvironment = RANDOM_PORT)`
- **`@ServiceConnection`** for auto-wiring Testcontainers to Spring Boot datasource

---

## Maven Multi-Module Structure (Hybrid)

The root POM retains `packaging=jar` and its existing `src/` — no files are moved. A `<modules>` block declares the `e2e/` child module. Maven's reactor builds the root first (producing the plain JAR), then the e2e module compiles against it at `test` scope.

```
wms/
├── pom.xml                  # unchanged packaging=jar, +<modules><module>e2e</module>
├── src/                     # untouched — test resources, Flyway migrations all remain
└── e2e/
    ├── pom.xml              # NEW: depends on root JAR at test scope
    ├── src/test/java/nst/wms/e2e/
    │   ├── config/
    │   │   └── TestContainerConfig.java
    │   ├── AbstractE2eTest.java
    │   └── user/
    │       └── UserE2eTest.java
    └── src/test/resources/
        └── application-e2e.properties
```

---

## Root POM Changes

1. **Add `<modules>` block** at the end, before `</project>`:

```xml
<modules>
    <module>e2e</module>
</modules>
```

2. **Add `<classifier>boot</classifier>`** to the `spring-boot-maven-plugin` — this preserves the plain JAR as the default artifact (consumed by e2e) while the repackaged fat JAR gets a `-boot` suffix:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>repackage</id>
            <goals><goal>repackage</goal></goals>
            <configuration>
                <classifier>boot</classifier>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## e2e/pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd"
         xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>nst</groupId>
        <artifactId>wms</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <artifactId>wms-e2e</artifactId>
    <name>wms-e2e</name>

    <properties>
        <maven.test.skip>true</maven.test.skip>
    </properties>

    <dependencies>
        <!-- App JAR (plain artifact, not boot JAR) — @SpringBootTest classpath -->
        <dependency>
            <groupId>nst</groupId>
            <artifactId>wms</artifactId>
            <version>${project.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Spring Boot test starter (JUnit 5, Mockito, etc.) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Testcontainers integration for Spring Boot (@ServiceConnection) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>mysql</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- RestAssured -->
        <dependency>
            <groupId>io.rest-assured</groupId>
            <artifactId>rest-assured</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <configuration>
                    <skipTests>${maven.test.skip}</skipTests>
                </configuration>
            </plugin>
        </plugins>
    </build>
</project>
```

### Key decisions in e2e/pom.xml

- `<maven.test.skip>true</maven.test.skip>` at the POM level means the e2e module is **skipped by default**. This keeps `./mvnw test` fast (no containers).
- The surefire plugin reads this property, so no `-DskipTests` needed.
- No `spring-boot-maven-plugin` repackaging — the e2e module doesn't produce a deployable artifact.

## application-e2e.properties

```properties
# Minimal overrides — everything else is satisfied by Testcontainers
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

- Flyway runs against the Testcontainers MySQL to apply migrations (the migrations are in the app JAR's classpath).
- `ddl-auto=validate` ensures the JPA entities match the Flyway-managed schema.
- `@ActiveProfiles("e2e")` on `AbstractE2eTest` activates this file.

## TestContainerConfig.java

```java
package nst.wms.e2e.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:8.0")
            .withDatabaseName("wms")
            .withUsername("test")
            .withPassword("test");

    static {
        MYSQL.start();
    }

    @Bean
    @ServiceConnection
    MySQLContainer<?> mysqlContainer() {
        return MYSQL;
    }
}
```

### Container lifecycle

- **Singleton per JVM:** The `static final` field + `static {}` initializer runs exactly once when the class is first loaded.
- **First test class loaded** → `TestContainerConfig` static init fires → MySQL container starts → `@ServiceConnection` registers the datasource properties → `@SpringBootTest` boots the app pointing at the real MySQL.
- **Every subsequent test class** picks up the already-running singleton container via `@Import(TestContainerConfig.class)`.
- **Container stops** when the JVM shuts down (Testcontainers Ryuk sidecar handles cleanup on JVM exit).

### Future additions (Kafka, Redis)

```java
static final KafkaContainer KAFKA = new KafkaContainer(
        DockerImageName.parse("confluentinc/cp-kafka:7.6.0")
);
static final GenericContainer<?> REDIS = new GenericContainer<>("redis:7-alpine")
        .withExposedPorts(6379);

static {
    MYSQL.start();
    KAFKA.start();
    REDIS.start();
}

@Bean @ServiceConnection
MySQLContainer<?> mysqlContainer() { return MYSQL; }

@Bean @ServiceConnection
KafkaContainer kafkaContainer() { return KAFKA; }

@Bean @ServiceConnection
GenericContainer<?> redisContainer() { return REDIS; }
```

All containers start once, all `@ServiceConnection` beans are registered, Spring Boot auto-configuration picks them up. No changes to `AbstractE2eTest` or any test class.

## AbstractE2eTest.java

```java
package nst.wms.e2e;

import io.restassured.RestAssured;
import nst.wms.e2e.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainerConfig.class)
@ActiveProfiles("e2e")
public abstract class AbstractE2eTest {

    @LocalServerPort
    protected int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
    }
}
```

### Design rationale

- **`@SpringBootTest` in the base class** — every E2E test boots the full application. No need to repeat `@SpringBootTest` on each subclass. Spring Boot's context caching ensures one context is reused across test classes if they share the same configuration.
- **`@ActiveProfiles("e2e")`** — isolates E2E configuration from both `application.properties` (MySQL) and `application-test.properties` (H2). The e2e profile's minimal settings allow Testcontainers to provide the datasource.
- **`@Import(TestContainerConfig.class)`** — pulls in the singleton container. Because the config is referenced here, it's part of Spring Boot's context cache key.

## UserE2eTest.java

```java
package nst.wms.e2e.user;

import io.restassured.common.mapper.TypeRef;
import io.restassured.http.ContentType;
import nst.wms.e2e.AbstractE2eTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

class UserE2eTest extends AbstractE2eTest {

    private static final String USERS_PATH = "/api/users";

    record CreateUserRequest(String name) {}
    record UpdateUserRequest(String name) {}
    record UserResponse(Long id, String name, String createdAt, String updatedAt) {}
    record UserSummary(Long id, String name) {}
    record PageResponse<T>(List<T> data, int page, int size, long count, int pages) {
        static <T> TypeRef<PageResponse<T>> typeRef() {
            return new TypeRef<>() {};
        }
    }

    @Test
    void shouldCreateGetUpdateDeleteUser() {
        // Create
        UserResponse user = given()
                .contentType(ContentType.JSON)
                .body(new CreateUserRequest("Alice"))
            .when()
                .post(USERS_PATH)
            .then()
                .statusCode(201)
                .extract().as(UserResponse.class);

        assertThat(user.id()).isNotNull();
        assertThat(user.name()).isEqualTo("Alice");
        assertThat(user.createdAt()).isNotNull();

        // Get by ID
        UserResponse fetched = given()
            .when()
                .get("{}/{}", USERS_PATH, user.id())
            .then()
                .statusCode(200)
                .extract().as(UserResponse.class);

        assertThat(fetched.name()).isEqualTo("Alice");

        // List — should contain Alice
        PageResponse<UserSummary> list = given()
                .param("page", 0)
                .param("size", 10)
            .when()
                .get(USERS_PATH)
            .then()
                .statusCode(200)
                .extract().as(PageResponse.typeRef());

        assertThat(list.data()).extracting(UserSummary::name).contains("Alice");

        // Update
        UserResponse updated = given()
                .contentType(ContentType.JSON)
                .body(new UpdateUserRequest("Alice Updated"))
            .when()
                .put("{}/{}", USERS_PATH, user.id())
            .then()
                .statusCode(200)
                .extract().as(UserResponse.class);

        assertThat(updated.name()).isEqualTo("Alice Updated");

        // Delete
        given()
            .when()
                .delete("{}/{}", USERS_PATH, user.id())
            .then()
                .statusCode(204);

        // Verify gone
        given()
            .when()
                .get("{}/{}", USERS_PATH, user.id())
            .then()
                .statusCode(404);
    }

    @Test
    void shouldReturn400WhenNameIsBlank() {
        given()
                .contentType(ContentType.JSON)
                .body(new CreateUserRequest(""))
            .when()
                .post(USERS_PATH)
            .then()
                .statusCode(400);
    }

    @Test
    void shouldReturn404ForNonExistentUser() {
        given()
            .when()
                .get("{}/{}", USERS_PATH, 99999L)
            .then()
                .statusCode(404);
    }
}
```

### Test design notes

- **Full CRUD lifecycle in one test method** — create → get → list → update → delete → verify gone. This avoids state leakage concerns (one test class, one container, one method covers the complete flow).
- **Error case tests are read-only** — `shouldReturn400WhenNameIsBlank` doesn't depend on DB state. `shouldReturn404ForNonExistentUser` relies on user 99999 never being created.
- **Local DTO records** — mirror the HTTP response shapes without importing `nst.wms.*` classes. This is the black-box guarantee: the test only knows what it receives over HTTP.
- **`TypeRef` for generics** — RestAssured needs `TypeRef` to deserialize `PageResponse<UserSummary>` with proper generic type info.

---

## Maven Command Reference

| Command | What runs | Use case |
|---------|-----------|----------|
| `./mvnw test` | Unit + integration tests only | CI fast feedback, local dev |
| `./mvnw test -pl e2e -Dmaven.test.skip=false` | Just E2E tests | CI E2E stage, local E2E run |
| `./mvnw test -pl . -pl e2e -Dmaven.test.skip=false` | All tests | Full suite |

The reactor resolves `e2e`'s dependency on the root JAR automatically — no `mvn install` needed for interactive use (Maven builds the root project first in the reactor).

---

## Key Design Decisions Summary

| Decision | Rationale |
|----------|-----------|
| **Separate Maven module** | Compile-time enforcement — e2e code physically cannot `import nst.wms.UserService`. Only the compiled JAR is on test classpath. |
| **Hybrid POM (no parent POM shift)** | `packaging=jar` with `<modules>` is valid Maven. No source files moved. Zero disruption to existing build. |
| **Singleton container via static init + `@Bean`** | One MySQL container per JVM regardless of number of test classes. Fast startup for E2E suite. |
| **`@ServiceConnection`** | Eliminates `@DynamicPropertySource` boilerplate. Spring Boot 4.1 natively supports it with Testcontainers. |
| **`@ActiveProfiles("e2e")`** | Isolates E2E configuration from `test` profile. Different settings without touching existing configs. |
| **Local DTO records** | Mirror HTTP response shapes without importing app classes. Black‑box guarantee enforced at compile time. |
| **`<classifier>boot</classifier>`** | Preserves plain JAR as default artifact for e2e dependency. Fat JAR for deployment gets `-boot` suffix. |
| **RestAssured fluent API** | Readable BDD‑style given/when/then. Built‑in JSON parsing, status code assertions. |
| **Skipped by default** | `<maven.test.skip>true</maven.test.skip>` prevents accidental container startup in normal test runs. |

---

## Implementation Tasks

1. **Root POM changes** — add `<modules>` block, add `<classifier>boot</classifier>` to spring-boot-maven-plugin
2. **Create `e2e/pom.xml`** — parent, dependencies, surefire config
3. **Create `application-e2e.properties`** — Flyway + JPA settings
4. **Create `TestContainerConfig.java`** — singleton MySQL container with `@ServiceConnection`
5. **Create `AbstractE2eTest.java`** — `@SpringBootTest(RANDOM_PORT)`, `@Import`, RestAssured setup
6. **Create `UserE2eTest.java`** — black-box CRUD + error case tests
7. **Verify compilation** — `./mvnw compile -pl e2e`
8. **Run E2E tests** — `./mvnw test -pl e2e -Dmaven.test.skip=false`
9. **Verify existing tests still pass** — `./mvnw test -pl .`

---

## Verification & Success Criteria

- [ ] `./mvnw test` succeeds without starting any Docker containers
- [ ] `./mvnw test -pl e2e -Dmaven.test.skip=false` starts MySQL container, runs all E2E tests, all pass
- [ ] `./mvnw test` still passes (existing unit + integration tests unaffected)
- [ ] No class from `nst.wms` package (except DTO records in e2e) is imported in e2e source
- [ ] Container lifecycle works: only one MySQL container started for all test classes
- [ ] No test relies on data from another test (each test creates its own state or is read-only)
