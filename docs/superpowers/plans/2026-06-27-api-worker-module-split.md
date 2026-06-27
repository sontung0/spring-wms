# API / Worker Module Split Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Split the monolith `app` module into `app` (shared library), `api` (HTTP layer), and `worker` (future background tasks).

**Architecture:** Three-module Maven structure: `app` stays as a plain jar with domain/services/repos; `api` is a new Spring Boot jar with the web layer; `worker` is a new Spring Boot jar scaffolding. Both `api` and `worker` depend on `app`. Deploy as separate JVMs sharing the same DB.

**Tech Stack:** Spring Boot 4.1, Java 25, Spring Modulith, Maven multi-module

---

## File Structure

### New files to create

| File | Purpose |
|------|---------|
| `api/pom.xml` | New Spring Boot module for HTTP layer |
| `api/src/main/java/nst/wms/ApiApplication.java` | Spring Boot entry point (renamed from WmsApplication) |
| `api/src/main/resources/application.properties` | API-specific config overrides |
| `api/src/test/resources/application-test.properties` | API test config |
| `worker/pom.xml` | New Spring Boot module for future background tasks |
| `worker/src/main/java/nst/wms/worker/WorkerApplication.java` | Bare Spring Boot entry point |
| `worker/src/main/resources/application.properties` | Minimal worker config |

### Files to move (copy + delete)

From `app/src/main/java/...` → `api/src/main/java/...` (keep same packages):

| File | New location |
|------|-------------|
| `WmsApplication.java` | `ApiApplication.java` (rename) |
| `auth/presentation/AuthController.java` | same path |
| `auth/presentation/AuthExceptionHandler.java` | same path |
| `auth/presentation/dto/AuthorizeResponse.java` | same path |
| `auth/presentation/dto/CallbackRequest.java` | same path |
| `auth/presentation/dto/CallbackResponse.java` | same path |
| `auth/infrastructure/SecurityConfig.java` | same path |
| `auth/infrastructure/OpenApiConfig.java` | same path |
| `user/presentation/UserController.java` | same path |
| `user/presentation/dto/CreateUserRequest.java` | same path |
| `user/presentation/dto/UpdateUserRequest.java` | same path |
| `user/presentation/dto/UserResponse.java` | same path |
| `user/presentation/dto/UserSummary.java` | same path |
| `user/presentation/dto/PageResponse.java` | same path |
| `common/error/GlobalExceptionHandler.java` | same path |
| `common/error/GlobalExceptionLoggingResolver.java` | same path |
| `common/api/ErrorResponse.java` | same path |

From `app/src/test/java/...` → `api/src/test/java/...`:

| File | New location |
|------|-------------|
| `WmsApplicationTests.java` | `ApiApplicationTests.java` (rename) |
| `ModulithVerificationTest.java` | same path (update class ref to `ApiApplication.class`) |
| `auth/presentation/AuthApiTest.java` | same path |
| `user/presentation/UserApiTest.java` | same path |
| `common/error/GlobalExceptionHandlerTest.java` | same path |
| `common/error/GlobalExceptionLoggingResolverTest.java` | same path |

### Files to modify

| File | Change |
|------|--------|
| `pom.xml` (root) | Add `api`, `worker` to modules list |
| `app/pom.xml` | Remove web deps, security deps, validation, springdoc, boot plugin |
| `e2e/pom.xml` | Switch dependency from `app` to `api` |

### Files to keep (unchanged in `app`)

