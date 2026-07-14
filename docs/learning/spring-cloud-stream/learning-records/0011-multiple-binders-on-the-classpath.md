# 0011-multiple-binders-on-the-classpath.md

**Date:** 2026-07-14  
**Lesson:** 0011 — Multiple Binders on the Classpath  
**Phase:** Binder Abstraction (Phase 3)

## What Was Taught

- When multiple binders are on the classpath, the application must explicitly indicate which binder to use per binding — auto-detection alone can't resolve it.
- Global selection via `spring.cloud.stream.defaultBinder=<name>`.
- Per-binding selection via `spring.cloud.stream.bindings.<bindingName>.binder=<name>`.
- Example: a processor with `input`/`output` bindings reading from Kafka and writing to RabbitMQ simultaneously.
- Connected the binder "name" used in these properties back to the `META-INF/spring.binders` registration key from Lesson 0009.

## Key Insights Captured

- This directly answers the open question raised at the end of Lesson 0010.
- User continues to move quickly through lessons without pausing for assessments — now 4 pending (0008, 0009, 0010, 0011).

## Zone of Proximal Development (Current)

The user understands selecting a binder per-binding within the same broker family conceptually. Next is connecting to multiple *instances* of the same broker type (e.g., two separate Kafka clusters).

**Next resource:** [Connecting to Multiple Systems](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/multiple-systems.html)

## Status

Lesson completed. Awaiting user answers to the comprehensive assessments for Lessons 0008–0011.
