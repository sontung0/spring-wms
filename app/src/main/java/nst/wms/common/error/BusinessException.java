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
