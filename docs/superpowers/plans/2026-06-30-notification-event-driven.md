# Notification Feature — Event-Driven Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Implement event-driven notification: when a user is created or updated, publish a domain event, externalize it to Kafka, and consume it in the worker to log a notification.

**Architecture:** Three-tier event pipeline: (1) `UserService` publishes `UserCreatedEvent`/`UserUpdatedEvent` via `ApplicationEventPublisher`, (2) `spring-modulith-events-kafka` in the worker reads the outbox and forwards to Kafka topic "users", (3) Spring Cloud Stream consumers in the worker receive the events and log notifications. The `ExternalEvent` marker interface provides type-safe routing without infrastructure annotations on domain objects.

**Tech Stack:** Spring Modulith 2.1.0, Spring Cloud Stream 2024.0.1, spring-modulith-events-kafka, Kafka

## Global Constraints

- All domain events must be in `nst.wms.user.domain.events` package (under `@NamedInterface`)
- All infrastructure config must be in the `worker` module, not in `app` or `api`
- Use `ExternalEvent` marker interface for type-safe routing — no `@Externalized` annotation
- Use `@Transactional` on service methods that publish events
- Use `@RequiredArgsConstructor` for constructor injection
- Use `@Configuration` classes for bean definitions
- All new code must have tests

---

## File Structure

### New files

| File | Responsibility |
|------|----------------|
| `app/src/main/java/nst/wms/common/event/ExternalEvent.java` | Marker interface for events that should be externalized |
| `app/src/main/java/nst/wms/user/domain/events/UserCreatedEvent.java` | Domain event for user creation |
| `app/src/main/java/nst/wms/user/domain/events/UserUpdatedEvent.java` | Domain event for user update |
| `worker/src/main/java/nst/wms/worker/config/EventExternalizationConfig.java` | Programmatic externalization config |
| `worker/src/main/java/nst/wms/worker/config/UserEventConsumer.java` | Spring Cloud Stream consumer beans |
| `worker/src/test/java/nst/wms/worker/config/UserEventConsumerTest.java` | Unit tests for consumer beans |

### Modified files

| File | Change |
|------|--------|
| `app/src/main/java/nst/wms/user/application/UserServiceImpl.java` | Add `ApplicationEventPublisher` field, publish events in `create()` and `updateByEmail()` |
| `app/src/test/java/nst/wms/user/application/UserServiceTest.java` | Add test for event publishing on `create()` |
| `app/src/test/java/nst/wms/user/application/UpdateByEmailTest.java` | Add test for event publishing on `updateByEmail()` |
| `worker/pom.xml` | Add `spring-modulith-events-kafka` and `spring-cloud-stream-binder-kafka` |
| `worker/src/main/resources/application.properties` | Add Kafka and Cloud Stream configuration |
| `pom.xml` (root) | Add Spring Cloud BOM to `dependencyManagement` |

---

## Tasks

### Task 1: Create `ExternalEvent` marker interface

**Files:**
- Create: `app/src/main/java/nst/wms/common/event/ExternalEvent.java`

**Interfaces:**
- Produces: `nst.wms.common.event.ExternalEvent` — interface with `getEventTarget(): String` and `getEventKey(): String`

- [ ] **Step 1: Create the interface**

Create `app/src/main/java/nst/wms/common/event/ExternalEvent.java`:

```java
package nst.wms.common.event;

public interface ExternalEvent {

    /** Target topic for routing (e.g. "users"). */
    String getEventTarget();

    /** Message key for partitioning/ordering (e.g. userId.toString()). */
    String getEventKey();
}
```

- [ ] **Step 2: Commit**

```bash
git add app/src/main/java/nst/wms/common/event/ExternalEvent.java
git commit -m "feat: add ExternalEvent marker interface for event externalization"
```

---

### Task 2: Create `UserCreatedEvent` and `UserUpdatedEvent` domain events

**Files:**
- Create: `app/src/main/java/nst/wms/user/domain/events/UserCreatedEvent.java`
- Create: `app/src/main/java/nst/wms/user/domain/events/UserUpdatedEvent.java`

