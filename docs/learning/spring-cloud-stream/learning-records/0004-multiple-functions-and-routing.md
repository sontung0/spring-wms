# Learning Record 0004: Multiple Functions and StreamBridge

**Date:** 2026-07-02
**Topic:** Multiple Functions and Routing (`StreamBridge`)

## Context
User has disabled CSS background on inline codes. The user now knows bindings and conversion, so the next logical gap in real-world application building is handling multiple streams and dynamic event publishing (imperative messaging).

## Insight / Concept Covered
- `spring.cloud.function.definition` is required when dealing with multiple functional beans to specify which ones should be bound to the broker.
- `StreamBridge` bridges the gap between imperative code (like REST endpoints or complex conditionals inside consumers) and the declarative Spring Cloud Stream bindings.

## Next Steps
Error Handling and Resilience (Retries, Dead Letter Queues/DLQ). This is critical for production readiness.