package cn.xu.domain.like.event;

import cn.xu.domain.like.model.LikeType;
import cn.xu.infrastructure.event.disruptor.EventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LikeEventPublisher {

    private final EventPublisher eventPublisher;

    public LikeEventPublisher(EventPublisher eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

    public void publish(Long userId, Long targetId, LikeType targetType, Boolean likeStatus) {
        LikeEvent event = LikeEvent.builder()
                .userId(userId)
                .targetId(targetId)
                .type(targetType)
                .status(likeStatus)
                .createTime(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event, "LikeEvent");
    }
    
    /**
     * 发布点赞事件的重载方法，支持更多参数
     */
    public void publish(Long userId, Long targetId, LikeType targetType, Boolean likeStatus, String targetTitle) {
        LikeEvent event = LikeEvent.builder()
                .userId(userId)
                .targetId(targetId)
                .type(targetType)
                .status(likeStatus)
                .createTime(LocalDateTime.now())
                .build();

        eventPublisher.publishEvent(event, "LikeEvent");
    }
}