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
}