**Interfaces:**
- Consumes: `nst.wms.common.event.ExternalEvent`
- Produces: `nst.wms.user.domain.events.UserCreatedEvent` and `nst.wms.user.domain.events.UserUpdatedEvent` — both implement `ExternalEvent`, both carry the full `User` domain object

- [ ] **Step 1: Create `UserCreatedEvent`**

Create `app/src/main/java/nst/wms/user/domain/events/UserCreatedEvent.java`:

```java
package nst.wms.user.domain.events;

import nst.wms.common.event.ExternalEvent;
import nst.wms.user.domain.User;

public record UserCreatedEvent(User user) implements ExternalEvent {

    @Override
    public String getEventTarget() {
        return "users";
    }

    @Override
    public String getEventKey() {
        return user.getId().toString();
    }
}
```

- [ ] **Step 2: Create `UserUpdatedEvent`**

Create `app/src/main/java/nst/wms/user/domain/events/UserUpdatedEvent.java`:

```java
package nst.wms.user.domain.events;

import nst.wms.common.event.ExternalEvent;
import nst.wms.user.domain.User;

public record UserUpdatedEvent(User user) implements ExternalEvent {

    @Override
    public String getEventTarget() {
        return "users";
    }

    @Override
    public String getEventKey() {
        return user.getId().toString();
    }
}
```

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/nst/wms/user/domain/events/
git commit -m "feat: add UserCreatedEvent and UserUpdatedEvent domain events"
```

---

### Task 3: Update `UserServiceImpl` to publish events

**Files:**
- Modify: `app/src/main/java/nst/wms/user/application/UserServiceImpl.java`
- Modify: `app/src/test/java/nst/wms/user/application/UserServiceTest.java`
- Modify: `app/src/test/java/nst/wms/user/application/UpdateByEmailTest.java`

**Interfaces:**
- Consumes: `ApplicationEventPublisher` (Spring Framework), `UserCreatedEvent`, `UserUpdatedEvent`
- Produces: `UserService.create()` now publishes `UserCreatedEvent`; `UserService.updateByEmail()` now publishes `UserUpdatedEvent`

- [ ] **Step 1: Add `ApplicationEventPublisher` field and `@Transactional` to `UserServiceImpl`**

In `app/src/main/java/nst/wms/user/application/UserServiceImpl.java`:

Add imports:
```java
import nst.wms.user.domain.events.UserCreatedEvent;
import nst.wms.user.domain.events.UserUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
```

Add `ApplicationEventPublisher` field:
```java
private final UserRepository userRepository;
private final ApplicationEventPublisher eventPublisher;  // new
```

Update `create()`:
```java
@Override
@Transactional
public User create(User user) {
    LocalDateTime now = LocalDateTime.now();
    user.setCreatedAt(now);
    user.setUpdatedAt(now);
    User saved = userRepository.save(user);
    eventPublisher.publishEvent(new UserCreatedEvent(saved));
    return saved;
}
```

Update `updateByEmail()` — add `@Transactional` and publish event after save:

```java
@Override
@Transactional
public User updateByEmail(String email, UserUpdateData data) {
    return userRepository.findByEmail(email)
            .map(existing -> {
                if (data.name != null) {
                    existing.setName(data.name);
                }
                if (data.avatarUrl != null) {
                    existing.setAvatarUrl(data.avatarUrl);
                }
                existing.setUpdatedAt(LocalDateTime.now());
                User saved = userRepository.save(existing);
                eventPublisher.publishEvent(new UserUpdatedEvent(saved));
                return saved;
            })
            .orElseGet(() -> {
                User newUser = new User();
                newUser.setEmail(email);
                newUser.setName(data.name);
                newUser.setAvatarUrl(data.avatarUrl);
                LocalDateTime now = LocalDateTime.now();
                newUser.setCreatedAt(now);
                newUser.setUpdatedAt(now);
                User saved = userRepository.save(newUser);
                eventPublisher.publishEvent(new UserCreatedEvent(saved));
                return saved;
            });
}
```

- [ ] **Step 2: Update `UserServiceTest` to verify event publishing on create**

In `app/src/test/java/nst/wms/user/application/UserServiceTest.java`:

Add imports:
```java
import nst.wms.user.domain.events.UserCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
```

Add `@Mock` field:
```java
@Mock
private ApplicationEventPublisher eventPublisher;
```

Add test to `create_shouldSetTimestampsAndCallRepository`:
```java
@Test
void create_shouldPublishUserCreatedEvent() {
    User user = new User();
    user.setName("Jane");

    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
        User saved = invocation.getArgument(0);
        saved.setId(2L);
        return saved;
    });

    User result = userService.create(user);

    assertNotNull(result.getId());
    verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
}
```

- [ ] **Step 3: Update `UpdateByEmailTest` to verify event publishing**

In `app/src/test/java/nst/wms/user/application/UpdateByEmailTest.java`:

Add imports:
```java
import nst.wms.user.domain.events.UserCreatedEvent;
import nst.wms.user.domain.events.UserUpdatedEvent;
import org.springframework.context.ApplicationEventPublisher;
```

Add `@Mock` field:
```java
@Mock
private ApplicationEventPublisher eventPublisher;
```

Add test for new user creation (should publish `UserCreatedEvent`):
```java
@Test
void updateByEmail_shouldPublishUserCreatedEventWhenNewUser() {
    when(userRepository.findByEmail("new@example.com")).thenReturn(Optional.empty());
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
        User saved = invocation.getArgument(0);
        saved.setId(1L);
        return saved;
    });

    UserUpdateData data = new UserUpdateData();
    data.name = "New User";
    userService.updateByEmail("new@example.com", data);

    verify(eventPublisher).publishEvent(any(UserCreatedEvent.class));
}
```

Add test for existing user update (should publish `UserUpdatedEvent`):
```java
@Test
void updateByEmail_shouldPublishUserUpdatedEventWhenExistingUser() {
    User existing = new User(1L, "Old Name", "existing@example.com", null, LocalDateTime.now(), LocalDateTime.now());
    when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existing));
    when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

    UserUpdateData data = new UserUpdateData();
    data.name = "New Name";
    userService.updateByEmail("existing@example.com", data);

    verify(eventPublisher).publishEvent(any(UserUpdatedEvent.class));
}
```

- [ ] **Step 4: Run tests to verify**

```bash
mvn test -pl app -Dtest="UserServiceTest,UpdateByEmailTest" -DfailIfNoTests=false
```

Expected: All tests pass (including new event-publishing tests).

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/nst/wms/user/application/UserServiceImpl.java
git add app/src/test/java/nst/wms/user/application/UserServiceTest.java
git add app/src/test/java/nst/wms/user/application/UpdateByEmailTest.java
git commit -m "feat: publish UserCreatedEvent/UserUpdatedEvent from UserService"
```

