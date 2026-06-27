package nst.wms.common.error;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

/**
 * Centralized exception logging resolver that intercepts all exceptions before
 * module-specific {@link org.springframework.web.bind.annotation.ExceptionHandler} methods.
 * 
 * <p>Logging behavior:
 * <ul>
 *   <li>{@link BusinessException} - not logged (expected business errors)</li>
 *   <li>All other exceptions - logged at ERROR level with full stack trace</li>
 * </ul>
 * 
 * <p>This resolver returns {@code null} to allow downstream exception handlers
 * to produce the actual HTTP response.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionLoggingResolver implements HandlerExceptionResolver {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionLoggingResolver.class);

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response,
                                         Object handler,
                                         Exception ex) {
        if (!(ex instanceof BusinessException)) {
            log.error("Unhandled exception for {} {}", request.getMethod(), request.getRequestURI(), ex);
        }
        return null;
    }
}
