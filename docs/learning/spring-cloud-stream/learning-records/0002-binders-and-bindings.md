# Learning Record 0002: Bridging Code and Infrastructure

**Date:** 2026-07-02
**Topic:** Binders and Bindings

## Context
User understood the functional model and needed to know how those functions actually talk to a physical message broker.

## Insight / Concept Covered
- **Binder:** The dependency that implements the specific broker protocol.
- **Binding Convention:** `functionName-in-index` / `functionName-out-index`.
- **Configuration:** Mapping logical bindings to physical `destination`s and specifying the `group` in `application.yml`.
- The critical importance of the `group` property for stateful/reliable consumption.

## Next Steps
Data serialization and content types. How does Spring know how to convert a byte array from Kafka into an `OrderEvent` object?