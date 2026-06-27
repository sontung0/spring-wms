package nst.wms.common.error;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.read.ListAppender;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

import static org.assertj.core.api.Assertions.assertThat;

class GlobalExceptionLoggingResolverTest {

    private GlobalExceptionLoggingResolver resolver;
    private ListAppender<ILoggingEvent> listAppender;
    private Logger logger;

    @BeforeEach
    void setUp() {
        resolver = new GlobalExceptionLoggingResolver();
        
        logger = (Logger) LoggerFactory.getLogger(GlobalExceptionLoggingResolver.class);
        listAppender = new ListAppender<>();
        listAppender.start();
        logger.addAppender(listAppender);
    }

    @Test
    void shouldNotLogBusinessException() {
        // given
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/users/1");
        HttpServletResponse response = new MockHttpServletResponse();
        TestBusinessException businessException = new TestBusinessException("Business error");

        // when
        ModelAndView result = resolver.resolveException(request, response, null, businessException);

        // then
        assertThat(result).isNull();
        assertThat(listAppender.list).isEmpty();
    }

    @Test
    void shouldLogUnexpectedExceptionAtErrorLevel() {
        // given
        HttpServletRequest request = new MockHttpServletRequest("POST", "/api/users");
        HttpServletResponse response = new MockHttpServletResponse();
        RuntimeException unexpectedException = new RuntimeException("Unexpected error");

        // when
        ModelAndView result = resolver.resolveException(request, response, null, unexpectedException);

        // then
        assertThat(result).isNull();
        assertThat(listAppender.list).hasSize(1);
        
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getLevel()).isEqualTo(ch.qos.logback.classic.Level.ERROR);
        assertThat(logEvent.getFormattedMessage()).contains("POST").contains("/api/users");
        assertThat(logEvent.getThrowableProxy()).isNotNull();
        assertThat(logEvent.getThrowableProxy().getMessage()).isEqualTo("Unexpected error");
    }

    @Test
    void shouldLogExceptionWithStackTrace() {
        // given
        HttpServletRequest request = new MockHttpServletRequest("GET", "/api/test");
        HttpServletResponse response = new MockHttpServletResponse();
        RuntimeException exceptionWithCause = new RuntimeException("Outer error", 
            new IllegalStateException("Inner cause"));

        // when
        resolver.resolveException(request, response, null, exceptionWithCause);

        // then
        assertThat(listAppender.list).hasSize(1);
        ILoggingEvent logEvent = listAppender.list.get(0);
        assertThat(logEvent.getThrowableProxy().getCause()).isNotNull();
        assertThat(logEvent.getThrowableProxy().getCause().getMessage()).isEqualTo("Inner cause");
    }

    /**
     * Test implementation of BusinessException for testing purposes.
     */
    private static class TestBusinessException extends RuntimeException implements BusinessException {
        public TestBusinessException(String message) {
            super(message);
        }
    }
}
