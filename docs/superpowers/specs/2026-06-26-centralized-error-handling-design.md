# Centralized Error Handling Design

**Date:** 2026-06-26  
**Status:** Draft

## Overview

Centralize error handling so modules don't need to implement their own exception handlers unless they require custom behavior. The common module provides fallback handlers for all unhandled exceptions, while modules can still override specific handlers when needed.

## Architecture

### Three-Layer Error Handling

**Layer 1: Common Module (Fallback)**
- `GlobalExceptionHandler` — centralized `@RestControllerAdvice` handling:
  - `NotFoundException` → 404
  - `MethodArgumentNotValidException` → 400 (with field-level errors)
  - `BusinessException` → 400 (generic business errors)
  - `Exception` → 500 (catch-all)
- `GlobalExceptionLoggingResolver` — unchanged, logs non-business exceptions at ERROR level

**Layer 2: Module Handlers (Optional Overrides)**
- Scoped to their controllers via `@RestControllerAdvice(assignableTypes = XxxController.class)`
- Only define handlers for exceptions requiring custom handling
- Take precedence over common handler for their specific exception types

**Layer 3: Exception Hierarchy**
- `BusinessException` — marker interface with default `getCode()` method
- `NotFoundException` — concrete exception extending `RuntimeException`, implementing `BusinessException`
- Module-specific exceptions — extend `RuntimeException`, implement `BusinessException` if needed

### Precedence Chain

1. **Module-specific handler** (if defined and scoped to controller)
2. **Common handler** (fallback for all unhandled exceptions)

Spring resolves handlers by specificity: more specific exception types win over general ones.

## Components

### 1. BusinessException Interface

**Location:** `nst.wms.common.error.BusinessException`

**Changes:**
- Add default `getCode()` method that returns exception class name without "Exception" suffix
- Example: `InvalidStateException` → `"InvalidState"`, `NotFoundException` → `"NotFound"`

```java
public interface BusinessException {
    default String getCode() {
        String className = this.getClass().getSimpleName();
        if (className.endsWith("Exception")) {
            return className.substring(0, className.length() - "Exception".length());
        }
        return className;
    }
}
```

**Rationale:** Allows exceptions to carry their own error codes without hardcoding in handlers. Exceptions can override `getCode()` if needed.

### 2. NotFoundException

**Location:** `nst.wms.common.error.NotFoundException`

**Purpose:** Concrete exception for "not found" scenarios that modules can throw directly.

```java
public class NotFoundException extends RuntimeException implements BusinessException {
    public NotFoundException(String message) {
        super(message);
    }
    
    public NotFoundException(Long id) {
        super("Resource not found with id: " + id);
    }
}
```

**Rationale:** Modules don't need to create their own "not found" exceptions. They can throw `NotFoundException` directly or create custom exceptions extending it.

### 3. GlobalExceptionHandler

**Location:** `nst.wms.common.error.GlobalExceptionHandler`

