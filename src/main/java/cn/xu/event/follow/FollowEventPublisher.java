package cn.xu.event.follow;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FollowEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publish(FollowEvent event) {
        publisher.publishEvent(event);
    }
}
