# Notification Feature — Event-Driven Design

**Date:** 2026-06-30
**Status:** Draft

## Overview

Implement a notification feature using an event-driven architecture: when a user is created or updated, publish a domain event → externalize it to Kafka → consume it in the worker module to send a notification (logging as mock for now).

The design enforces **event ordering** per user (same Kafka partition for the same `userId`), **transactional guarantees** (Transactional Outbox pattern), and **Clean Architecture** (no infrastructure annotations in domain).

## Architecture

```
┌──────────────────────────────────────────────────────────────────────────────────┐
│                                Shared Database                                    │
│                                                                                   │
│   ┌──────────────────────────────────────────────────────────────────────────┐   │
│   │                        EVENT_PUBLICATION (outbox)                        │   │
│   │  ┌──────┬──────────────────────────────┬─────────────────────────────┐   │   │
│   │  │  id  │  event_type / serialized     │  completion_status / ...    │   │   │
│   │  ├──────┼──────────────────────────────┼─────────────────────────────┤   │   │
│   │  │  1   │  UserCreatedEvent{…}          │  COMPLETED                  │   │   │
│   │  │  2   │  UserUpdatedEvent{…}          │  INCOMPLETE                 │   │   │
│   │  └──────┴──────────────────────────────┴─────────────────────────────┘   │   │
│   └──────────────────────────────────────────────────────────────────────────┘   │
└──────────────────────────────────────────────────────────────────────────────────┘
         ▲  writes (same TX)                                  │  reads & forwards
         │                                                    ▼
┌──────────────────────────────────────┐  ┌──────────────────────────────────────────────────┐
│         api process                  │  │              worker process                       │
│  ┌────────────────────────────────┐  │  │  ┌──────────────────────────────────────────┐   │
│  │ UserService (app lib)          │  │  │  │ spring-modulith-events-kafka              │   │
│  │  @Transactional                │  │  │  │  polls outbox → sends to topic "users"   │   │
│  │  publishEvent(UserCreatedEvent │──┼──┼─▶│  with key = ue.getEventKey()             │   │
│  │  / UserUpdatedEvent)          │  │  │  │                                          │   │
│  │  (persists to outbox in same   │  │  │  └──────────────────────────────────────────┘   │
│  │   DB transaction)              │  │  │  ┌──────────────────────────────────────────┐   │
│  └────────────────────────────────┘  │  │  │ Spring Cloud Stream consumers            │   │
│                                      │  │  │  sendUserCreatedNoti / sendUserUpdatedNoti │   │
│  (no kafka dependency)               │  │  │  (mock — real sender added later)        │   │
│                                      │  │  └──────────────────────────────────────────┘   │
└──────────────────────────────────────┘  └──────────────────────────────────────────────────┘
```

### Why outbox reader must live in the worker (not api)

| Aspect | api process | worker process |
|--------|-------------|----------------|
| Writes to outbox | ✅ (api writes, same TX) | ❌ |
| Reads outbox → Kafka | ❌ (no kafka dep) | ✅ (`spring-modulith-events-kafka`) |
| EventExternalizationConfig | ❌ | ✅ (single instance → sequential outbox read → ordering preserved) |
| Notification consumer | ❌ | ✅ (Cloud Stream) |

If the externalization ran in `api`, the outbox-poller and the Kafka consumer would compete in the same process, and a multi-instance `api` deployment would cause duplicate outbox reads — breaking ordering. By isolating externalization + consumption to a **single-worker** deployment, we guarantee:

1. Single outbox poller → reads in insertion order
2. Same `userId` → same Kafka partition → order preserved per user
3. Consumer in the same process → no cross-process coordination needed

## Module: `app` (shared library) — adds

### `ExternalEvent` marker interface

**Location:** `nst.wms.common.event` — the `common` module is `@ApplicationModule(type = OPEN)`, so this type is visible to all modules.

```java
package nst.wms.common.event;

/**
 * Marker interface for domain events that should be externalized to Kafka.
 */
public interface ExternalEvent {

    /** Target Kafka topic (routing destination). */
    String getEventTarget();

    /** Message key for partitioning/ordering (e.g. userId.toString()). */
    String getEventKey();
}
```

### `UserCreatedEvent` & `UserUpdatedEvent` domain event records

**Location:** `nst.wms.user.domain.events` — `user.domain` is `@NamedInterface` (public), so `worker` can import it.

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

**Convention:** Each event receives the full `User` domain object — the consumer has access to all user fields without needing to extend the event contract later.

### `UserServiceImpl` — publishes `UserCreatedEvent` / `UserUpdatedEvent` via `ApplicationEventPublisher`

```java
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;  // ← new

    @Override
    @Transactional                                                  // ← new
    public User create(User user) {
        LocalDateTime now = LocalDateTime.now();
        user.setCreatedAt(now);
        user.setUpdatedAt(now);
        User saved = userRepository.save(user);
        eventPublisher.publishEvent(new UserCreatedEvent(saved));   // ← new
        return saved;
    }

    @Override
    @Transactional                                                  // ← new
    public User updateByEmail(String email, UserUpdateData data) {
        // existing logic (unchanged)...
        // after save:
        eventPublisher.publishEvent(new UserUpdatedEvent(saved));   // ← new
    }
}
```

**`update(Long id, …)` not annotated** — it's an admin-only operation. Can be added later if needed.

