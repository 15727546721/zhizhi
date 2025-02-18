package cn.xu.domain.notification.factory;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.template.AbstractNotificationTemplate;
import cn.xu.domain.notification.model.template.LikeNotificationTemplate;
import cn.xu.domain.notification.model.template.impl.*;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * 通知模板工厂
 */
@Component
@RequiredArgsConstructor
public class NotificationTemplateFactory {

    private final IUserService userService;

    /**
     * 创建系统通知
     */
    public NotificationAggregate createSystemNotification(String title, String content, Long receiverId) {
        AbstractNotificationTemplate template = new SystemNotificationTemplate(title, content, receiverId);
        return template.build();
    }

    /**
     * 创建点赞通知
     */
    public NotificationAggregate createLikeNotification(Long senderId, Long receiverId, BusinessType notificationBusinessType, Long businessId, String senderName) {
        AbstractNotificationTemplate template = new LikeNotificationTemplate(senderId, receiverId, notificationBusinessType, businessId, senderName);
        return template.build();
    }

    /**
     * 创建收藏通知
     */
    public NotificationAggregate createFavoriteNotification(Long senderId, Long receiverId, BusinessType notificationBusinessType, Long businessId, String senderName) {
        AbstractNotificationTemplate template = new FavoriteNotificationTemplate(senderId, receiverId, notificationBusinessType, businessId, senderName);
        return template.build();
    }

    /**
     * 创建评论通知
     */
    public NotificationAggregate createCommentNotification(Long senderId, Long receiverId, BusinessType notificationBusinessType, Long businessId, String senderName, String commentContent) {
        AbstractNotificationTemplate template = new CommentNotificationTemplate(senderId, receiverId, notificationBusinessType, businessId, senderName, commentContent);
        return template.build();
    }

    /**
     * 创建关注通知
     */
    public NotificationAggregate createFollowNotification(Long senderId, Long receiverId, String senderName) {
        AbstractNotificationTemplate template = new FollowNotificationTemplate(senderId, receiverId, senderName);
        return template.build();
    }

    /**
     * 创建回复通知
     */
    public NotificationAggregate createReplyNotification(Long senderId, Long receiverId, BusinessType notificationBusinessType, Long businessId, String senderName, String replyContent) {
        AbstractNotificationTemplate template = new ReplyNotificationTemplate(senderId, receiverId, notificationBusinessType, businessId, senderName, replyContent);
        return template.build();
    }
} 