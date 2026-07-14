# 0010-binder-detection.md

**Date:** 2026-07-14  
**Lesson:** 0010 — Binder Detection  
**Phase:** Binder Abstraction (Phase 3)

## What Was Taught

- Spring Cloud Stream relies on Spring Boot's auto-configuration by default to configure the binding process.
- If exactly one `Binder` implementation is found on the classpath, it's automatically selected and used — no explicit configuration needed.
- Example: adding only `spring-cloud-stream-binder-rabbit` as a dependency is sufficient to bind exclusively to RabbitMQ.
- Flagged the open question of what happens with multiple binders on the classpath — deferred to the next lesson.

## Key Insights Captured

- Ties back to Lesson 0008's claim that swapping brokers is "just a dependency change" — classpath detection is the actual mechanism that makes that true, provided only one binder is present.
- User is moving quickly through lessons without pausing for assessments yet — multiple lessons (0008, 0009, 0010) now have pending comprehensive assessments.

## Zone of Proximal Development (Current)

The user understands single-binder auto-detection. The natural next question is what happens when multiple binder implementations coexist on the classpath.

**Next resource:** [Multiple Binders on the Classpath](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/multiple-binders.html)

## Status

Lesson completed. Awaiting user answers to the comprehensive assessments for Lessons 0008, 0009, and 0010.
