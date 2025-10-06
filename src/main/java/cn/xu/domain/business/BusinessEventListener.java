package cn.xu.domain.business;

import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.comment.event.CommentDeletedEvent;
import cn.xu.domain.comment.event.CommentUpdatedEvent;
import cn.xu.domain.like.event.LikeEvent;
import cn.xu.domain.post.event.PostCreatedEvent;
import cn.xu.domain.post.event.PostDeletedEvent;
import cn.xu.domain.post.event.PostUpdatedEvent;
import cn.xu.domain.user.event.UserLoggedInEvent;
import cn.xu.domain.user.event.UserRegisteredEvent;
import cn.xu.domain.user.event.UserUpdatedEvent;
import cn.xu.infrastructure.event.annotation.DisruptorListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 综合业务事件监听器
 * 处理跨领域的业务逻辑联动，使用统一的数据一致性服务
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BusinessEventListener {

    private final DataConsistencyService dataConsistencyService;

    /**
     * 处理用户注册事件
     */
    @DisruptorListener(eventType = "UserRegisteredEvent", priority = 5)
    public void handleUserRegistered(UserRegisteredEvent event) {
        try {
            log.info("处理用户注册事件: {}", event);
            // 处理用户注册后的数据一致性
            dataConsistencyService.handleUserRegistration(event);
        } catch (Exception e) {
            log.error("处理用户注册事件失败", e);
        }
    }

    /**
     * 处理用户登录事件
     */
    @DisruptorListener(eventType = "UserLoggedInEvent", priority = 5)
    public void handleUserLoggedIn(UserLoggedInEvent event) {
        try {
            log.info("处理用户登录事件: {}", event);
            // 可以在这里实现用户登录后的业务逻辑
            // 例如：记录登录日志、更新最后登录时间等
        } catch (Exception e) {
            log.error("处理用户登录事件失败", e);
        }
    }

    /**
     * 处理用户更新事件
     */
    @DisruptorListener(eventType = "UserUpdatedEvent", priority = 5)
    public void handleUserUpdated(UserUpdatedEvent event) {
        try {
            log.info("处理用户更新事件: {}", event);
            // 可以在这里实现用户更新后的业务逻辑
            // 例如：同步用户信息到其他系统、更新缓存等
        } catch (Exception e) {
            log.error("处理用户更新事件失败", e);
        }
    }

    /**
     * 处理帖子创建事件
     */
    @DisruptorListener(eventType = "PostCreatedEvent", priority = 5)
    public void handlePostCreated(PostCreatedEvent event) {
        try {
            log.info("处理帖子创建事件: {}", event);
            // 处理帖子创建后的数据一致性
            dataConsistencyService.handlePostCreation(event);
        } catch (Exception e) {
            log.error("处理帖子创建事件失败", e);
        }
    }

    /**
     * 处理帖子更新事件
     */
    @DisruptorListener(eventType = "PostUpdatedEvent", priority = 5)
    public void handlePostUpdated(PostUpdatedEvent event) {
        try {
            log.info("处理帖子更新事件: {}", event);
            // 可以在这里实现帖子更新后的业务逻辑
            // 例如：更新缓存、发送通知等
        } catch (Exception e) {
            log.error("处理帖子更新事件失败", e);
        }
    }

    /**
     * 处理帖子删除事件
     */
    @DisruptorListener(eventType = "PostDeletedEvent", priority = 5)
    public void handlePostDeleted(PostDeletedEvent event) {
        try {
            log.info("处理帖子删除事件: {}", event);
            // 处理帖子删除后的数据一致性
            dataConsistencyService.handlePostDeletion(event);
        } catch (Exception e) {
            log.error("处理帖子删除事件失败", e);
        }
    }

    /**
     * 处理评论创建事件
     */
    @DisruptorListener(eventType = "CommentCreatedEvent", priority = 5)
    public void handleCommentCreated(CommentCreatedEvent event) {
        try {
            log.info("处理评论创建事件: {}", event);
            // 处理评论创建后的数据一致性
            dataConsistencyService.handleCommentCreation(event);
        } catch (Exception e) {
            log.error("处理评论创建事件失败", e);
        }
    }

    /**
     * 处理评论更新事件
     */
    @DisruptorListener(eventType = "CommentUpdatedEvent", priority = 5)
    public void handleCommentUpdated(CommentUpdatedEvent event) {
        try {
            log.info("处理评论更新事件: {}", event);
            // 可以在这里实现评论更新后的业务逻辑
            // 例如：更新缓存、发送通知等
        } catch (Exception e) {
            log.error("处理评论更新事件失败", e);
        }
    }

    /**
     * 处理评论删除事件
     */
    @DisruptorListener(eventType = "CommentDeletedEvent", priority = 5)
    public void handleCommentDeleted(CommentDeletedEvent event) {
        try {
            log.info("处理评论删除事件: {}", event);
            // 处理评论删除后的数据一致性
            dataConsistencyService.handleCommentDeletion(event);
        } catch (Exception e) {
            log.error("处理评论删除事件失败", e);
        }
    }

    /**
     * 处理点赞事件
     */
    @DisruptorListener(eventType = "LikeEvent", priority = 5)
    public void handleLikeEvent(LikeEvent event) {
        try {
            log.info("处理点赞事件: {}", event);
            // 处理点赞后的数据一致性
            dataConsistencyService.handleLikeOperation(event);
        } catch (Exception e) {
            log.error("处理点赞事件失败", e);
        }
    }
}