---

### Task 4: Add Spring Cloud BOM and worker dependencies

**Files:**
- Modify: `pom.xml` (root)
- Modify: `worker/pom.xml`

**Interfaces:**
- Produces: `spring-modulith-events-kafka` and `spring-cloud-stream-binder-kafka` available on worker classpath

- [ ] **Step 1: Add Spring Cloud BOM to root `pom.xml`**

In `pom.xml` (root), add after the `<modules>` section (or at the end before `</project>`):

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.cloud</groupId>
            <artifactId>spring-cloud-dependencies</artifactId>
            <version>2024.0.1</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

- [ ] **Step 2: Add dependencies to `worker/pom.xml`**

In `worker/pom.xml`, add after the `lombok` dependency:

```xml
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-events-kafka</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-stream-binder-kafka</artifactId>
</dependency>
```

- [ ] **Step 3: Verify compilation**

```bash
mvn compile -pl worker -am
```

Expected: BUILD SUCCESS

- [ ] **Step 4: Commit**

```bash
git add pom.xml worker/pom.xml
git commit -m "chore: add Spring Cloud BOM and worker dependencies for Kafka"
```

---

### Task 5: Create `EventExternalizationConfig` in worker

**Files:**
- Create: `worker/src/main/java/nst/wms/worker/config/EventExternalizationConfig.java`

