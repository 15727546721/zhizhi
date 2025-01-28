package cn.xu.domain.notification.factory;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;
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
    public NotificationAggregate createLikeNotification(Long senderId, Long userId, Long businessId, BusinessType businessType, String senderName) {
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("senderName", senderName);
        
        return NotificationAggregate.builder()
                .type(NotificationType.LIKE)
                .receiverId(userId)
                .senderId(senderId)
                .senderType(SenderType.USER)
                .content(senderName + "赞了你的" + businessType.getDescription())
                .businessType(businessType)
                .businessId(businessId)
                .extraInfo(extraInfo)
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
                .senderType(SenderType.USER)
                .content(senderName + "收藏了你的文章")
                .businessType(BusinessType.ARTICLE)
                .businessId(articleId)
                .extraInfo(extraInfo)
                .read(false)
                .status(true)
                .build();
    }

    /**
     * 创建评论通知
     */
    public NotificationAggregate createCommentNotification(Long senderId, Long userId, Long articleId, String content, String senderName) {
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("senderName", senderName);
        
        return NotificationAggregate.builder()
                .type(NotificationType.COMMENT)
                .receiverId(userId)
                .senderId(senderId)
                .senderType(SenderType.USER)
                .content(content)
                .businessType(BusinessType.ARTICLE)
                .businessId(articleId)
                .extraInfo(extraInfo)
                .read(false)
                .status(true)
                .build();
    }

    /**
     * 创建回复通知
     */
    public NotificationAggregate createReplyNotification(Long senderId, Long userId, Long commentId, String content, String senderName) {
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("senderName", senderName);
        
        return NotificationAggregate.builder()
                .type(NotificationType.REPLY)
                .receiverId(userId)
                .senderId(senderId)
                .senderType(SenderType.USER)
                .content(content)
                .businessType(BusinessType.COMMENT)
                .businessId(commentId)
                .extraInfo(extraInfo)
                .read(false)
                .status(true)
                .build();
    }

    /**
     * 创建关注通知
     */
    public NotificationAggregate createFollowNotification(Long senderId, Long userId, String senderName) {
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("senderName", senderName);
        
        return NotificationAggregate.builder()
                .type(NotificationType.FOLLOW)
                .receiverId(userId)
                .senderId(senderId)
                .senderType(SenderType.USER)
                .content(senderName + "关注了你")
                .businessType(BusinessType.USER)
                .businessId(userId)
                .extraInfo(extraInfo)
                .read(false)
                .status(true)
                .build();
    }
} 