package cn.xu.domain.comment.event;

import cn.xu.infrastructure.event.disruptor.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 评论领域事件发布器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CommentEventPublisher {

    private final EventPublisher eventPublisher;

    /**
     * 发布评论创建事件
     */
    public void publishCommentCreatedEvent(CommentCreatedEvent event) {
        eventPublisher.publishEvent(event, "CommentCreatedEvent");
        log.info("发布评论创建事件: {}", event);
    }

    /**
     * 发布评论点赞事件
     */
    public void publishCommentLikedEvent(CommentLikedEvent event) {
        eventPublisher.publishEvent(event, "CommentLikedEvent");
        log.info("发布评论点赞事件: {}", event);
    }

    /**
     * 发布评论删除事件
     */
    public void publishCommentDeletedEvent(CommentDeletedEvent event) {
        eventPublisher.publishEvent(event, "CommentDeletedEvent");
        log.info("发布评论删除事件: {}", event);
    }

    /**
     * 通用评论事件发布
     */
    public void publishCommentEvent(CommentEvent event) {
        eventPublisher.publishEvent(event, "CommentEvent");
        log.info("发布通用评论事件: {}", event);
    }
    
    /**
     * 发布评论更新事件
     */
    public void publishCommentUpdatedEvent(CommentUpdatedEvent event) {
        eventPublisher.publishEvent(event, "CommentUpdatedEvent");
        log.info("发布评论更新事件: {}", event);
    }
}