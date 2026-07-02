# Learning Record 0003: Payloads and Headers

**Date:** 2026-07-02
**Topic:** Message Conversion and the `Message<T>` wrapper.

## Context
User understood bindings and now needed to know how byte arrays translate to Java POJOs and how to interact with message metadata.

## Insight / Concept Covered
- **Automatic Conversion:** Spring uses `MessageConverter` (defaulting to Jackson for JSON) to seamlessly handle serialization based on the generic types of the `Function/Consumer`.
- **`content-type`:** Can be explicitly set on the binding.
- **The `Message<T>` interface:** By changing the function signature to accept or return `Message<T>` instead of just `T`, the developer gains access to read or write headers (metadata).

## Next Steps
Advanced routing (handling multiple functions in one app, or dynamically choosing an output destination).