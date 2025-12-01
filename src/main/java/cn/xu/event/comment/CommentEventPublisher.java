package cn.xu.event.comment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 评论事件发布器
 * 使用Spring Event机制发布评论相关事件
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventPublisher {

    private final ApplicationEventPublisher eventPublisher;

    /**
     * 发布统一的评论事件
     */
    public void publishEvent(CommentEvent event) {
        if (event.getOccurredTime() == null) {
            event.setOccurredTime(LocalDateTime.now());
        }
        log.debug("发布评论事件: action={}, commentId={}", event.getAction(), event.getCommentId());
        eventPublisher.publishEvent(event);
    }
}