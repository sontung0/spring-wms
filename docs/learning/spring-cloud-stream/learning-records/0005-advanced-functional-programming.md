# 0005-advanced-functional-programming.md

**Date:** 2026-07-13  
**Lesson:** 0005 — Advanced Functional Programming  
**Phase:** Programming Model (Phase 2)

## What Was Taught

- **Functional Composition:** Using the `|` operator in `spring.cloud.function.definition` to chain multiple functions together (e.g., `enrich|process`).
- **Multiple Functions:** Using the `;` operator to tell the framework to bind multiple independent functions in the same application (e.g., `processOrders;processReturns`).
- **Reactive Functions:** Using Project Reactor's `Flux` and `Mono` in function signatures for reactive stream processing.
- **Batch Processing:** Enabling `batch-mode=true` and changing the function signature to accept a `List<T>` to process messages in chunks.

## Key Insights Captured

- The user correctly identified that Lesson 0004 skipped significant portions of the documentation. This shows high engagement and a desire for comprehensive understanding.
- Splitting the "Producing and Consuming Messages" documentation into two lessons (Basic vs Advanced) was necessary to respect the user's working memory limits, but it should have been explicitly stated in Lesson 0004.
- Functional composition is a powerful way to implement the "Pipes and Filters" enterprise integration pattern without writing complex Spring Integration flows.

## Zone of Proximal Development (Current)

The user now has a complete picture of how to write message handlers, from simple imperative functions to complex reactive, composed, and batched functions. 

The next step is to understand how to route messages dynamically based on their content (e.g., sending VIP orders to one topic and standard orders to another).

**Next resource:** [Event Routing](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/event-routing.html)

## Status

Lesson completed. Awaiting user questions or explicit request to continue with Lesson 0006.
