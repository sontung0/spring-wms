# 0012-connecting-to-multiple-systems.md

**Date:** 2026-07-14  
**Lesson:** 0012 — Connecting to Multiple Systems  
**Phase:** Binder Abstraction (Phase 3)

## What Was Taught

- By default, only one instance of each binder type is created, shared via Spring Boot auto-configuration.
- To connect to multiple instances of the *same* broker type, define multiple named binder configurations under `spring.cloud.stream.binders.<name>`, each with its own `type` and `environment`.
- Turning on explicit binder configuration disables default binder auto-configuration entirely — all binders in use must then be explicitly declared.
- The `environment` block accepts arbitrary Spring Boot properties per binder (e.g., `spring.main.sources` to override auto-configured beans, or `spring.profiles.active` for a binder-specific profile).
- The `defaultCandidate=false` flag lets a named binder configuration exist independently of the default binder configuration process (useful for frameworks wrapping Spring Cloud Stream).

## Key Insights Captured

- User answered Lesson 0011's assessment correctly (all 4/4) before requesting to proceed — first assessment resolved in this batch of Phase 3 lessons; 3 lessons (0008, 0009, 0010) still have pending unanswered assessments.
- This lesson completes the distinction: Lesson 0011 = same binding set, different binder *types*; Lesson 0012 = same binder *type*, different broker *instances*.

## Zone of Proximal Development (Current)

The user understands connecting to multiple instances of the same broker type. Last topic in Phase 3 is customizing binder behavior in these multi-binder setups.

**Next resource:** [Customizing binders in multi binder applications](https://docs.spring.io/spring-cloud-stream/reference/spring-cloud-stream/binder-customizer.html)

## Status

Lesson completed. Awaiting user answers to comprehensive assessments for Lessons 0008, 0009, 0010, and 0012.
