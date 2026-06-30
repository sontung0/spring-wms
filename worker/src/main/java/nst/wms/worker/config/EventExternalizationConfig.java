package nst.wms.worker.config;

import nst.wms.common.event.ExternalEvent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.modulith.events.EventExternalizationConfiguration;
import org.springframework.modulith.events.RoutingTarget;

@Configuration
class EventExternalizationConfig {

    @Bean
    EventExternalizationConfiguration eventExternalizationConfiguration() {
        return EventExternalizationConfiguration.externalizing()
            .selectByType(ExternalEvent.class)
            .routeAll(event -> {
                var e = (ExternalEvent) event;
                return RoutingTarget.forTarget(e.getEventTarget()).andKey(e.getEventKey());
            })
            .build();
    }
}
