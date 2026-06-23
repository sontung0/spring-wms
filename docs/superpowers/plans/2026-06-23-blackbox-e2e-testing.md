# Black-Box E2E Testing Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Restructure the single-module project into a Maven multi-module build and add a black-box E2E test module with Testcontainers + RestTestClient.

**Architecture:** Root POM becomes a pure `pom` aggregator. Existing source moves to `app/` via `git mv`. New `e2e/` module depends on the compiled app JAR at test scope and boots the full application against a real MySQL container. Tests communicate only over HTTP — zero compile-time dependency on app internals.

**Tech Stack:** Spring Boot 4.1.0, Maven multi-module, Testcontainers 2.0.x (`testcontainers-mysql`, `testcontainers-junit-jupiter`), RestTestClient (Spring Framework 7.0, bundled with Spring Boot 4.1.0), JUnit 5, Flyway, MySQL LTS Docker image.

> **Implementation note:** RestAssured 5.5.1 was originally planned but replaced by RestTestClient due to Java 26 incompatibility. RestAssured's Groovy 5.0.6 internals trigger `Class.isAssignableFrom(Native Method)` NPE on Java 26's stricter module system. RestTestClient (`org.springframework.test.web.servlet.client.RestTestClient`) from Spring Framework 7.0 provides `bindToServer()` for real HTTP connections with a fluent assertion API — no additional dependencies needed.

**Spec:** `docs/superpowers/specs/2026-06-23-blackbox-e2e-testing.md`

---

## File Structure

| Action | File | Responsibility |
|--------|------|----------------|
| Modify | `pom.xml` | Root aggregator: `packaging=pom`, `<modules>`, parent only |
| Create | `app/pom.xml` | App module: all deps, plugins, depMgmt |
| Move | `src/main/` → `app/src/main/` | `git mv` (history preserved) |
| Move | `src/test/` → `app/src/test/` | `git mv` (history preserved) |
| Create | `e2e/pom.xml` | E2E module: app dep, TC 2.x, Testcontainers deps |
| Create | `e2e/src/test/resources/application-e2e.properties` | E2E Spring profile config |
| Create | `e2e/src/test/java/nst/wms/e2e/config/TestContainerConfig.java` | Singleton MySQL container |
| Create | `e2e/src/test/java/nst/wms/e2e/AbstractE2eTest.java` | Base test class |
| Create | `e2e/src/test/java/nst/wms/e2e/user/UserE2eTest.java` | Black-box CRUD tests |

---

### Task 1: Rewrite root POM to pure aggregator

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Replace root POM content**

Replace the entire `pom.xml` with the pure aggregator POM. This removes all dependencies, plugins, and dependency management — those move to `app/pom.xml` in Task 2.

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>4.1.0</version>
        <relativePath/>
    </parent>
    <groupId>nst</groupId>
    <artifactId>wms</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <packaging>pom</packaging>
    <name>wms</name>
    <description/>
    <url/>
    <licenses>
        <license/>
    </licenses>
    <developers>
        <developer/>
    </developers>
    <scm>
        <connection/>
        <developerConnection/>
        <tag/>
        <url/>
    </scm>
    <properties>
        <java.version>26</java.version>
    </properties>
    <modules>
        <module>app</module>
        <module>e2e</module>
    </modules>
