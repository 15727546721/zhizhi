package cn.xu.domain.comment.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

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
     * 发布评论创建事件
     */
    public void publishCommentCreatedEvent(CommentCreatedEvent event) {
        log.debug("发布评论创建事件: commentId={}", event.getCommentId());
        eventPublisher.publishEvent(event);
    }

    /**
     * 发布评论点赞事件
     */
    public void publishCommentLikedEvent(CommentLikedEvent event) {
        log.debug("发布评论点赞事件: commentId={}", event.getCommentId());
        eventPublisher.publishEvent(event);
    }

    /**
     * 发布评论删除事件
     */
    public void publishCommentDeletedEvent(CommentDeletedEvent event) {
        log.debug("发布评论删除事件: commentId={}", event.getCommentId());
        eventPublisher.publishEvent(event);
    }

    /**
     * 发布通用评论事件
     */
    public void publishCommentEvent(CommentEvent event) {
        log.debug("发布通用评论事件: commentId={}", event.getCommentId());
        eventPublisher.publishEvent(event);
    }
    
    /**
     * 发布评论更新事件
     */
    public void publishCommentUpdatedEvent(CommentUpdatedEvent event) {
        log.debug("发布评论更新事件: commentId={}", event.getCommentId());
        eventPublisher.publishEvent(event);
    }
}