package nst.wms.common.api;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(description = "Structured error response")
public class ErrorResponse {

    @Schema(description = "Error code (exception name without 'Exception' suffix)", example = "UserNotFound")
    private String error;

    @Schema(description = "Human-readable error message", example = "User not found with id: 42")
    private String message;

    @Schema(description = "Field-level validation errors (optional)")
    private List<FieldError> errors;

    public ErrorResponse() {
    }

    public ErrorResponse(String error, String message) {
        this.error = error;
        this.message = message;
    }

    public ErrorResponse(String error, String message, List<FieldError> errors) {
        this.error = error;
        this.message = message;
        this.errors = errors;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<FieldError> getErrors() {
        return errors;
    }

    public void setErrors(List<FieldError> errors) {
        this.errors = errors;
    }

    @Schema(description = "Field-level validation error")
    public static class FieldError {

        @Schema(description = "Field name", example = "name")
        private String field;

        @Schema(description = "Error message", example = "Name must not be blank")
        private String message;

        public FieldError() {
        }

        public FieldError(String field, String message) {
            this.field = field;
            this.message = message;
        }

        public String getField() {
            return field;
        }

        public void setField(String field) {
            this.field = field;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }
    }
}
