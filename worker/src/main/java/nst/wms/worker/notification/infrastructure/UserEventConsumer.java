package nst.wms.worker.notification.infrastructure;

import nst.wms.user.domain.events.UserCreatedEvent;
import nst.wms.user.domain.events.UserUpdatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
class UserEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(UserEventConsumer.class);

    @Bean
    Consumer<UserCreatedEvent> sendUserCreatedNoti() {
        return event -> log.info(
            "Sending notification to user {} <{}> — created",
            event.user().getId(), event.user().getEmail()
        );
    }

    @Bean
    Consumer<UserUpdatedEvent> sendUserUpdatedNoti() {
        return event -> log.info(
            "Sending notification to user {} <{}> — updated",
            event.user().getId(), event.user().getEmail()
        );
    }
}
