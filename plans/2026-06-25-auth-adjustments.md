# Auth Adjustments Implementation Plan

## Overview
Three adjustments to the authentication module:
1. Remove `provider` parameter from `/auth/callback` endpoint (already stored in state cache)
2. Add comprehensive Swagger authentication documentation
3. Refactor `UserService.updateByEmail()` to accept a data object instead of explicit parameters

---

## Task 1: Remove `provider` param from `/auth/callback`

### Current State
- [`CallbackRequest`](app/src/main/java/nst/wms/auth/presentation/dto/CallbackRequest.java:3) has `provider`, `code`, `state`
- [`AuthService.callback()`](app/src/main/java/nst/wms/auth/application/AuthService.java:7) signature: `callback(String provider, String code, String state)`
- [`AuthServiceImpl.callback()`](app/src/main/java/nst/wms/auth/application/AuthServiceImpl.java:49) already retrieves provider from state cache and ignores the passed `provider` param

### Changes Required

| File | Change |
|------|--------|
| [`CallbackRequest.java`](app/src/main/java/nst/wms/auth/presentation/dto/CallbackRequest.java) | Remove `provider` field → `record CallbackRequest(String code, String state)` |
| [`AuthService.java`](app/src/main/java/nst/wms/auth/application/AuthService.java) | Update signature → `AuthCallbackResponse callback(String code, String state)` |
| [`AuthServiceImpl.java`](app/src/main/java/nst/wms/auth/application/AuthServiceImpl.java) | Update method signature, remove unused `provider` param |
| [`AuthController.java`](app/src/main/java/nst/wms/auth/presentation/AuthController.java) | Update call → `authService.callback(request.code(), request.state())` |
| [`AuthServiceImplTest.java`](app/src/test/java/nst/wms/auth/application/AuthServiceImplTest.java) | Update test calls to remove `provider` arg |
| [`AuthApiTest.java`](app/src/test/java/nst/wms/auth/presentation/AuthApiTest.java) | Remove `provider` from request body in tests |

---

## Task 2: Add Swagger Auth Documentation

### Current State
- Springdoc dependency exists in [`pom.xml`](app/pom.xml:79)
- [`AuthController`](app/src/main/java/nst/wms/auth/presentation/AuthController.java) has basic `@Tag` and `@Operation` annotations
- [`SecurityConfig`](app/src/main/java/nst/wms/auth/infrastructure/SecurityConfig.java:31) already permits `/swagger-ui/**` and `/v3/api-docs/**`

### Changes Required

| File | Change |
|------|--------|
| [`AuthController.java`](app/src/main/java/nst/wms/auth/presentation/AuthController.java) | Add `@ApiResponses` with error cases (400, 401, 502) |
| [`AuthorizeResponse.java`](app/src/main/java/nst/wms/auth/presentation/dto/AuthorizeResponse.java) | Add `@Schema` descriptions |
| [`CallbackRequest.java`](app/src/main/java/nst/wms/auth/presentation/dto/CallbackRequest.java) | Add `@Schema` descriptions for fields |
| [`CallbackResponse.java`](app/src/main/java/nst/wms/auth/presentation/dto/CallbackResponse.java) | Add `@Schema` descriptions |
| New: [`OpenApiConfig.java`](app/src/main/java/nst/wms/auth/infrastructure/OpenApiConfig.java) | Add security scheme for JWT Bearer token |

### Swagger Annotations to Add
- `@ApiResponses` for each endpoint documenting:
  - 200: Success
  - 400: Invalid request / UnknownProvider / InvalidState (from [`AuthExceptionHandler`](app/src/main/java/nst/wms/auth/presentation/AuthExceptionHandler.java))
  - 401: Unauthorized
  - 500: Unexpected error (from `handleGeneric`)
  - 502: IdP exchange error (IdpExchangeException)
- `@Schema` on DTOs with field descriptions
- Security scheme for Bearer JWT authentication

### Error Responses from [`AuthExceptionHandler`](app/src/main/java/nst/wms/auth/presentation/AuthExceptionHandler.java)
| Exception | HTTP Status | Error Code |
|-----------|-------------|------------|
| `InvalidStateException` | 400 | `InvalidState` |
| `UnknownProviderException` | 400 | `UnknownProvider` |
| `IdpExchangeException` | 502 | `IdpError` |
| `Exception` (generic) | 500 | `Unexpected` |

---

## Task 3: Refactor `UserService.updateByEmail()` to use data object

### Current State
- [`UserService.updateByEmail()`](app/src/main/java/nst/wms/user/application/UserService.java:14) signature: `updateByEmail(String email, String name, String avatarUrl)`
- [`UserServiceImpl.updateByEmail()`](app/src/main/java/nst/wms/user/application/UserServiceImpl.java:58) implements the logic
- [`AuthServiceImpl`](app/src/main/java/nst/wms/auth/application/AuthServiceImpl.java:61) calls: `userService.updateByEmail(authUser.getEmail(), authUser.getName(), authUser.getAvatarUrl())`

### Design Decision: Simple Class with Public Properties

Using a **simple class with public properties** for:
- **Simplicity**: No boilerplate getters/setters or builder
- **Future extensibility**: Easy to add new fields (address, phone, etc.)
- **Partial updates**: Only set fields you want to update (null = skip)

### New `UserUpdateData` Class

```java
package nst.wms.user.application;

public class UserUpdateData {
    public String name;
    public String avatarUrl;
    // Future fields:
    // public String address;
    // public String phone;
}
```

### Usage Examples

```java
// Update all fields (OAuth flow)
UserUpdateData data = new UserUpdateData();
data.name = "John Doe";
data.avatarUrl = "https://example.com/avatar.jpg";
userService.updateByEmail(email, data);

// Update only address (future use case)
UserUpdateData data = new UserUpdateData();
data.address = "123 Main St";
userService.updateByEmail(email, data);

// Update only name
UserUpdateData data = new UserUpdateData();
data.name = "Jane Doe";
userService.updateByEmail(email, data);
```

### Changes Required

| File | Change |
|------|--------|
| New: [`UserUpdateData.java`](app/src/main/java/nst/wms/user/application/UserUpdateData.java) | Create simple class with public properties |
| [`UserService.java`](app/src/main/java/nst/wms/user/application/UserService.java) | Update signature → `updateByEmail(String email, UserUpdateData data)` |
| [`UserServiceImpl.java`](app/src/main/java/nst/wms/user/application/UserServiceImpl.java) | Update implementation to use `data.name` and `data.avatarUrl` |
| [`AuthServiceImpl.java`](app/src/main/java/nst/wms/auth/application/AuthServiceImpl.java) | Update call to create and populate `UserUpdateData` |
| [`UpdateByEmailTest.java`](app/src/test/java/nst/wms/user/application/UpdateByEmailTest.java) | Update tests to use new signature |
| [`AuthServiceImplTest.java`](app/src/test/java/nst/wms/auth/application/AuthServiceImplTest.java) | Update mock setup for new signature |
| [`AuthApiTest.java`](app/src/test/java/nst/wms/auth/presentation/AuthApiTest.java) | Update call if needed |

---

## Execution Order

1. **Task 3 first** - Refactor `UserService.updateByEmail()` (foundational change)
2. **Task 1 second** - Remove `provider` param from callback (depends on Task 3 being complete for clean testing)
3. **Task 3 last** - Add Swagger documentation (independent, can be done anytime)

---

## Testing Strategy

- Run `mvn test` after each task to ensure no regressions
- Verify Swagger UI loads at `/swagger-ui.html`
- Verify all auth tests pass with updated signatures
