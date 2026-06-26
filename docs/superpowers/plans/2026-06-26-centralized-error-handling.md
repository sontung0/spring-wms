# Centralized Error Handling Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Centralize error handling so modules don't need to implement their own exception handlers unless they require custom behavior.

**Architecture:** Common module provides fallback handlers for all unhandled exceptions via `GlobalExceptionHandler`. Modules can still override specific handlers when needed using `@RestControllerAdvice(assignableTypes = XxxController.class)`. Exceptions implement `BusinessException` marker interface with default `getCode()` method.

**Tech Stack:** Spring Boot, Spring MVC, JUnit 5, AssertJ

---

## Task 1: Add getCode() to BusinessException

**Files:**
- Test: `app/src/test/java/nst/wms/common/error/BusinessExceptionTest.java`
- Modify: `app/src/main/java/nst/wms/common/error/BusinessException.java`

- [ ] **Step 1: Write the failing test**

```java
package nst.wms.common.error;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class BusinessExceptionTest {

    @Test
    void getCode_shouldReturnClassNameWithoutExceptionSuffix() {
        // given
        BusinessException exception = new TestException();

        // when
        String code = exception.getCode();

        // then
        assertThat(code).isEqualTo("Test");
    }

    @Test
    void getCode_shouldReturnFullClassNameWhenNoExceptionSuffix() {
        // given
        BusinessException exception = new TestError();

        // when
        String code = exception.getCode();

        // then
        assertThat(code).isEqualTo("TestError");
    }

    @Test
    void getCode_shouldAllowOverride() {
        // given
        BusinessException exception = new CustomCodeException();

        // when
        String code = exception.getCode();

        // then
        assertThat(code).isEqualTo("CustomCode");
    }

    private static class TestException extends RuntimeException implements BusinessException {
    }

    private static class TestError extends RuntimeException implements BusinessException {
    }

    private static class CustomCodeException extends RuntimeException implements BusinessException {
        @Override
        public String getCode() {
            return "CustomCode";
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -Dtest=BusinessExceptionTest`
Expected: FAIL with "cannot find symbol: method getCode()"

- [ ] **Step 3: Implement getCode() in BusinessException**

```java
package nst.wms.common.error;

/**
 * Marker interface for business/domain exceptions that should not be logged.
 * 
 * <p>Exceptions implementing this interface represent expected business logic errors
 * (e.g., validation failures, not found errors) and are logged at DEBUG level or not at all.
 * 
 * <p>Unexpected exceptions (not implementing this interface) are logged at ERROR level
 * with full stack traces by {@link GlobalExceptionLoggingResolver}.
 */
public interface BusinessException {
    
    /**
     * Returns the error code for this exception.
     * 
     * <p>Default implementation returns the simple class name without the "Exception" suffix.
     * For example, {@code UserNotFoundException} returns {@code "UserNotFound"}.
     * 
     * <p>Exceptions can override this method to provide custom error codes.
     * 
     * @return the error code
     */
    default String getCode() {
        String className = this.getClass().getSimpleName();
        if (className.endsWith("Exception")) {
            return className.substring(0, className.length() - "Exception".length());
        }
        return className;
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -Dtest=BusinessExceptionTest`
Expected: PASS (3 tests)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/nst/wms/common/error/BusinessException.java
git add app/src/test/java/nst/wms/common/error/BusinessExceptionTest.java
git commit -m "feat: add default getCode() to BusinessException interface"
```

---

## Task 2: Create NotFoundException

**Files:**
- Create: `app/src/main/java/nst/wms/common/error/NotFoundException.java`
- Test: `app/src/test/java/nst/wms/common/error/NotFoundExceptionTest.java`

- [ ] **Step 1: Write the failing test**

```java
package nst.wms.common.error;

import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

class NotFoundExceptionTest {

    @Test
    void constructor_withMessage_shouldSetMessage() {
        // given
        String message = "User not found";

        // when
        NotFoundException exception = new NotFoundException(message);

        // then
        assertThat(exception.getMessage()).isEqualTo(message);
    }

