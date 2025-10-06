package cn.xu.domain.user.event;

import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.post.event.PostCreatedEvent;
import cn.xu.infrastructure.event.annotation.DisruptorListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 用户领域事件监听器
 * 处理其他领域发布的与用户相关的事件
 * 注意：用户积分更新已移到DataConsistencyService中统一处理
 */
@Slf4j
@Component
public class UserEventListener {

    /**
     * 处理帖子创建事件，可以在这里处理其他用户相关逻辑
     */
    @DisruptorListener(eventType = "PostCreatedEvent", priority = 15)
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
     * 处理评论创建事件，可以在这里处理其他用户相关逻辑
     */
    @DisruptorListener(eventType = "CommentCreatedEvent", priority = 15)
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
     * 处理点赞事件，可以在这里处理其他用户相关逻辑
     */
    @DisruptorListener(eventType = "LikeEvent", priority = 15)
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