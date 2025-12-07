package cn.xu.event.handler;

import cn.xu.event.events.*;
import cn.xu.model.entity.Comment;
import cn.xu.model.entity.Notification;
import cn.xu.model.entity.Post;
import cn.xu.repository.ICommentRepository;
import cn.xu.service.notification.NotificationService;
import cn.xu.service.post.PostService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 通知处理器
 *
 * <p>统一处理所有事件的通知创建逻辑：
 * <ul>
 *   <li>评论通知</li>
 *   <li>点赞通知</li>
 *   <li>关注通知</li>
 *   <li>收藏通知</li>
 *   <li>私信通知</li>
 * </ul>
 *
 *
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationHandler {

    private final NotificationService notificationService;
    private final PostService postService;
    private final ICommentRepository commentRepository;

    // ==================== 评论通知 ====================

    @Async
    @EventListener
    public void onComment(CommentEvent event) {
        if (event.getAction() != CommentEvent.EventAction.CREATE) {
            return;
        }

        try {
            Long senderId = event.getOperatorId();
            Long receiverId;
            int notificationType;

            if (event.isRootComment()) {
                // 一级评论时通知帖子作者
                Optional<Post> postOpt = postService.getPostById(event.getPostId());
                if (!postOpt.isPresent()) return;
                receiverId = postOpt.get().getUserId();
                notificationType = Notification.TYPE_COMMENT;
            } else {
                // 回复评论时通知被回复用户
                receiverId = event.getReplyUserId();
                notificationType = Notification.TYPE_REPLY;
            }

            // 不通知自己
            if (senderId.equals(receiverId)) return;

            Notification notification = Notification.builder()
                    .type(notificationType)
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .senderType(Notification.SENDER_TYPE_USER)
                    .content(truncate(event.getContent(), 50))
                    .businessType(Notification.BUSINESS_POST)
                    .businessId(event.getPostId())
                    .isRead(Notification.READ_NO)
                    .status(Notification.STATUS_VALID)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            notificationService.sendNotification(notification);
            log.debug("[Handler] 评论通知已创建 - receiver: {}", receiverId);

        } catch (Exception e) {
            log.error("[Handler] 处理评论事件失败", e);
        }
    }

    // ==================== 点赞通知 ====================

    @Async
    @EventListener
    public void onLike(LikeEvent event) {
        if (!event.isLiked()) return; // 只处理点赞

        try {
            Long senderId = event.getOperatorId();
            Long receiverId;
            int businessType;

            if (event.getType() == LikeEvent.LikeType.POST) {
                Optional<Post> postOpt = postService.getPostById(event.getTargetId());
                if (!postOpt.isPresent()) return;
                receiverId = postOpt.get().getUserId();
                businessType = Notification.BUSINESS_POST;
            } else {
                Comment comment = commentRepository.findById(event.getTargetId());
                if (comment == null) return;
                receiverId = comment.getUserId();
                businessType = Notification.BUSINESS_COMMENT;
            }

            if (senderId.equals(receiverId)) return;

            Notification notification = Notification.createLikeNotification(
                    senderId, receiverId, event.getTargetId(), businessType);
            notificationService.sendNotification(notification);
            log.debug("[Handler] 点赞通知已创建 - receiver: {}", receiverId);

        } catch (Exception e) {
            log.error("[Handler] 处理点赞事件失败", e);
        }
    }

    // ==================== 关注通知 ====================

    @Async
    @EventListener
    public void onFollow(FollowEvent event) {
        if (!event.isFollowed()) return; // 只处理关注

        try {
            Notification notification = Notification.createFollowNotification(
                    event.getFollowerId(), event.getFolloweeId());
            notificationService.sendNotification(notification);
            log.debug("[Handler] 关注通知已创建 - followee: {}", event.getFolloweeId());

        } catch (Exception e) {
            log.error("[Handler] 处理关注事件失败", e);
        }
    }

    // ==================== 收藏通知 ====================

    @Async
    @EventListener
    public void onFavorite(FavoriteEvent event) {
        if (!event.isFavorited()) return; // 只处理收藏

        try {
            Optional<Post> postOpt = postService.getPostById(event.getPostId());
            if (!postOpt.isPresent()) return;

            Long senderId = event.getOperatorId();
            Long receiverId = postOpt.get().getUserId();

            if (senderId.equals(receiverId)) return;

            Notification notification = Notification.builder()
                    .type(Notification.TYPE_FAVORITE)
                    .senderId(senderId)
                    .receiverId(receiverId)
                    .senderType(Notification.SENDER_TYPE_USER)
                    .content("收藏了你的帖子")
                    .businessType(Notification.BUSINESS_POST)
                    .businessId(event.getPostId())
                    .isRead(Notification.READ_NO)
                    .status(Notification.STATUS_VALID)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            notificationService.sendNotification(notification);
            log.debug("[Handler] 收藏通知已创建 - receiver: {}", receiverId);

        } catch (Exception e) {
            log.error("[Handler] 处理收藏事件失败", e);
        }
    }

    // ==================== 私信通知 ====================

    @Async
    @EventListener
    public void onDM(DMEvent event) {
        if (event.getDmEventType() != DMEvent.DMEventType.SENT) return;

        try {
            String content = event.isGreeting()
                    ? "向你打了个招呼"
                    : "给你发送了一条消息";

            if (event.getContentPreview() != null) {
                content = content + " " + event.getContentPreview();
            }

            Notification notification = Notification.builder()
                    .type(Notification.TYPE_PRIVATE_MESSAGE)
                    .senderId(event.getSenderId())
                    .receiverId(event.getReceiverId())
                    .senderType(Notification.SENDER_TYPE_USER)
                    .content(content)
                    .businessType(Notification.BUSINESS_USER)
                    .businessId(event.getSenderId())
                    .isRead(Notification.READ_NO)
                    .status(Notification.STATUS_VALID)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            notificationService.sendNotification(notification);
            log.debug("[Handler] 私信通知已创建 - receiver: {}", event.getReceiverId());

        } catch (Exception e) {
            log.error("[Handler] 处理私信事件失败", e);
        }
    }

    // ==================== 工具方法 ====================

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() > maxLength ? str.substring(0, maxLength) + "..." : str;
    }
}