**Interfaces:**
- Consumes: `ExternalEvent` (from app), `EventExternalizationConfiguration` (from Modulith)
- Produces: `EventExternalizationConfiguration` bean that routes all `ExternalEvent` implementations to their target topic with key

- [ ] **Step 1: Create the config class**

Create `worker/src/main/java/nst/wms/worker/config/EventExternalizationConfig.java`:

```java
package nst.wms.worker.config;

import nst.wms.common.event.ExternalEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;

@Configuration
class EventExternalizationConfig {

    @Bean
    EventExternalizationConfiguration eventExternalizationConfiguration() {
        return EventExternalizationConfiguration.externalizing()
            .select(EventExternalizationConfiguration.selectByType(ExternalEvent.class))
            .routeAll(event -> {
                var e = (ExternalEvent) event;
                return RoutingTarget.forTarget(e.getEventTarget()).andKey(e.getEventKey());
            })
            .build();
    }
}
```

- [ ] **Step 2: Verify compilation**

```bash
mvn compile -pl worker -am
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add worker/src/main/java/nst/wms/worker/config/EventExternalizationConfig.java
git commit -m "feat: add EventExternalizationConfig for ExternalEvent routing"
```

---

### Task 6: Create `UserEventConsumer` with Spring Cloud Stream beans

**Files:**
- Create: `worker/src/main/java/nst/wms/worker/config/UserEventConsumer.java`
- Create: `worker/src/test/java/nst/wms/worker/config/UserEventConsumerTest.java`

**Interfaces:**
- Consumes: `UserCreatedEvent`, `UserUpdatedEvent` (from app)
- Produces: `Consumer<UserCreatedEvent>` bean named `sendUserCreatedNoti`, `Consumer<UserUpdatedEvent>` bean named `sendUserUpdatedNoti`

- [ ] **Step 1: Write the failing test**

Create `worker/src/test/java/nst/wms/worker/config/UserEventConsumerTest.java`:

```java
package nst.wms.worker.config;

import nst.wms.user.domain.User;
import nst.wms.user.domain.events.UserCreatedEvent;
import nst.wms.user.domain.events.UserUpdatedEvent;
import org.junit.jupiter.api.Test;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserEventConsumerTest {

    private final UserEventConsumer userEventConsumer = new UserEventConsumer();

    @Test
    void sendUserCreatedNoti_shouldBeDefined() {
        Consumer<UserCreatedEvent> consumer = userEventConsumer.sendUserCreatedNoti();
        assertNotNull(consumer);
    }

    @Test
    void sendUserUpdatedNoti_shouldBeDefined() {
        Consumer<UserUpdatedEvent> consumer = userEventConsumer.sendUserUpdatedNoti();
        assertNotNull(consumer);
    }

    @Test
    void sendUserCreatedNoti_shouldNotThrow() {
        User user = new User(1L, "John", "john@example.com", null, null, null);
        UserCreatedEvent event = new UserCreatedEvent(user);
        userEventConsumer.sendUserCreatedNoti().accept(event);
    }

    @Test
    void sendUserUpdatedNoti_shouldNotThrow() {
        User user = new User(1L, "John", "john@example.com", null, null, null);
        UserUpdatedEvent event = new UserUpdatedEvent(user);
        userEventConsumer.sendUserUpdatedNoti().accept(event);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

```bash
mvn test -pl worker -Dtest="UserEventConsumerTest" -DfailIfNoTests=false
```

Expected: FAIL with "cannot find symbol UserEventConsumer"

- [ ] **Step 3: Create the consumer class**

Create `worker/src/main/java/nst/wms/worker/config/UserEventConsumer.java`:

```java
package nst.wms.worker.config;

