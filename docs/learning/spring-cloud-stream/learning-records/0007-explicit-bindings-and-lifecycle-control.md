# 0007-explicit-bindings-and-lifecycle-control.md

**Date:** 2026-07-13  
**Lesson:** 0007 — Explicit Bindings, Lifecycle Control, and Post-Processing  
**Phase:** Programming Model (Phase 2)

## What Was Taught

- **Explicit Binding Creation:** Using `spring.cloud.stream.input-bindings` and `output-bindings` to create bindings not tied to a function. These bindings do not get the `-in-0` or `-out-0` suffix.
- **Binding Lifecycle Control:** How to view, start, stop, pause, and resume bindings at runtime.
  - Via HTTP using `spring-boot-starter-actuator` and the `/actuator/bindings` endpoint.
  - Programmatically using the `BindingsLifecycleController` bean.
- **Post-Processing:** Using the `PostProcessingFunction` interface to execute logic strictly *after* a message has been successfully sent to the broker (unlike standard function composition which executes *before* sending).

## Key Insights Captured

- The user correctly pointed out that we missed several topics from Phase 2 in the `RESOURCES.md`. This lesson fills those gaps.
- Explicit bindings are crucial when using `StreamBridge` if you want to pre-configure the destination properties rather than relying on dynamic destination resolution.
- Lifecycle control is a powerful operational feature for pausing consumption during downstream outages or maintenance windows without restarting the JVM.

## Zone of Proximal Development (Current)

The user has now truly completed Phase 2 (Programming Model). They understand how to write functions, route messages, manage bindings explicitly, and control their lifecycle at runtime.

The next phase is Phase 3, which dives into the Binder Abstraction and Error Handling.

**Next resource:** [Error Handling](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/overview-error-handling.html)

## Status

Lesson completed. Phase 2 fully completed. Awaiting user answers to the comprehensive assessment or explicit request to continue with Lesson 0008.
