package cn.xu.domain.notification.factory;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class NotificationFactory {

    /**
     * 创建系统通知
     */
    public NotificationAggregate createSystemNotification(String title, String content, Long userId) {
        return NotificationAggregate.createSystemNotification(title, content, userId);
    }

    /**
     * 创建点赞通知
     */
    public NotificationAggregate createLikeNotification(Long senderId, Long userId, Long businessId, BusinessType notificationBusinessType, String senderName) {
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("senderName", senderName);

        return NotificationAggregate.builder()
                .type(NotificationType.LIKE)
                .receiverId(userId)
                .senderId(senderId)
                .content(senderName + "赞了你的" + notificationBusinessType.getDescription())
                .businessType(notificationBusinessType)
                .businessId(businessId)
                .read(false)
                .status(true)
                .build();
    }

    /**
     * 创建收藏通知
     */
    public NotificationAggregate createFavoriteNotification(Long senderId, Long userId, Long articleId, String senderName) {
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("senderName", senderName);

        return NotificationAggregate.builder()
                .type(NotificationType.FAVORITE)
                .receiverId(userId)
                .senderId(senderId)
                .content(senderName + "收藏了你的文章")
                .businessType(BusinessType.ARTICLE)
                .businessId(articleId)
                .read(false)
                .status(true)
                .build();
    }

    /**
     * 创建评论通知
     */
    public NotificationAggregate createCommentNotification(
            Long commentId,
            Long senderId,
            Long receiverId,
            String commentContent,
            Long articleId,
            BusinessType notificationBusinessType) {

        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("commentId", commentId);
        extraInfo.put("articleId", articleId);

        return NotificationAggregate.builder()
                .type(NotificationType.COMMENT)
                .senderId(senderId)
                .receiverId(receiverId)
                .content("评论了你的文章：" + commentContent)
                .businessType(notificationBusinessType)
                .businessId(articleId)
                .read(false)
                .status(true)
                .build();
    }

    /**
     * 创建回复通知
     */
    public NotificationAggregate createReplyNotification(
            Long replyId,
            Long senderId,
            Long receiverId,
            String replyContent,
            Long parentCommentId,
            BusinessType notificationBusinessType) {

        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("replyId", replyId);
        extraInfo.put("parentCommentId", parentCommentId);

        return NotificationAggregate.builder()
                .type(NotificationType.REPLY)
                .senderId(senderId)
                .receiverId(receiverId)
                .content("回复了你的评论：" + replyContent)
                .businessType(notificationBusinessType)
                .businessId(parentCommentId)
                .read(false)
                .status(true)
                .build();
    }

    /**
     * 创建关注通知
     */
    public NotificationAggregate createFollowNotification(
            Long senderId,
            Long userId,
            String senderName,
            BusinessType notificationBusinessType) {
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("senderName", senderName);

        return NotificationAggregate.builder()
                .type(NotificationType.FOLLOW)
                .receiverId(userId)
                .senderId(senderId)
                .content(senderName + "关注了你")
                .businessType(notificationBusinessType)
                .businessId(userId)
                .read(false)
                .status(true)
                .build();
    }
} 