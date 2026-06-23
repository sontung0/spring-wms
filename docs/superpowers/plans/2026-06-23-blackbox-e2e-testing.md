# Black-Box E2E Testing — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Add a separate `e2e/` Maven module with Testcontainers + RestAssured black-box E2E tests for the User API.

**Architecture:** The root POM (packaging=jar, src/ untouched) gets a `<modules>` block referencing `e2e/`. The `spring-boot-maven-plugin` uses `<classifier>boot</classifier>` so the plain JAR remains the default artifact. The e2e module depends on the root JAR at test scope — it can never import app source classes. A `TestContainerConfig` singleton starts one MySQL container per JVM via `@ServiceConnection`. `AbstractE2eTest` provides `@SpringBootTest(RANDOM_PORT)` + RestAssured setup. `UserE2eTest` exercises the full CRUD lifecycle via HTTP.

**Tech Stack:** Java 26, Spring Boot 4.1.0, Testcontainers, RestAssured, Maven

---

## Task 1: Root POM — Add Modules + Boot Classifier

**Files:**
- Modify: `pom.xml`

- [ ] **Step 1: Add `<modules>` block at end of POM**

Before the closing `</project>` tag, add:

```xml
<modules>
    <module>e2e</module>
</modules>
```

- [ ] **Step 2: Add `<classifier>boot</classifier>` to spring-boot-maven-plugin**

Change:

```xml
<plugin>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-maven-plugin</artifactId>
</plugin>
```

To:

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

This preserves the plain JAR as the default artifact (consumed by e2e), while the fat JAR gets `-boot` suffix.

- [ ] **Step 3: Verify compilation**

Run: `./mvnw compile -q`
Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add pom.xml
git commit -m "feat: add e2e module and boot classifier to spring-boot-maven-plugin"
```

---

## Task 2: Create e2e/pom.xml

**Files:**
- Create: `e2e/pom.xml`

- [ ] **Step 1: Create `e2e/pom.xml`**

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
        <!-- App JAR (plain artifact) — @SpringBootTest classpath -->
        <dependency>
            <groupId>nst</groupId>
            <artifactId>wms</artifactId>
            <version>${project.version}</version>
            <scope>test</scope>
        </dependency>

        <!-- Spring Boot test starter -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>

        <!-- Testcontainers -->
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

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -pl e2e -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add e2e/pom.xml
git commit -m "feat: create e2e module with Testcontainers and RestAssured dependencies"
```

---

## Task 3: Create application-e2e.properties

**Files:**
- Create: `e2e/src/test/resources/application-e2e.properties`

- [ ] **Step 1: Create the properties file**

```properties
spring.jpa.hibernate.ddl-auto=validate
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
```

- [ ] **Step 2: Commit**

```bash
git add e2e/src/test/resources/application-e2e.properties
git commit -m "feat: add e2e profile config with Flyway and JPA validation"
```

---

## Task 4: Create TestContainerConfig.java

**Files:**
- Create: `e2e/src/test/java/nst/wms/e2e/config/TestContainerConfig.java`

- [ ] **Step 1: Create the container configuration**

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

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -pl e2e -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add e2e/src/test/java/nst/wms/e2e/config/TestContainerConfig.java
git commit -m "feat: add singleton MySQL Testcontainer with @ServiceConnection"
```

---

## Task 5: Create AbstractE2eTest.java

**Files:**
- Create: `e2e/src/test/java/nst/wms/e2e/AbstractE2eTest.java`

- [ ] **Step 1: Create the base test class**

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

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -pl e2e -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add e2e/src/test/java/nst/wms/e2e/AbstractE2eTest.java
git commit -m "feat: add E2E base test class with @SpringBootTest(RANDOM_PORT) and RestAssured setup"
```

---

## Task 6: Create UserE2eTest.java

**Files:**
- Create: `e2e/src/test/java/nst/wms/e2e/user/UserE2eTest.java`

- [ ] **Step 1: Create the user E2E test class**

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

        // List
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

- [ ] **Step 2: Verify compilation**

Run: `./mvnw compile -pl e2e -q`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add e2e/src/test/java/nst/wms/e2e/user/UserE2eTest.java
git commit -m "feat: add black-box E2E test for User CRUD API"
```

---

## Task 7: Run E2E Tests and Verify

**Note:** This requires Docker to be running on the machine.

- [ ] **Step 1: Run E2E tests**

Run: `./mvnw test -pl e2e -Dmaven.test.skip=false`
Expected: BUILD SUCCESS, 3 tests pass (shouldCreateGetUpdateDeleteUser, shouldReturn400WhenNameIsBlank, shouldReturn404ForNonExistentUser)

If tests fail, examine the output and fix issues. Common issues:
- Docker not running → start Docker daemon
- Port conflict → ensure no local MySQL on port 3306 (Testcontainers uses random ports, so this is unlikely)
- Flyway migration path issue → verify `spring.flyway.locations=classpath:db/migration` finds `V1__create_user_table.sql` in the app JAR

- [ ] **Step 2: Verify existing tests still pass**

Run: `./mvnw test -pl .`
Expected: BUILD SUCCESS — all existing unit and integration tests pass

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "chore: finalize E2E module setup and verify tests pass"
```

---

## Verification & Success Criteria

- [ ] `./mvnw test` succeeds without starting any Docker containers (unit + integration tests only)
- [ ] `./mvnw test -pl e2e -Dmaven.test.skip=false` starts a MySQL container and all 3 E2E tests pass
- [ ] `./mvnw test` still passes — existing tests unaffected
- [ ] No `import nst.wms.*` statement exists in any e2e source file
- [ ] Only one MySQL container started for the entire E2E suite
