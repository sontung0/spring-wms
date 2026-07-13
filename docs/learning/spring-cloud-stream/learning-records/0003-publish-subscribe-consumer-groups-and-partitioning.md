# 0003-publish-subscribe-consumer-groups-and-partitioning.md

**Date:** 2026-07-13  
**Lesson:** 0003 — Publish-Subscribe, Consumer Groups, and Partitioning  
**Phase:** Foundations & Core Concepts (Phase 1)

## What Was Taught

- **Publish-Subscribe:** How multiple independent applications can subscribe to the same destination and receive their own copies of messages.
- **Consumer Groups:** How to scale a single application by grouping instances together. Only one instance in a group processes a given message (competing consumers).
- **Partitioning:** How to ensure related data (e.g., messages with the same sensor ID) is consistently routed to the exact same consumer instance, which is critical for stateful processing.

## Key Insights Captured

- Without consumer groups, every instance of an application would process every message, leading to duplicated work.
- Anonymous consumer groups (the default if no group is specified) act like independent subscribers. Always specify a group name for production microservices.
- Partitioning requires configuration on the producer side (to extract the partition key) and sometimes on the consumer side (to know which partition index it owns), though Kafka handles the consumer side automatically if auto-rebalance is enabled.

## Zone of Proximal Development (Current)

The user has completed Phase 1 (Foundations & Core Concepts). They understand the history, the application model, the binder abstraction, and the core messaging semantics (pub-sub, groups, partitioning).

The next logical step is to move to **Phase 2: Programming Model**, where they will learn exactly how to write the code to implement these concepts using `Supplier`, `Function`, and `Consumer`.

**Next resource:** [Programming Model](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/programming-model.html)

## Status

Lesson completed. Phase 1 completed. Awaiting user questions or explicit request to continue with Lesson 0004 (Phase 2).
