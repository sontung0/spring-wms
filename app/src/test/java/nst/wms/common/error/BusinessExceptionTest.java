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
