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