All other source in `app`: domain entities, services, repos, JPA entities, OAuth providers, token service, caches, `BusinessException`, `NotFoundException`, `UserFilter` (stays in `app` despite `presentation.dto` package — it's used by `UserService`/`UserRepository`).

---

## Task 1: Create `api/pom.xml`

**Files:**
- Create: `api/pom.xml`

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>nst</groupId>
		<artifactId>wms</artifactId>
		<version>0.0.1-SNAPSHOT</version>
		<relativePath>../pom.xml</relativePath>
	</parent>
	<artifactId>wms-api</artifactId>
	<packaging>jar</packaging>
	<name>wms-api</name>

	<dependencies>
		<dependency>
			<groupId>nst</groupId>
			<artifactId>app</artifactId>
			<version>${project.version}</version>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-security</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-oauth2-resource-server</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-validation</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>
		<dependency>
			<groupId>org.springdoc</groupId>
			<artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
			<version>3.0.3</version>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-webmvc-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.springframework.security</groupId>
			<artifactId>spring-security-test</artifactId>
			<scope>test</scope>
		</dependency>
		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<annotationProcessorPaths>
						<path>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</path>
					</annotationProcessorPaths>
				</configuration>
			</plugin>
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

- [ ] **Step 1: Create `api/pom.xml`**

Create file at `api/pom.xml` with the content above.

- [ ] **Step 2: Create `api` parent directories**

Run:
```bash
mkdir -p api/src/main/java/nst/wms/api-dist
mkdir -p api/src/main/java/nst/wms/auth/presentation/dto
mkdir -p api/src/main/java/nst/wms/auth/infrastructure
mkdir -p api/src/main/java/nst/wms/user/presentation/dto
mkdir -p api/src/main/java/nst/wms/common/error
mkdir -p api/src/main/java/nst/wms/common/api
mkdir -p api/src/main/resources
mkdir -p api/src/test/java/nst/wms/auth/presentation
mkdir -p api/src/test/java/nst/wms/user/presentation
mkdir -p api/src/test/java/nst/wms/common/error
mkdir -p api/src/test/resources
```

---

## Task 2: Create `worker/pom.xml` and scaffolding

**Files:**
- Create: `worker/pom.xml`
- Create: `worker/src/main/java/nst/wms/worker/WorkerApplication.java`
- Create: `worker/src/main/resources/application.properties`

- [ ] **Step 1: Create `worker/pom.xml`**

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>nst</groupId>
		<artifactId>wms</artifactId>
		<version>0.0.1-SNAPSHOT</version>
		<relativePath>../pom.xml</relativePath>
	</parent>
	<artifactId>wms-worker</artifactId>
	<packaging>jar</packaging>
	<name>wms-worker</name>

	<dependencies>
		<dependency>
			<groupId>nst</groupId>
			<artifactId>app</artifactId>
			<version>${project.version}</version>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-actuator</artifactId>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.apache.maven.plugins</groupId>
				<artifactId>maven-compiler-plugin</artifactId>
				<configuration>
					<annotationProcessorPaths>
						<path>
							<groupId>org.projectlombok</groupId>
							<artifactId>lombok</artifactId>
						</path>
					</annotationProcessorPaths>
				</configuration>
			</plugin>
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
		</plugins>
	</build>
</project>
```

- [ ] **Step 2: Create directory structure**

```bash
mkdir -p worker/src/main/java/nst/wms/worker
mkdir -p worker/src/main/resources
```

- [ ] **Step 3: Create `WorkerApplication.java`**

```java
package nst.wms.worker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class WorkerApplication {

    public static void main(String[] args) {
        SpringApplication.run(WorkerApplication.class, args);
    }
}
```

- [ ] **Step 4: Create `worker/src/main/resources/application.properties`**

```properties
spring.application.name=wms-worker
```

---

## Task 3: Move web source files from `app` to `api`

**Files:**
- Copy each file listed in "Files to move" above from `app/src/main/java/...` to `api/src/main/java/...`
- Rename `WmsApplication.java` → `ApiApplication.java` during copy

- [ ] **Step 1: Copy all moved Java source files**

Run (from project root):

```bash
# ApiApplication (renamed entry point)
cp app/src/main/java/nst/wms/WmsApplication.java api/src/main/java/nst/wms/ApiApplication.java

# Auth presentation
cp app/src/main/java/nst/wms/auth/presentation/AuthController.java api/src/main/java/nst/wms/auth/presentation/
cp app/src/main/java/nst/wms/auth/presentation/AuthExceptionHandler.java api/src/main/java/nst/wms/auth/presentation/
cp app/src/main/java/nst/wms/auth/presentation/dto/AuthorizeResponse.java api/src/main/java/nst/wms/auth/presentation/dto/
cp app/src/main/java/nst/wms/auth/presentation/dto/CallbackRequest.java api/src/main/java/nst/wms/auth/presentation/dto/
cp app/src/main/java/nst/wms/auth/presentation/dto/CallbackResponse.java api/src/main/java/nst/wms/auth/presentation/dto/

# Auth infrastructure (config)
cp app/src/main/java/nst/wms/auth/infrastructure/SecurityConfig.java api/src/main/java/nst/wms/auth/infrastructure/
cp app/src/main/java/nst/wms/auth/infrastructure/OpenApiConfig.java api/src/main/java/nst/wms/auth/infrastructure/

# User presentation
cp app/src/main/java/nst/wms/user/presentation/UserController.java api/src/main/java/nst/wms/user/presentation/
cp app/src/main/java/nst/wms/user/presentation/dto/CreateUserRequest.java api/src/main/java/nst/wms/user/presentation/dto/
cp app/src/main/java/nst/wms/user/presentation/dto/UpdateUserRequest.java api/src/main/java/nst/wms/user/presentation/dto/
cp app/src/main/java/nst/wms/user/presentation/dto/UserResponse.java api/src/main/java/nst/wms/user/presentation/dto/
cp app/src/main/java/nst/wms/user/presentation/dto/UserSummary.java api/src/main/java/nst/wms/user/presentation/dto/
cp app/src/main/java/nst/wms/user/presentation/dto/PageResponse.java api/src/main/java/nst/wms/user/presentation/dto/

# Common web
cp app/src/main/java/nst/wms/common/error/GlobalExceptionHandler.java api/src/main/java/nst/wms/common/error/
cp app/src/main/java/nst/wms/common/error/GlobalExceptionLoggingResolver.java api/src/main/java/nst/wms/common/error/
cp app/src/main/java/nst/wms/common/api/ErrorResponse.java api/src/main/java/nst/wms/common/api/
```

- [ ] **Step 2: Rename class in `ApiApplication.java`**

Replace `WmsApplication` with `ApiApplication` in the new file.

Change `public class WmsApplication` to `public class ApiApplication`.

- [ ] **Step 3: Delete moved files from `app/src/main/java`**

```bash
rm app/src/main/java/nst/wms/WmsApplication.java

rm -rf app/src/main/java/nst/wms/auth/presentation/

rm app/src/main/java/nst/wms/auth/infrastructure/SecurityConfig.java
rm app/src/main/java/nst/wms/auth/infrastructure/OpenApiConfig.java

rm app/src/main/java/nst/wms/user/presentation/UserController.java
rm -rf app/src/main/java/nst/wms/user/presentation/dto/CreateUserRequest.java
rm -rf app/src/main/java/nst/wms/user/presentation/dto/UpdateUserRequest.java
rm -rf app/src/main/java/nst/wms/user/presentation/dto/UserResponse.java
rm -rf app/src/main/java/nst/wms/user/presentation/dto/UserSummary.java
rm -rf app/src/main/java/nst/wms/user/presentation/dto/PageResponse.java
rm -rf app/src/main/java/nst/wms/user/presentation/dto/
rmdir app/src/main/java/nst/wms/user/presentation/ || true

rm app/src/main/java/nst/wms/common/error/GlobalExceptionHandler.java
rm app/src/main/java/nst/wms/common/error/GlobalExceptionLoggingResolver.java
rm app/src/main/java/nst/wms/common/api/ErrorResponse.java
rmdir app/src/main/java/nst/wms/common/api/ || true
rmdir app/src/main/java/nst/wms/common/error/ || true
```

- [ ] **Step 4: Add `@ComponentScan(basePackageClasses = ApiApplication.class)` to `ApiApplication`**

Since `app` can't be found via `WmsApplication` anymore, we need to ensure `api`'s `@SpringBootApplication` scans the right packages. Replace the content of `ApiApplication.java`:

```java
package nst.wms;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
```

`@SpringBootApplication` auto-scans `nst.wms` and sub-packages, and `app`'s classes are on the classpath with the same package prefix, so they'll be picked up automatically. No extra component scan needed.

---

## Task 4: Move test files from `app` to `api`

**Files:**
- Copy test files listed above from `app/src/test/java/...` to `api/src/test/java/...`
- Rename `WmsApplicationTests.java` → `ApiApplicationTests.java` and update class name + reference

- [ ] **Step 1: Copy test files**

```bash
# Context load test
cp app/src/test/java/nst/wms/WmsApplicationTests.java api/src/test/java/nst/wms/ApiApplicationTests.java

# Modulith verification
cp app/src/test/java/nst/wms/ModulithVerificationTest.java api/src/test/java/nst/wms/

# Auth presentation tests
cp app/src/test/java/nst/wms/auth/presentation/AuthApiTest.java api/src/test/java/nst/wms/auth/presentation/

# User presentation tests
cp app/src/test/java/nst/wms/user/presentation/UserApiTest.java api/src/test/java/nst/wms/user/presentation/

# Common error tests
cp app/src/test/java/nst/wms/common/error/GlobalExceptionHandlerTest.java api/src/test/java/nst/wms/common/error/
cp app/src/test/java/nst/wms/common/error/GlobalExceptionLoggingResolverTest.java api/src/test/java/nst/wms/common/error/
```

- [ ] **Step 2: Rename class in `ApiApplicationTests.java`**

Change `class WmsApplicationTests` to `class ApiApplicationTests`.

- [ ] **Step 3: Update `ModulithVerificationTest.java` to reference `ApiApplication`**

Change `ApplicationModules.of(WmsApplication.class)` to `ApplicationModules.of(ApiApplication.class)`.

- [ ] **Step 4: Delete test files from `app`**

```bash
rm app/src/test/java/nst/wms/WmsApplicationTests.java
rm app/src/test/java/nst/wms/ModulithVerificationTest.java
rm app/src/test/java/nst/wms/auth/presentation/AuthApiTest.java
rmdir app/src/test/java/nst/wms/auth/presentation/ || true
rm app/src/test/java/nst/wms/user/presentation/UserApiTest.java
rmdir app/src/test/java/nst/wms/user/presentation/ || true
rm app/src/test/java/nst/wms/common/error/GlobalExceptionHandlerTest.java
rm app/src/test/java/nst/wms/common/error/GlobalExceptionLoggingResolverTest.java
rmdir app/src/test/java/nst/wms/common/error/ || true
```

- [ ] **Step 5: Copy application-test.properties to api**

```bash
cp app/src/test/resources/application-test.properties api/src/test/resources/application-test.properties
```

---

## Task 5: Update `app/pom.xml` — strip web dependencies

**Files:**
- Modify: `app/pom.xml`

- [ ] **Step 1: Remove removed dependencies**

Remove these dependencies from `app/pom.xml`:
- `spring-boot-starter-webmvc`
- `spring-boot-starter-webmvc-test`
- `spring-boot-starter-security`
- `spring-boot-starter-oauth2-resource-server`
- `spring-boot-starter-validation`
- `springdoc-openapi-starter-webmvc-ui`
- `spring-security-test`

- [ ] **Step 2: Remove `spring-boot-maven-plugin`**

Remove the entire `<plugin><groupId>org.springframework.boot</groupId>...</plugin>` from the `<plugins>` section.

- [ ] **Step 3: Swap `webmvc` for `restclient`**

Replace `<artifactId>spring-boot-starter-webmvc</artifactId>` with `<artifactId>spring-boot-starter-restclient</artifactId>`.

- [ ] **Step 4: Verify final `app/pom.xml` dependencies**

The remaining dependencies should be:
- `spring-boot-starter-actuator`
- `spring-boot-starter-restclient`
- `spring-boot-starter-json`
- `spring-modulith-starter-core`
- `spring-modulith-starter-test` (test)
- `spring-boot-starter-data-jpa`
- `mysql-connector-j` (runtime)
- `spring-boot-starter-cache`
- `caffeine`
- `flyway-core`
- `flyway-mysql`
- `spring-boot-starter-flyway`
- `h2` (runtime, test)
- `lombok` (optional)

---

## Task 6: Update root `pom.xml` — add api and worker modules

**Files:**
- Modify: `pom.xml` (root)

- [ ] **Step 1: Add modules**

Add `api` and `worker` to the `<modules>` list:

```xml
<modules>
    <module>app</module>
    <module>api</module>
    <module>worker</module>
    <module>e2e</module>
</modules>
```

---

## Task 7: Update `e2e/pom.xml` — switch dependency from app to api

**Files:**
- Modify: `e2e/pom.xml`

- [ ] **Step 1: Change dependency**

Change:
```xml
<dependency>
    <groupId>nst</groupId>
    <artifactId>app</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```
To:
```xml
<dependency>
    <groupId>nst</groupId>
    <artifactId>wms-api</artifactId>
    <version>${project.version}</version>
    <scope>test</scope>
</dependency>
```

---

## Task 8: Create API config files

**Files:**
- Create: `api/src/main/resources/application.properties`
- Create: `api/src/test/resources/application-test.properties`

- [ ] **Step 1: Create `api/src/main/resources/application.properties`**

```properties
spring.application.name=wms-api
server.port=8080
```

Since `app`'s `application.properties` is on the classpath (shared library), all the shared config (DB, Flyway, JPA, cache, auth secrets) is inherited automatically. Only API-specific overrides needed here.

- [ ] **Step 2: Create `api/src/test/resources/application-test.properties`**

```properties
spring.application.name=wms-api-test
spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.H2Dialect
spring.flyway.enabled=false
auth.oauth.redirectUri=http://localhost:3000/auth/callback
auth.jwt.issuer=wms-test
auth.jwt.ttl=PT1H
```

For testing in `api`, we still need fake RSA keys. Use the same test keys that were used for `app`'s tests. Copy them:

```bash
cp app/src/test/resources/application-test.properties api/src/test/resources/application-test.properties
```

And add `auth.jwt.private-key` / `auth.jwt.public-key` test values if they exist in `app`'s test config.

---

## Task 9: Verify compilation

- [ ] **Step 1: Compile the full project**

Run:
```bash
./mvnw clean compile -q 2>&1 | tail -30
```
Expected: `BUILD SUCCESS`

- [ ] **Step 2: Fix any compilation errors**

If any class in `api` references a missing import (e.g., Springdoc annotations on DTOs that stayed in `app`), fix the import path. The DTOs in `api` already have `springdoc` as a dependency so `@Schema` works fine.

---

## Task 10: Run tests

- [ ] **Step 1: Run `app` tests (service-layer)**

```bash
./mvnw test -pl app -q 2>&1 | tail -20
```
Expected: All tests pass (AuthServiceImplTest, TokenServiceImplTest, UserServiceTest, UpdateByEmailTest, UserSpecificationTest, BusinessExceptionTest, NotFoundExceptionTest).

- [ ] **Step 2: Run `api` tests (web-layer)**

```bash
./mvnw test -pl api -q 2>&1 | tail -20
```
Expected: All tests pass (AuthApiTest, UserApiTest, GlobalExceptionHandlerTest, GlobalExceptionLoggingResolverTest, ApiApplicationTests, ModulithVerificationTest).

- [ ] **Step 3: Run `worker` tests (none yet)**

```bash
./mvnw test -pl worker -q 2>&1 | tail -10
```
Expected: `BUILD SUCCESS` (no tests, just compilation check).

- [ ] **Step 4: Full project build**

```bash
./mvnw clean verify -q 2>&1 | tail -20
```
Expected: `BUILD SUCCESS` across all 4 modules (app, api, worker, e2e).

---

## Commit

- [ ] **Step 1: Commit the full split**

```bash
git add pom.xml app/pom.xml api/ worker/ e2e/pom.xml
git rm app/src/main/java/nst/wms/WmsApplication.java
git rm -r app/src/main/java/nst/wms/auth/presentation/
git rm app/src/main/java/nst/wms/auth/infrastructure/SecurityConfig.java
git rm app/src/main/java/nst/wms/auth/infrastructure/OpenApiConfig.java
git rm app/src/main/java/nst/wms/user/presentation/UserController.java
git rm -r app/src/main/java/nst/wms/user/presentation/dto/
git rm app/src/main/java/nst/wms/common/error/GlobalExceptionHandler.java
git rm app/src/main/java/nst/wms/common/error/GlobalExceptionLoggingResolver.java
git rm app/src/main/java/nst/wms/common/api/ErrorResponse.java
git rm app/src/test/java/nst/wms/WmsApplicationTests.java
git rm app/src/test/java/nst/wms/ModulithVerificationTest.java
git rm app/src/test/java/nst/wms/auth/presentation/AuthApiTest.java
git rm app/src/test/java/nst/wms/user/presentation/UserApiTest.java
git rm app/src/test/java/nst/wms/common/error/GlobalExceptionHandlerTest.java
git rm app/src/test/java/nst/wms/common/error/GlobalExceptionLoggingResolverTest.java
git commit -m "feat: split app module into app (library), api (web), worker (background)

- app: stripped of web dependencies, becomes shared domain library
- api: new Spring Boot module with controllers, security, swagger
- worker: new Spring Boot module scaffolding for future background tasks
- e2e: updated to depend on wms-api instead of app"
```
