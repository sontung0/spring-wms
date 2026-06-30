package nst.wms.user.domain.events;

import nst.wms.common.event.ExternalEvent;
import nst.wms.user.domain.User;

public record UserCreatedEvent(User user) implements ExternalEvent {

    @Override
    public String getEventTarget() {
        return "users";
    }

    @Override
    public String getEventKey() {
        return user.getId().toString();
    }
}