import nst.wms.user.domain.events.UserCreatedEvent;
import nst.wms.user.domain.events.UserUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    @Bean
    Consumer<UserCreatedEvent> sendUserCreatedNoti() {
        return event -> log.info(
            "Sending notification to user {} <{}> — created",
            event.user().getId(), event.user().getEmail()
        );
    }

    @Bean
    Consumer<UserUpdatedEvent> sendUserUpdatedNoti() {
        return event -> log.info(
            "Sending notification to user {} <{}> — updated",
            event.user().getId(), event.user().getEmail()
        );
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

```bash
mvn test -pl worker -Dtest="UserEventConsumerTest" -DfailIfNoTests=false
```

Expected: PASS (4/4)

- [ ] **Step 5: Commit**

```bash
git add worker/src/main/java/nst/wms/worker/config/UserEventConsumer.java
git add worker/src/test/java/nst/wms/worker/config/UserEventConsumerTest.java
git commit -m "feat: add UserEventConsumer with sendUserCreatedNoti and sendUserUpdatedNoti"
```

---

### Task 7: Add worker configuration for Kafka and Cloud Stream

**Files:**
- Modify: `worker/src/main/resources/application.properties`

**Interfaces:**
- Consumes: `sendUserCreatedNoti` and `sendUserUpdatedNoti` bean names (from Task 6)

- [ ] **Step 1: Add properties to `worker/src/main/resources/application.properties`**

Read the current file first to see if there's existing content:

```bash
cat worker/src/main/resources/application.properties
```

Append the following to `worker/src/main/resources/application.properties`:

```properties
# Kafka bootstrap (Modulith externalization produces to this broker)
spring.kafka.bootstrap-servers=localhost:9092

# Spring Cloud Stream function binding
spring.cloud.function.definition=sendUserCreatedNoti;sendUserUpdatedNoti
spring.cloud.stream.bindings.sendUserCreatedNoti-in-0.destination=users
spring.cloud.stream.bindings.sendUserCreatedNoti-in-0.group=notification-service
spring.cloud.stream.bindings.sendUserCreatedNoti-in-0.consumer.max-attempts=3
spring.cloud.stream.kafka.bindings.sendUserCreatedNoti-in-0.consumer.dlq-name=users.dlq
spring.cloud.stream.bindings.sendUserUpdatedNoti-in-0.destination=users
spring.cloud.stream.bindings.sendUserUpdatedNoti-in-0.group=notification-service
spring.cloud.stream.bindings.sendUserUpdatedNoti-in-0.consumer.max-attempts=3
spring.cloud.stream.kafka.bindings.sendUserUpdatedNoti-in-0.consumer.dlq-name=users.dlq

# Modulith externalization — retry incomplete publications on restart
spring.modulith.events.republish-outstanding-events-on-restart=true
```

- [ ] **Step 2: Verify compilation**

```bash
mvn compile -pl worker -am
```

Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add worker/src/main/resources/application.properties
git commit -m "chore: add Kafka and Cloud Stream configuration for worker"
```

---

## Self-Checklist

- [ ] All domain events are in `user.domain.events` (under `@NamedInterface`)
- [ ] `ExternalEvent` is in `common` (OPEN module, accessible everywhere)
- [ ] `EventExternalizationConfig` is in `worker` module, not `app` or `api`
- [ ] No `@Externalized` annotation used anywhere
- [ ] `@Transactional` on `create()` and `updateByEmail()`
- [ ] `ApplicationEventPublisher` injected via constructor (no `@Autowired`)
- [ ] Spring Cloud BOM in root `pom.xml` dependencyManagement
- [ ] `spring-modulith-events-kafka` and `spring-cloud-stream-binder-kafka` in worker
- [ ] Cloud Stream function definition includes both consumer beans
- [ ] Both consumers bind to the same topic `"users"` with the same group
- [ ] DLQ configured for both consumers
- [ ] `republish-outstanding-events-on-restart` enabled
- [ ] All new code has tests
