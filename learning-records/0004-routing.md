# Learning Record 0004: Routing

**Date:** 2026-07-01
**Topic:** Message Routing

## Insights
- User successfully executed and understood payload-based routing.
- User grasped the concept of `defaultOutputToParentFlow()` for unmapped routes.

## Next Step
- Introduce **Service Activators** and **POJOs**. So far we've been passing `String` payloads and writing inline lambdas (`.handle(m -> ...)`). In a real Spring app, we pass domain objects (like an `Order`) and delegate to existing Spring `@Service` beans.