**Purpose:** Centralized fallback handler for all exceptions.

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
        ErrorResponse response = new ErrorResponse(ex.getCode(), ex.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> new ErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
                .collect(Collectors.toList());
        
        ErrorResponse response = new ErrorResponse("ValidationFailed", "Validation failed", fieldErrors);
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex) {
        ErrorResponse response = new ErrorResponse(ex.getCode(), ex.getMessage());
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        ErrorResponse response = new ErrorResponse("Unexpected", "An unexpected error occurred");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}
```

**Handler Resolution:**
- `NotFoundException` handler wins over `BusinessException` handler (more specific type)
- `BusinessException` handler catches any other `BusinessException` implementations
- `Exception` handler is the final fallback

## Migration Plan

### UserExceptionHandler

**Action:** Delete entirely

**Reason:** All handlers moved to common module:
- `handleNotFound` — `UserNotFoundException` will extend `NotFoundException`, handled by common handler
- `handleValidation` — common handler covers `MethodArgumentNotValidException`
- `handleGeneric` — common handler's catch-all covers it

### UserNotFoundException

**Action:** Delete entirely

**Reason:** Modules throw `NotFoundException` directly. No need for module-specific "not found" exception.

### AuthExceptionHandler

**Action:** Modify

**Changes:**
- Remove `handleGeneric` — common handler's catch-all covers it
- Keep `handleInvalidState`, `handleIdpExchange`, `handleUnknownProvider` — these have custom error codes and specific HTTP statuses (400, 502, 400)
- Already has `assignableTypes = AuthController.class` ✓

### Existing Exception Classes

**Action:** Update to use `NotFoundException`

**Changes:**
- Any module throwing `UserNotFoundException` → throw `NotFoundException` instead
- Update imports and references

## Testing Strategy

### Unit Tests

**GlobalExceptionHandlerTest:**
- Verify `NotFoundException` → 404 with correct `ErrorResponse`
- Verify `MethodArgumentNotValidException` → 400 with field-level errors
- Verify `BusinessException` → 400 with `getCode()` result
- Verify `Exception` → 500 with generic message

**BusinessExceptionTest:**
- Verify default `getCode()` returns class name without "Exception" suffix
- Verify custom `getCode()` override works

### Integration Tests

**Handler Precedence:**
- Verify module handlers override common handler when scoped
- Verify common handler catches exceptions not handled by modules

**Existing Tests:**
- Update `UserApiTest` — remove tests for `UserNotFoundException`, add tests for `NotFoundException`
- Update `AuthApiTest` — verify auth-specific exceptions still handled correctly

## Files to Create

1. `app/src/main/java/nst/wms/common/error/NotFoundException.java`
2. `app/src/main/java/nst/wms/common/error/GlobalExceptionHandler.java`
3. `app/src/test/java/nst/wms/common/error/GlobalExceptionHandlerTest.java`
4. `app/src/test/java/nst/wms/common/error/BusinessExceptionTest.java`

## Files to Modify

1. `app/src/main/java/nst/wms/common/error/BusinessException.java` — add `default getCode()` method
2. `app/src/main/java/nst/wms/auth/presentation/AuthExceptionHandler.java` — remove `handleGeneric`
3. `app/src/test/java/nst/wms/user/presentation/UserApiTest.java` — update exception references
4. `app/src/test/java/nst/wms/auth/presentation/AuthApiTest.java` — verify auth exceptions

## Files to Delete

1. `app/src/main/java/nst/wms/user/presentation/UserExceptionHandler.java`
2. `app/src/main/java/nst/wms/user/domain/UserNotFoundException.java`

## Coding Conventions

**Location:** `.claude/CLAUDE.md`

**Rule:** Module exception handlers must be scoped to their controllers via `@RestControllerAdvice(assignableTypes = XxxController.class)` to avoid global ambiguity and ensure proper handler precedence.

## Error Handling Flow

1. Exception thrown in controller/service
2. `GlobalExceptionLoggingResolver` logs if not `BusinessException` (returns `null` to continue)
3. Spring resolves handler:
   - Check module-specific handlers (scoped via `assignableTypes`)
   - Fall back to common `GlobalExceptionHandler`
4. Handler produces `ResponseEntity<ErrorResponse>` with appropriate HTTP status
5. Response sent to client

## Benefits

1. **Reduced duplication** — modules don't repeat common handlers (validation, not found, catch-all)
2. **Consistency** — all exceptions handled uniformly across modules
3. **Flexibility** — modules can still override specific handlers when needed
4. **Maintainability** — error handling logic centralized in one place
5. **Domain purity** — exceptions don't know about HTTP status codes, only markers

## Future Extensions

If more HTTP status codes are needed (400, 403, 409, etc.), add concrete exception classes in `common.error`:
- `BadRequestException` → 400
- `ForbiddenException` → 403
- `ConflictException` → 409

Each would extend `RuntimeException`, implement `BusinessException`, and have a corresponding handler in `GlobalExceptionHandler`.
