package cn.xu.domain.like.event;

import cn.xu.domain.like.model.LikeType;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LikeEventPublisher {

    private final ApplicationEventPublisher publisher;

    public LikeEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void publish(Long userId, Long targetId, LikeType targetType, Boolean likeStatus) {
        LikeEvent event = LikeEvent.builder()
                .userId(userId)
                .targetId(targetId)
                .type(targetType)
                .status(likeStatus)
                .createTime(LocalDateTime.now())
                .build();

        publisher.publishEvent(event);
    }
}