### Dependency changes

| Dependency | app | api | worker |
|---|---|---|---|
| `spring-modulith-starter-core` | ✅ (unchanged) | (transitive) | (transitive) |
| `spring-modulith-events-api` | ❌ not needed (no annotations) | — | — |
| `spring-modulith-events-kafka` | ❌ | ❌ | ✅ **new** |
| `spring-cloud-stream-binder-kafka` | ❌ | ❌ | ✅ **new** |

## Module: `worker` — new files

### `EventExternalizationConfig` — programmatic config

Uses the **type-safe** `ExternalEvent` interface — no casting, no reflection.

```java
@Configuration
class EventExternalizationConfig {

    @Bean
    EventExternalizationConfiguration eventExternalizationConfiguration() {
        return EventExternalizationConfiguration.externalizing()
            .select(EventExternalizationConfiguration.selectByType(ExternalEvent.class))
            .routeAll(event -> {
                var e = (ExternalEvent) event;      // safe: only ExternalEvent impls pass the filter
                return RoutingTarget.forTarget(e.getEventTarget()).andKey(e.getEventKey());
            })
            .build();
    }
}
```

**Why this works:**
- `selectByType(ExternalEvent.class)` — only publishes events that implement `ExternalEvent`
- `routeAll(...)` — uses the interface methods, not a per-event-type switch
- Adding a new event type = just implement `ExternalEvent`, no config change needed

### `UserEventConsumer` — Spring Cloud Stream consumer beans

```java
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

### `application.properties` — worker-specific

```properties
spring.application.name=wms-worker

# Kafka bootstrap (Modulith externalization produces to this broker)
spring.kafka.bootstrap-servers=localhost:9092

# Spring Cloud Stream function binding — two consumers, same topic "users"
spring.cloud.function.definition=sendUserCreatedNoti;sendUserUpdatedNoti
spring.cloud.stream.bindings.sendUserCreatedNoti-in-0.destination=users
spring.cloud.stream.bindings.sendUserCreatedNoti-in-0.group=notification-service
spring.cloud.stream.bindings.sendUserUpdatedNoti-in-0.destination=users
spring.cloud.stream.bindings.sendUserUpdatedNoti-in-0.group=notification-service
spring.cloud.stream.bindings.sendUserCreatedNoti-in-0.consumer.max-attempts=3
spring.cloud.stream.bindings.sendUserUpdatedNoti-in-0.consumer.max-attempts=3
spring.cloud.stream.kafka.bindings.sendUserCreatedNoti-in-0.consumer.dlq-name=users.dlq
spring.cloud.stream.kafka.bindings.sendUserUpdatedNoti-in-0.consumer.dlq-name=users.dlq

# Modulith externalization — retry incomplete publications on restart
spring.modulith.events.republish-outstanding-events-on-restart=true
```

### Dependencies — `worker/pom.xml`

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

### Dependencies — `root pom.xml`

Add Spring Cloud BOM 2024.0.1 to `dependencyManagement`:

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

## Transactional Guarantees (Transactional Outbox)

```
┌─ Business Transaction (@Transactional) ───────────────────┐
│  1. UserService.create()                                   │
│  2. ApplicationEventPublisher.publishEvent(UserCreatedEvent│
│     / UserUpdatedEvent)                                     │
│  3. Modulith writes event to EVENT_PUBLICATION table        │
│  4. Transaction commits ✅                                 │
└────────────────────────────────────────────────────────────┘
                              │
                              ▼  (only after commit)
┌─ Externalization (REQUIRES_NEW, async) ───────────────────┐
│  5. Modulith polls outbox → picks up event                 │
│  6. Sends to Kafka topic "users" with key = userId         │
│  7. On success → marks EVENT_PUBLICATION as COMPLETED      │
│  8. On failure → stays INCOMPLETE (retried automatically)  │
└────────────────────────────────────────────────────────────┘
```

| Concern | Guarantee |
|---------|-----------|
| Event published if DB TX rolls back? | ❌ No — rolls back together with business TX |
| Event published before DB commit? | ❌ No — only after commit (TransactionalEventListener) |
| Kafka broker is down? | ✅ Event stays in outbox, auto-retried |
| App crashes after commit but before Kafka? | ✅ Outbox survives, retried on restart (`republish-outstanding-events-on-restart=true`) |
| Listener failure / processing error? | ✅ Tracked as INCOMPLETE, eligible for retry |
| Event ordering per user? | ✅ Single worker process → `userId` as Kafka key → same partition |

## Adding Future Event Types

To add a new externalized event (e.g. `OrderCreated`):

1. Create the event record implementing `ExternalEvent`:
   ```java
   public record OrderCreated(Order order) implements ExternalEvent {
       @Override public String getEventTarget() { return "orders"; }
       @Override public String getEventKey() { return order.getUserId().toString(); }
   }
   ```
2. `publishEvent(...)` in the service
3. Add new `Consumer<OrderCreated>` beans in the worker (or another module)

**No changes to `EventExternalizationConfig`** — it automatically routes any `ExternalEvent` implementation.

## Open Questions

- [ ] DLQ retry policy: backoff interval? max retries before human intervention?
- [ ] Monitoring: expose incomplete publication count as a metric?
- [ ] Notification content: what fields beyond userId, email will be needed? Should the event carry a different view of the data?
