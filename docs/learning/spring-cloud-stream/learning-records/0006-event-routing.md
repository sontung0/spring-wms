# 0006-event-routing.md

**Date:** 2026-07-13  
**Lesson:** 0006 — Event Routing  
**Phase:** Programming Model (Phase 2)

## What Was Taught

- **Inbound Routing (Routing TO Consumer):** How to use the built-in `RoutingFunction` to dynamically route incoming messages from a single topic to different functions.
- Enabling routing via `spring.cloud.stream.function.routing.enabled=true`.
- The special input binding name: `functionRouter-in-0`.
- Using SpEL (`spring.cloud.function.routing-expression`) vs Java (`MessageRoutingCallback`) to determine the target function.
- **Outbound Routing (Routing FROM Consumer):** How to dynamically determine the output destination by setting the `spring.cloud.stream.sendto.destination` header on the returned `Message`.

## Key Insights Captured

- The `RoutingFunction` acts as a hidden dispatcher. You bind the broker to the router (`functionRouter-in-0`), and the router invokes your actual business functions.
- `MessageRoutingCallback` is the preferred approach when routing logic is too complex for a simple SpEL expression.
- Outbound routing via headers is a declarative alternative to using `StreamBridge` programmatically.

## Zone of Proximal Development (Current)

The user has mastered the programming model, including advanced functional features and dynamic routing. The next critical topic for building production-ready systems is understanding how to handle failures gracefully.

**Next resource:** [Error Handling](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-error-handling.html)

## Status

Lesson completed. Awaiting user answers to the comprehensive assessment or explicit request to continue with Lesson 0007.