</project>
```

- [ ] **Step 2: Commit**

```bash
git add pom.xml
git commit -m "refactor: convert root POM to pure aggregator (packaging=pom)"
```

---

### Task 2: Create app/pom.xml

**Files:**
- Create: `app/pom.xml`

- [ ] **Step 1: Create `app/pom.xml` with all dependencies, plugins, and dependency management from the original root POM**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>
    <parent>
        <groupId>nst</groupId>
        <artifactId>wms</artifactId>
        <version>0.0.1-SNAPSHOT</version>
        <relativePath>../pom.xml</relativePath>
    </parent>
    <artifactId>app</artifactId>
    <packaging>jar</packaging>
    <name>wms-app</name>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-json</artifactId>
        </dependency>

        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-webmvc-test</artifactId>
            <scope>test</scope>
        </dependency>

        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-starter-core</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.modulith</groupId>
            <artifactId>spring-modulith-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>com.mysql</groupId>
            <artifactId>mysql-connector-j</artifactId>
            <scope>runtime</scope>
        </dependency>

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
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-flyway</artifactId>
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
            <scope>runtime</scope>
        </dependency>
    </dependencies>

    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <executions>
                    <execution>
                        <id>repackage</id>
                        <goals>
                            <goal>repackage</goal>
                        </goals>
                        <configuration>
                            <classifier>boot</classifier>
                        </configuration>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springframework.modulith</groupId>
                <artifactId>spring-modulith-bom</artifactId>
                <version>2.1.0</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>
        </dependencies>
    </dependencyManagement>
</project>
```

- [ ] **Step 2: Commit**

```bash
git add app/pom.xml
git commit -m "feat: create app submodule POM with all deps and plugins"
```

---

### Task 3: Move source files to app/

**Files:**
- Move: `src/main/` → `app/src/main/`
- Move: `src/test/` → `app/src/test/`

- [ ] **Step 1: Move source directories**

```bash
mkdir -p app
git mv src/main app/
git mv src/test app/
```

- [ ] **Step 2: Verify the moved files exist**

```bash
ls app/src/main/java/nst/wms/WmsApplication.java
ls app/src/test/java/nst/wms/WmsApplicationTests.java
ls app/src/main/resources/db/migration/V1__create_user_table.sql
ls app/src/main/resources/application.properties
ls app/src/test/resources/application-test.properties
```

Expected: all paths exist.

- [ ] **Step 3: Verify app compiles**

```bash
./mvnw compile -pl app
```

Expected: `BUILD SUCCESS`. This confirms the new module structure works — `app` inherits Spring Boot BOM from root's parent, and all dependencies resolve.

- [ ] **Step 4: Verify existing tests pass against H2**

```bash
./mvnw test -pl app
```

Expected: all tests pass (H2 in-memory, no MySQL needed). This confirms the git move didn't break anything.

- [ ] **Step 5: Commit**

```bash
git add -A
git commit -m "refactor: move src/ into app/ submodule via git mv"
```

---

### Task 4: Create e2e/pom.xml

**Files:**
- Create: `e2e/pom.xml`

- [ ] **Step 1: Create `e2e/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
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
            <artifactId>app</artifactId>
            <version>${project.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Spring Boot test starter (JUnit 5, Mockito, AssertJ) -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!--
          Testcontainers 2.0.x (managed by Spring Boot 4.1.0 BOM):
          artifact IDs changed from TC 1.x — 'junit-jupiter' → 'testcontainers-junit-jupiter',
          'mysql' → 'testcontainers-mysql'.
        -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-testcontainers</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.testcontainers</groupId>
            <artifactId>testcontainers-mysql</artifactId>
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

- [ ] **Step 2: Verify e2e module compiles (tests skipped by default)**

```bash
./mvnw compile -pl e2e
```

Expected: `BUILD SUCCESS` (no test classes to compile yet, and tests are skipped by default).

- [ ] **Step 3: Commit**

```bash
git add e2e/pom.xml
git commit -m "feat: create e2e module POM with TC 2.x and Testcontainers deps"
```

---

### Task 5: Create application-e2e.properties

**Files:**
- Create: `e2e/src/test/resources/application-e2e.properties`

- [ ] **Step 1: Create the e2e profile properties file**

```properties
# Minimal overrides — everything else is satisfied by Testcontainers
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

- [ ] **Step 2: Commit**

```bash
git add e2e/src/test/resources/application-e2e.properties
git commit -m "feat: add application-e2e.properties profile"
```

---

### Task 6: Create TestContainerConfig.java

