# Learning Record 0006: Message Headers & Enrichment

**Date:** 2026-07-01
**Topic:** Message Headers and `@Header` injection

## Insights
- User understands the separation of concerns between Payload (domain object) and Headers (metadata).
- User successfully executed and understood `HeaderEnricher` (`.enrichHeaders`) and injecting headers into Service Activators.

## Next Step
- Introduce **Filters**. Now that we can route and enrich, we need to know how to discard or drop messages that shouldn't proceed (e.g., invalid data, duplicated data, or logic-based dropping).