    @Test
    void constructor_withId_shouldSetMessage() {
        // given
        Long id = 42L;

        // when
        NotFoundException exception = new NotFoundException(id);

        // then
        assertThat(exception.getMessage()).isEqualTo("Resource not found with id: 42");
    }

    @Test
    void getCode_shouldReturnNotFound() {
        // given
        NotFoundException exception = new NotFoundException("Test");

        // when
        String code = exception.getCode();

        // then
        assertThat(code).isEqualTo("NotFound");
    }

    @Test
    void shouldImplementBusinessException() {
        // given
        NotFoundException exception = new NotFoundException("Test");

        // when & then
        assertThat(exception).isInstanceOf(BusinessException.class);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -Dtest=NotFoundExceptionTest`
Expected: FAIL with "cannot find symbol: class NotFoundException"

- [ ] **Step 3: Implement NotFoundException**

```java
package nst.wms.common.error;

/**
 * Exception thrown when a requested resource is not found.
 * 
 * <p>This is a concrete exception that modules can throw directly without creating
 * their own "not found" exceptions. It implements {@link BusinessException} to indicate
 * this is an expected business error.
 * 
 * <p>The common error handler maps this to HTTP 404.
 */
public class NotFoundException extends RuntimeException implements BusinessException {

    public NotFoundException(String message) {
        super(message);
    }

    public NotFoundException(Long id) {
        super("Resource not found with id: " + id);
    }
}
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -Dtest=NotFoundExceptionTest`
Expected: PASS (4 tests)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/nst/wms/common/error/NotFoundException.java
git add app/src/test/java/nst/wms/common/error/NotFoundExceptionTest.java
git commit -m "feat: add NotFoundException for common not-found scenarios"
```

---

## Task 3: Create GlobalExceptionHandler

**Files:**
- Create: `app/src/main/java/nst/wms/common/error/GlobalExceptionHandler.java`
- Test: `app/src/test/java/nst/wms/common/error/GlobalExceptionHandlerTest.java`

- [ ] **Step 1: Write the failing test**

```java
package nst.wms.common.error;

import nst.wms.common.api.ErrorResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@WebMvcTest
@Import(GlobalExceptionHandler.class)
class GlobalExceptionHandlerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private GlobalExceptionHandler handler;

    @Test
    void handleNotFound_shouldReturn404() {
        // given
        NotFoundException exception = new NotFoundException(42L);

        // when
        ResponseEntity<ErrorResponse> response = handler.handleNotFound(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("NotFound");
        assertThat(response.getBody().getMessage()).isEqualTo("Resource not found with id: 42");
    }

    @Test
    void handleBusiness_shouldReturn400() {
        // given
        BusinessException exception = new TestBusinessException();

        // when
        ResponseEntity<ErrorResponse> response = handler.handleBusiness(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("TestBusiness");
        assertThat(response.getBody().getMessage()).isEqualTo("Business error");
    }

    @Test
    void handleGeneric_shouldReturn500() {
        // given
        Exception exception = new RuntimeException("Unexpected error");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleGeneric(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("Unexpected");
        assertThat(response.getBody().getMessage()).isEqualTo("An unexpected error occurred");
    }

    @Test
    void handleValidation_shouldReturn400WithFieldErrors() {
        // given
        MethodArgumentNotValidException exception = mock(MethodArgumentNotValidException.class);
        BindingResult bindingResult = mock(BindingResult.class);
        FieldError fieldError = new FieldError("object", "name", "must not be blank");
        
        when(exception.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(List.of(fieldError));
        when(fieldError.getField()).thenReturn("name");
        when(fieldError.getDefaultMessage()).thenReturn("must not be blank");

        // when
        ResponseEntity<ErrorResponse> response = handler.handleValidation(exception);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getError()).isEqualTo("ValidationFailed");
        assertThat(response.getBody().getMessage()).isEqualTo("Validation failed");
        assertThat(response.getBody().getErrors()).hasSize(1);
        assertThat(response.getBody().getErrors().get(0).getField()).isEqualTo("name");
        assertThat(response.getBody().getErrors().get(0).getMessage()).isEqualTo("must not be blank");
    }

    private static class TestBusinessException extends RuntimeException implements BusinessException {
        public TestBusinessException() {
            super("Business error");
        }
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -Dtest=GlobalExceptionHandlerTest`
Expected: FAIL with "cannot find symbol: class GlobalExceptionHandler"

- [ ] **Step 3: Implement GlobalExceptionHandler**

```java
package nst.wms.common.error;

import nst.wms.common.api.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Centralized exception handler that provides fallback handling for all exceptions.
 * 
 * <p>This handler is a global fallback. Module-specific handlers scoped via
 * {@code @RestControllerAdvice(assignableTypes = XxxController.class)} take precedence
 * for their specific exception types.
 * 
 * <p>Handler precedence:
 * <ol>
 *   <li>Module-specific handler (if defined and scoped to controller)</li>
 *   <li>This common handler (fallback for all unhandled exceptions)</li>
 * </ol>
 */
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

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -Dtest=GlobalExceptionHandlerTest`
Expected: PASS (4 tests)

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/nst/wms/common/error/GlobalExceptionHandler.java
git add app/src/test/java/nst/wms/common/error/GlobalExceptionHandlerTest.java
git commit -m "feat: add GlobalExceptionHandler for centralized error handling"
```

---

## Task 4: Update UserService to Use NotFoundException

**Files:**
- Modify: `app/src/main/java/nst/wms/user/application/UserService.java`
- Test: `app/src/test/java/nst/wms/user/application/UserServiceTest.java`

- [ ] **Step 1: Update UserServiceTest to expect NotFoundException**

Open `app/src/test/java/nst/wms/user/application/UserServiceTest.java` and replace:

```java
import nst.wms.user.domain.UserNotFoundException;
```

with:

```java
import nst.wms.common.error.NotFoundException;
```

Then replace all occurrences of:

```java
assertThrows(UserNotFoundException.class, () -> ...
```

with:

```java
assertThrows(NotFoundException.class, () -> ...
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./mvnw test -Dtest=UserServiceTest`
Expected: FAIL because UserService still throws UserNotFoundException

- [ ] **Step 3: Update UserService to throw NotFoundException**

Open `app/src/main/java/nst/wms/user/application/UserService.java` and replace:

```java
import nst.wms.user.domain.UserNotFoundException;
```

with:

```java
import nst.wms.common.error.NotFoundException;
```

Then replace all occurrences of:

```java
throw new UserNotFoundException(id);
```

with:

```java
throw new NotFoundException(id);
```

- [ ] **Step 4: Run test to verify it passes**

Run: `./mvnw test -Dtest=UserServiceTest`
Expected: PASS

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/nst/wms/user/application/UserService.java
git add app/src/test/java/nst/wms/user/application/UserServiceTest.java
git commit -m "refactor: use NotFoundException instead of UserNotFoundException"
```

---

## Task 5: Update UserApiTest Expectations

**Files:**
- Modify: `app/src/test/java/nst/wms/user/presentation/UserApiTest.java`

- [ ] **Step 1: Update error code expectation**

Open `app/src/test/java/nst/wms/user/presentation/UserApiTest.java` and find the test:

```java
@Test
void getUserById_whenNotFound_shouldReturn404() throws Exception {
    mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("UserNotFound"));
}
```

Replace `"UserNotFound"` with `"NotFound"`:

```java
@Test
void getUserById_whenNotFound_shouldReturn404() throws Exception {
    mockMvc.perform(get("/api/users/999"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.error").value("NotFound"));
}
```

- [ ] **Step 2: Run test to verify it passes**

Run: `./mvnw test -Dtest=UserApiTest`
Expected: PASS (all tests)

- [ ] **Step 3: Commit**

```bash
git add app/src/test/java/nst/wms/user/presentation/UserApiTest.java
git commit -m "test: update UserApiTest to expect NotFound error code"
```

---

## Task 6: Delete UserNotFoundException

**Files:**
- Delete: `app/src/main/java/nst/wms/user/domain/UserNotFoundException.java`

- [ ] **Step 1: Delete UserNotFoundException**

```bash
rm app/src/main/java/nst/wms/user/domain/UserNotFoundException.java
```

- [ ] **Step 2: Verify no compilation errors**

Run: `./mvnw clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "refactor: remove UserNotFoundException, use NotFoundException instead"
```

---

## Task 7: Delete UserExceptionHandler

**Files:**
- Delete: `app/src/main/java/nst/wms/user/presentation/UserExceptionHandler.java`

- [ ] **Step 1: Delete UserExceptionHandler**

```bash
rm app/src/main/java/nst/wms/user/presentation/UserExceptionHandler.java
```

- [ ] **Step 2: Verify no compilation errors**

Run: `./mvnw clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add -A
git commit -m "refactor: remove UserExceptionHandler, use GlobalExceptionHandler instead"
```

---

## Task 8: Update AuthExceptionHandler

**Files:**
- Modify: `app/src/main/java/nst/wms/auth/presentation/AuthExceptionHandler.java`

- [ ] **Step 1: Remove handleGeneric method**

Open `app/src/main/java/nst/wms/auth/presentation/AuthExceptionHandler.java` and delete the entire `handleGeneric` method:

```java
@ExceptionHandler(Exception.class)
public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
    ErrorResponse response = new ErrorResponse("Unexpected", "An unexpected error occurred");
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
}
```

The file should now only contain:
- `handleInvalidState`
- `handleIdpExchange`
- `handleUnknownProvider`

- [ ] **Step 2: Verify no compilation errors**

Run: `./mvnw clean compile`
Expected: BUILD SUCCESS

- [ ] **Step 3: Commit**

```bash
git add app/src/main/java/nst/wms/auth/presentation/AuthExceptionHandler.java
git commit -m "refactor: remove handleGeneric from AuthExceptionHandler"
```

---

## Task 9: Run All Tests

**Files:**
- All test files

- [ ] **Step 1: Run all tests**

Run: `./mvnw test`
Expected: All tests PASS

- [ ] **Step 2: Verify test count**

Check the output for the number of tests run. Expected: all existing tests pass.

- [ ] **Step 3: Commit (if any test fixes were needed)**

If you had to fix any tests during this step:

```bash
git add -A
git commit -m "test: fix test failures after centralized error handling"
```

---

## Task 10: Final Verification

- [ ] **Step 1: Verify application starts**

Run: `./mvnw spring-boot:run`
Expected: Application starts successfully

- [ ] **Step 2: Test error handling manually (optional)**

Test a few scenarios:
- GET `/api/users/999` → 404 with `"error": "NotFound"`
- POST `/api/users` with invalid data → 400 with `"error": "ValidationFailed"`
- GET `/auth/authorize?provider=UNKNOWN` → 400 with `"error": "UnknownProvider"`

- [ ] **Step 3: Final commit**

```bash
git add -A
git commit -m "feat: implement centralized error handling

- Add BusinessException.getCode() with default implementation
- Add NotFoundException for common not-found scenarios
- Add GlobalExceptionHandler as fallback for all exceptions
- Remove UserNotFoundException and UserExceptionHandler
- Update UserService to use NotFoundException
- Update AuthExceptionHandler to remove duplicate handlers
- Add coding convention for module exception handlers"
```

---

## Summary

After completing all tasks:

**Created:**
- `NotFoundException` — concrete exception for not-found scenarios
- `GlobalExceptionHandler` — centralized fallback handler
- Tests for both

**Modified:**
- `BusinessException` — added `getCode()` method
- `UserService` — throws `NotFoundException` instead of `UserNotFoundException`
- `AuthExceptionHandler` — removed duplicate `handleGeneric`
- `UserApiTest` — updated error code expectation
- `UserServiceTest` — updated exception expectation

**Deleted:**
- `UserNotFoundException` — replaced by `NotFoundException`
- `UserExceptionHandler` — replaced by `GlobalExceptionHandler`

**Coding Convention:**
- Added to `.claude/CLAUDE.md`: Module exception handlers must be scoped to their controllers via `@RestControllerAdvice(assignableTypes = XxxController.class)`