**Files:**
- Create: `e2e/src/test/java/nst/wms/e2e/config/TestContainerConfig.java`

- [ ] **Step 1: Create the Testcontainers configuration class**

```java
package nst.wms.e2e.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.MySQLContainer;

@TestConfiguration(proxyBeanMethods = false)
public class TestContainerConfig {

    static final MySQLContainer<?> MYSQL = new MySQLContainer<>("mysql:lts")
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

- [ ] **Step 2: Commit**

```bash
git add e2e/src/test/java/nst/wms/e2e/config/TestContainerConfig.java
git commit -m "feat: add TestContainerConfig with singleton MySQL container"
```

---

### Task 7: Create AbstractE2eTest.java

**Files:**
- Create: `e2e/src/test/java/nst/wms/e2e/AbstractE2eTest.java`

- [ ] **Step 1: Create the abstract base test class**

```java
package nst.wms.e2e;

import nst.wms.e2e.config.TestContainerConfig;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.client.RestTestClient;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestContainerConfig.class)
@ActiveProfiles("e2e")
public abstract class AbstractE2eTest {

    @LocalServerPort
    protected int port;

    protected RestTestClient client;

    @BeforeEach
    void setUp() {
        client = RestTestClient.bindToServer()
                .baseUrl("http://localhost:" + port)
                .build();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add e2e/src/test/java/nst/wms/e2e/AbstractE2eTest.java
git commit -m "feat: add AbstractE2eTest base class with RestTestClient setup"
```

---

### Task 8: Create UserE2eTest.java

**Files:**
- Create: `e2e/src/test/java/nst/wms/e2e/user/UserE2eTest.java`

- [ ] **Step 1: Create the black-box User E2E test**

```java
package nst.wms.e2e.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import nst.wms.e2e.AbstractE2eTest;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UserE2eTest extends AbstractE2eTest {

    private static final String USERS_PATH = "/api/users";

    // Local DTOs — no imports from nst.wms.* (black-box guarantee)
    record CreateUserRequest(String name) {}
    record UpdateUserRequest(String name) {}

    public record UserResponse(
            Long id,
            String name,
            @JsonProperty("createdAt") String createdAt,
            @JsonProperty("updatedAt") String updatedAt
    ) {}

    public record UserSummary(Long id, String name) {}

    public record PageResponse<T>(
            List<T> data,
            int page,
            int size,
            long count,
            int pages
    ) {}

    @Test
    void shouldCreateGetUpdateDeleteUser() {
        // Create
        UserResponse[] created = { null };
        client.post().uri(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateUserRequest("Alice"))
                .exchange()
                .expectStatus().isCreated()
                .expectBody(UserResponse.class)
                .value(resp -> {
                    created[0] = resp;
                    assertThat(resp.id()).isNotNull();
                    assertThat(resp.name()).isEqualTo("Alice");
                    assertThat(resp.createdAt()).isNotNull();
                });

        assertThat(created[0]).isNotNull();
        Long userId = created[0].id();

        // Get by ID
        client.get().uri(USERS_PATH + "/" + userId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(resp -> {
                    assertThat(resp.name()).isEqualTo("Alice");
                });

        // List — should contain Alice
        client.get().uri(uriBuilder -> uriBuilder
                        .path(USERS_PATH)
                        .queryParam("page", 0)
                        .queryParam("size", 10)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(new ParameterizedTypeReference<PageResponse<UserSummary>>() {})
                .value(list -> {
                    assertThat(list.data()).extracting(UserSummary::name).contains("Alice");
                });

        // Update
        client.put().uri(USERS_PATH + "/" + userId)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new UpdateUserRequest("Alice Updated"))
                .exchange()
                .expectStatus().isOk()
                .expectBody(UserResponse.class)
                .value(resp -> {
                    assertThat(resp.name()).isEqualTo("Alice Updated");
                });

        // Delete
        client.delete().uri(USERS_PATH + "/" + userId)
                .exchange()
                .expectStatus().value(status -> {
                    assertThat(status).isEqualTo(204);
                });

        // Verify gone
        client.get().uri(USERS_PATH + "/" + userId)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void shouldReturn400WhenNameIsBlank() {
        client.post().uri(USERS_PATH)
                .contentType(MediaType.APPLICATION_JSON)
                .body(new CreateUserRequest(""))
                .exchange()
                .expectStatus().isBadRequest();
    }

    @Test
    void shouldReturn404ForNonExistentUser() {
        client.get().uri(USERS_PATH + "/99999")
                .exchange()
                .expectStatus().isNotFound();
    }
}
```

- [ ] **Step 2: Commit**

```bash
git add e2e/src/test/java/nst/wms/e2e/user/UserE2eTest.java
git commit -m "feat: add UserE2eTest — black-box CRUD + error case tests"
```

---

### Task 9: Run E2E tests

**Files:**
- No file changes — verification only.

- [ ] **Step 1: Run E2E tests with Maven override**

```bash
./mvnw test -pl e2e -am -Dmaven.test.skip=false
```

Expected: Maven builds `app` first (reactor order via `-am`), then builds `e2e`. Docker starts `mysql:lts` container. Flyway applies `V1__create_user_table.sql`. All 3 tests pass:
- `shouldCreateGetUpdateDeleteUser` — PASS
- `shouldReturn400WhenNameIsBlank` — PASS
- `shouldReturn404ForNonExistentUser` — PASS

If `shouldReturn400WhenNameIsBlank` fails with 201 instead of 400, check that the `@Valid` annotation and `spring-boot-starter-validation` are working. The `CreateUserRequest` class needs `@NotBlank` on the `name` field. Since the spec uses local DTO records without validation annotations, the validation comes from the app's `CreateUserRequest` which is on the classpath via the app JAR. The app's controller receives `@Valid @RequestBody CreateUserRequest request` — this validates against the **app's** `CreateUserRequest` (which should have `@NotBlank`). Verify this with:

```bash
grep -r "@NotBlank\|@NotEmpty" app/src/main/java/nst/wms/user/presentation/dto/CreateUserRequest.java
```

If the annotation is missing in the app, the test would get 201 instead of 400. In that case, the app needs a `@NotBlank` on `CreateUserRequest.name` first — but that's an app fix, not an E2E test issue.

- [ ] **Step 2: Verify app tests still pass independently**

```bash
./mvnw test -pl app
```

Expected: all existing tests pass, no Docker containers started.

- [ ] **Step 3: Verify default `./mvnw test` skips E2E**

```bash
./mvnw test
```

Expected: app tests run, e2e tests are skipped (no Docker containers started).

---

## Self-Review Checklist

- [x] **Spec coverage:** Task 1 (root POM), Task 2 (app POM), Task 3 (git mv), Task 4 (e2e POM), Task 5 (e2e properties), Task 6 (TestContainerConfig), Task 7 (AbstractE2eTest), Task 8 (UserE2eTest), Task 9 (verification) — all spec sections covered.
- [x] **Placeholder scan:** No TBD/TODO. All code is complete and copy-pasteable.
- [x] **Type consistency:** `UserResponse`, `UserSummary`, `PageResponse`, `CreateUserRequest`, `UpdateUserRequest` record names match between `UserE2eTest` and the spec. `TestContainerConfig` class name and `@ServiceConnection` usage consistent. `AbstractE2eTest` references correct config class.
- [x] **TC 2.x artifacts:** `testcontainers-junit-jupiter` and `testcontainers-mysql` (not the TC 1.x names). RestTestClient requires no explicit version (bundled with Spring Framework 7.0 via Spring Boot 4.1.0).
- [x] **`MySQLContainer` import:** `org.testcontainers.containers.MySQLContainer` — confirmed valid in TC 2.0.x jar.
