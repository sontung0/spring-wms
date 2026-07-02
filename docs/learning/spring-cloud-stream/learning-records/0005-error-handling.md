# Learning Record 0005: Resilience and DLQs

**Date:** 2026-07-02
**Topic:** Error Handling, Retries, and Dead Letter Queues

## Context
User has covered bindings, multi-functions, and imperative publishing. To apply this to a work/production project, they must understand how Spring handles failure natively so they don't accidentally lose data.

## Insight / Concept Covered
- **Default Behavior:** 3 retries, then silently drop the message.
- **DLQ Configuration:** Requires *binder-specific* configuration blocks (e.g., `spring.cloud.stream.kafka.bindings...`) because DLQs are a physical infrastructure concept, not just a logical Spring concept.
- **Customizing Retries:** Can be done logically via `spring.cloud.stream.bindings.<binding>.consumer.max-attempts`.

## Next Steps
Testing Spring Cloud Stream applications. How to verify this logic without spinning up a real Kafka or RabbitMQ container in CI/CD.