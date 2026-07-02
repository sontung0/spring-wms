# Learning Record 0001: Functional Paradigm Shift

**Date:** 2026-07-02
**Topic:** The Functional Model of Spring Cloud Stream

## Context
The user is experienced with messaging but learning Spring Cloud Stream for real-world projects.

## Insight / Concept Covered
Instead of using legacy annotations like `@StreamListener`, modern Spring Cloud Stream uses standard `java.util.function` interfaces (`Supplier`, `Function`, `Consumer`) to define message sources, processors, and sinks.

## Rationale
Starting with the functional paradigm prevents the user from learning the deprecated annotation model, ensuring their knowledge is immediately applicable to modern Spring Boot projects.

## Next Steps
Teach "Binders and Bindings" — how to map these plain Java functions to physical broker destinations using `application.yml`.