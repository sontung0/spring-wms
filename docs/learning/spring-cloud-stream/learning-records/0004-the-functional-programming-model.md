# 0004-the-functional-programming-model.md

**Date:** 2026-07-13  
**Lesson:** 0004 — The Functional Programming Model  
**Phase:** Programming Model (Phase 2)

## What Was Taught

- The shift from legacy annotation-based programming (`@StreamListener`) to the modern functional programming model using Spring Cloud Function.
- The three core interfaces: `Function` (Processor), `Consumer` (Sink), and `Supplier` (Source).
- How `Supplier` beans are triggered automatically by a default polling mechanism (every 1 second by default).
- The strict naming convention for functional bindings: `<functionName>-in-<index>` and `<functionName>-out-<index>`.
- How to use `StreamBridge` to send arbitrary data to an output on-demand (e.g., from a REST controller), bypassing the polling mechanism of a `Supplier`.

## Key Insights Captured

- The functional model drastically reduces boilerplate. A message handler is literally just a standard Java method returning a `Function`, `Consumer`, or `Supplier`.
- The binding names are predictable and derived directly from the method name. This makes configuration straightforward but requires developers to know the convention.
- `StreamBridge` is the essential tool for bridging synchronous, imperative code (like Spring MVC controllers) with the asynchronous, event-driven world of Spring Cloud Stream.

## Zone of Proximal Development (Current)

The user now knows how to write basic message handlers and how to configure their bindings. The next step is to understand how to route messages dynamically based on their content or type, rather than just static bindings.

**Next resource:** [Event Routing](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/event-routing.html)

## Status

Lesson completed. Awaiting user questions or explicit request to continue with Lesson 0005.
