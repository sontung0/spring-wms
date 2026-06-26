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
