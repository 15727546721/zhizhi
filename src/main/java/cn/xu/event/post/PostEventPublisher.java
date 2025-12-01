package cn.xu.event.post;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * 帖子事件发布器
 * 使用Spring Event机制发布帖子相关事件
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布统一的帖子事件
     */
    public void publishEvent(PostEvent event) {
        if (event.getOccurredTime() == null) {
            event.setOccurredTime(LocalDateTime.now());
        }
        log.debug("发布帖子事件: eventType={}, postId={}", event.getEventType(), event.getPostId());
        eventPublisher.publishEvent(event);
    }
}