package cn.xu.domain.user.event;

import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.post.event.PostCreatedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 用户事件监听器
 * 使用Spring Event机制异步处理用户相关事件
 */
@Slf4j
@Component
public class UserEventListener {

    /**
     * 处理帖子创建事件
     */
    @Async
    @EventListener
    public void handlePostCreated(PostCreatedEvent event) {
        try {
            log.info("处理帖子创建事件: {}", event);
            // 用户相关处理逻辑（非积分相关）
            // 例如：更新用户发帖统计等
        } catch (Exception e) {
            log.error("处理帖子创建事件失败", e);
        }
    }

    /**
     * 处理评论创建事件
     */
    @Async
    @EventListener
    public void handleCommentCreated(CommentCreatedEvent event) {
        try {
            log.info("处理评论创建事件: {}", event);
            // 用户相关处理逻辑（非积分相关）
            // 例如：更新用户评论统计等
        } catch (Exception e) {
            log.error("处理评论创建事件失败", e);
        }
    }

    /**
     * 处理点赞事件
     */
    @Async
    @EventListener
    public void handleLikeEvent(LikeEvent event) {
        try {
            log.info("处理点赞事件: {}", event);
            // 用户相关处理逻辑（非积分相关）
            // 例如：更新用户点赞统计等
        } catch (Exception e) {
            log.error("处理点赞事件失败", e);
        }
    }
}