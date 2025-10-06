package cn.xu.domain.post.event;

import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.comment.event.CommentDeletedEvent;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.infrastructure.event.annotation.DisruptorListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 帖子领域事件监听器
 * 处理其他领域发布的与帖子相关的事件
 * 注意：帖子热度更新已移到DataConsistencyService中统一处理
 */
@Slf4j
@Component
public class PostEventListener {

    /**
     * 处理评论创建事件，可以在这里处理其他帖子相关逻辑
     */
    @DisruptorListener(eventType = "CommentCreatedEvent", priority = 20)
    public void handleCommentCreated(CommentCreatedEvent event) {
        try {
            log.info("处理评论创建事件: {}", event);
            // 帖子相关处理逻辑（非热度相关）
            // 例如：更新帖子的最后评论时间等
            
            // 更新帖子的评论数
            if (event.getTargetId() != null) {
                // 这里应该调用帖子服务来更新评论数
                // 由于这是一个事件监听器，应该通过事件总线发布更新事件
            }
        } catch (Exception e) {
            log.error("处理评论创建事件失败", e);
        }
    }

    /**
     * 处理评论删除事件，可以在这里处理其他帖子相关逻辑
     */
    @DisruptorListener(eventType = "CommentDeletedEvent", priority = 20)
    public void handleCommentDeleted(CommentDeletedEvent event) {
        try {
            log.info("处理评论删除事件: {}", event);
            // 帖子相关处理逻辑（非热度相关）
            // 例如：更新帖子的最后评论时间等
        } catch (Exception e) {
            log.error("处理评论删除事件失败", e);
        }
    }

    /**
     * 处理点赞事件，可以在这里处理其他帖子相关逻辑
     */
    @DisruptorListener(eventType = "LikeEvent", priority = 20)
    public void handleLikeEvent(LikeEvent event) {
        try {
            log.info("处理点赞事件: {}", event);
            // 帖子相关处理逻辑（非热度相关）
            // 例如：记录帖子的点赞用户等
        } catch (Exception e) {
            log.error("处理点赞事件失败", e);
        }
    }
}