# Learning Record 0005: Service Activators & POJOs

**Date:** 2026-07-01
**Topic:** Connecting Integration Flows to Spring Services using POJOs

## Insights
- User understands how to pass Domain Objects (POJOs) through channels.
- User understands how to use `ServiceActivator` (`.handle(bean, methodName)`) to cleanly separate messaging infrastructure from business logic.

## Next Step
- Introduce **Message Headers and Header Enrichment**. Now that the user knows how to handle the Payload, they need to know how to handle the metadata (Headers) without modifying the original POJO.