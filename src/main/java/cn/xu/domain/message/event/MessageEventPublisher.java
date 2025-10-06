package cn.xu.domain.message.event;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class MessageEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public MessageEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    public void publishEvent(BaseMessageEvent event) {
        applicationEventPublisher.publishEvent(event);
    }
}
