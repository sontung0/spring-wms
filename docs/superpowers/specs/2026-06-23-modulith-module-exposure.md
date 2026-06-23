# Spring Modulith Module Exposure — Design Spec

## Status

- **2026-06-23:** Approved. Derived from the User Management API design — module boundary corrections discovered during implementation.

---

## Overview

This spec addresses a `MODULITH_TYPE_REF_VIOLATION` where `UserController` (in `user.internal.presentation`) imports `ErrorResponse` from `common.api`. The root cause: Spring Modulith treats all packages as private by default — only types in packages annotated with `@NamedInterface` or within `OPEN` modules are accessible across module boundaries.

The original design assumed that only `internal/` packages are private, but Spring Modulith's actual rule is: **everything is private unless explicitly exposed**.

---

## Package Structure (Corrected)

```
src/main/java/nst/wms/
├── WmsApplication.java
│
├── user/                              ← User module
│   ├── domain/                        ← PUBLIC: @NamedInterface
│   │   ├── User.java
│   │   └── UserNotFoundException.java
│   │
│   ├── application/                   ← PUBLIC: @NamedInterface
│   │   ├── UserService.java
│   │   └── UserServiceImpl.java
│   │
│   ├── infrastructure/                ← PRIVATE (no annotation)
│   │   ├── UserRepository.java
│   │   ├── UserJpaEntity.java
│   │   ├── UserRepositoryAdapter.java
│   │   ├── UserJpaRepository.java
│   │   └── UserSpecification.java
│   │
│   └── presentation/                  ← PRIVATE (no annotation)
│       ├── UserController.java
│       ├── UserExceptionHandler.java
│       └── dto/
│           ├── CreateUserRequest.java
│           ├── UpdateUserRequest.java
│           ├── UserResponse.java
│           ├── UserSummary.java
│           ├── PageResponse.java
│           └── UserFilter.java
│
└── common/                            ← OPEN module — all packages public
    └── api/
        └── ErrorResponse.java
```

### Key changes from original design

| Change | Reason |
|--------|--------|
| Removed `internal/` wrapper | The `@NamedInterface` annotation is now the single source of truth for visibility. The `internal/` convention was redundant and added unnecessary nesting depth. Packages without `@NamedInterface` are private regardless of folder structure. |
| `common/` → OPEN module | All types in `common` are meant to be shared across modules. `@ApplicationModule(type = OPEN)` makes every package public without needing per-package annotations. |
| `user.domain` / `user.application` → `@NamedInterface` | These layers are the public API of the `user` module. Other modules should access domain entities and services but not infrastructure or presentation details. |

---

## Module Configuration

### `common` module — fully open

```java
// src/main/java/nst/wms/common/package-info.java
@org.springframework.modulith.ApplicationModule(
    type = org.springframework.modulith.ApplicationModule.Type.OPEN
)
package nst.wms.common;
```

`OPEN` is appropriate here because `common` exists solely to provide shared types. There is no private implementation to protect.

### `user` module — selective exposure via @NamedInterface

```java
// src/main/java/nst/wms/user/domain/package-info.java
@org.springframework.modulith.NamedInterface
package nst.wms.user.domain;
```

```java
// src/main/java/nst/wms/user/application/package-info.java
@org.springframework.modulith.NamedInterface
package nst.wms.user.application;
```

`@NamedInterface` without an explicit name is sufficient — Spring Modulith derives the name from the package.

---

## Access Matrix

| Consumer → Target | Access | Mechanism |
|---|---|---|
| Any module → `common.api` | ✅ Public | `common` is OPEN |
| Any module → `user.domain` | ✅ Public | `@NamedInterface` |
| Any module → `user.application` | ✅ Public | `@NamedInterface` |
| Any module → `user.infrastructure` | ❌ Private | No annotation |
| Any module → `user.presentation` | ❌ Private | No annotation |
| `user.presentation` → `common.api` | ✅ Allowed | `common` is OPEN |
| `user.presentation` → `user.domain` | ✅ Allowed (same module) | Intra-module |
| `user.presentation` → `user.application` | ✅ Allowed (same module) | Intra-module |
| `user.infrastructure` → `user.domain` | ✅ Allowed (same module) | Intra-module |
| `user.application` → `user.domain` | ✅ Allowed (same module) | Intra-module |

---

## Module Verification Test

Add a test dependency and verification test to catch future violations at build time:

```xml
<!-- pom.xml -->
<dependency>
    <groupId>org.springframework.modulith</groupId>
    <artifactId>spring-modulith-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

```java
@SpringBootTest
class ModulithVerificationTest {

    @Test
    void verifyModuleStructure() {
        ApplicationModules.of(WmsApplication.class).verify();
    }
}
```

This test scans all module boundaries and fails if any illegal reference exists. Run as part of CI.

---

## Implementation Tasks

1. **Remove `internal/` wrapper layer** — Move `presentation/` and `infrastructure/` from `internal/` up to `user/`. Update imports and package declarations.

2. **Create `common/package-info.java`** — Set `@ApplicationModule(type = OPEN)`.

3. **Create `user/domain/package-info.java`** — Add `@NamedInterface`.

4. **Create `user/application/package-info.java`** — Add `@NamedInterface`.

5. **Add Modulith test dependency** — Add `spring-modulith-starter-test` to `pom.xml`.

6. **Create `ModulithVerificationTest.java`** — Verify module structure during test phase.

---

## Verification & Success Criteria

- [ ] Compilation succeeds with no `MODULITH_TYPE_REF_VIOLATION`
- [ ] `ModulithVerificationTest.verifyModuleStructure()` passes
- [ ] All existing tests still pass
- [ ] Application starts and all endpoints work
