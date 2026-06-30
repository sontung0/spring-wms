package nst.wms.worker.notification.infrastructure;

import nst.wms.user.domain.User;
import nst.wms.user.domain.events.UserCreatedEvent;
import nst.wms.user.domain.events.UserUpdatedEvent;
import org.junit.jupiter.api.Test;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class UserEventConsumerTest {

    private final UserEventConsumer userEventConsumer = new UserEventConsumer();

    @Test
    void sendUserCreatedNoti_shouldBeDefined() {
        Consumer<UserCreatedEvent> consumer = userEventConsumer.sendUserCreatedNoti();
        assertNotNull(consumer);
    }

    @Test
    void sendUserUpdatedNoti_shouldBeDefined() {
        Consumer<UserUpdatedEvent> consumer = userEventConsumer.sendUserUpdatedNoti();
        assertNotNull(consumer);
    }

    @Test
    void sendUserCreatedNoti_shouldNotThrow() {
        User user = new User(1L, "John", "john@example.com", null, null, null);
        UserCreatedEvent event = new UserCreatedEvent(user);
        userEventConsumer.sendUserCreatedNoti().accept(event);
    }

    @Test
    void sendUserUpdatedNoti_shouldNotThrow() {
        User user = new User(1L, "John", "john@example.com", null, null, null);
        UserUpdatedEvent event = new UserUpdatedEvent(user);
        userEventConsumer.sendUserUpdatedNoti().accept(event);
    }
}
