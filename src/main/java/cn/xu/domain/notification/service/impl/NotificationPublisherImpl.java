package cn.xu.domain.notification.service.impl;

import cn.xu.domain.notification.factory.NotificationFactory;
import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.service.INotificationService;
import cn.xu.domain.notification.service.NotificationPublisher;
import cn.xu.domain.user.service.IUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationPublisherImpl implements NotificationPublisher {

    private final NotificationFactory notificationFactory;
    private final INotificationService notificationService;
    private final IUserService userService;

    @Override
    public void publishSystemNotification(String title, String content, Long userId) {
        try {
            NotificationAggregate notification = notificationFactory.createSystemNotification(title, content, userId);
            notificationService.sendNotification(notification);
        } catch (Exception e) {
            log.error("发送系统通知失败: title={}, userId={}", title, userId, e);
        }
    }

    @Override
    public void publishLikeNotification(Long senderId, Long userId, Long businessId, BusinessType notificationBusinessType) {
        try {
            String senderName = userService.getNicknameById(senderId);
            NotificationAggregate notification = notificationFactory.createLikeNotification(
                    senderId, userId, businessId, notificationBusinessType, senderName);
            notificationService.sendNotification(notification);
        } catch (Exception e) {
            log.error("发送点赞通知失败: senderId={}, userId={}", senderId, userId, e);
        }
    }

    @Override
    public void publishFavoriteNotification(Long senderId, Long userId, Long articleId, BusinessType notificationBusinessType) {
        try {
            String senderName = userService.getNicknameById(senderId);
            NotificationAggregate notification = notificationFactory.createFavoriteNotification(
                    senderId, userId, articleId, senderName);
            notificationService.sendNotification(notification);
        } catch (Exception e) {
            log.error("发送收藏通知失败: senderId={}, userId={}", senderId, userId, e);
        }
    }

    @Override
    public void publishCommentNotification(Long senderId, Long userId, Long articleId, String content, BusinessType notificationBusinessType) {
        try {
            String senderName = userService.getNicknameById(senderId);
            NotificationAggregate notification = notificationFactory.createCommentNotification(
                    articleId,  // commentId
                    senderId,
                    userId,    // receiverId
                    content,
                    articleId,
                    notificationBusinessType
            );
            notificationService.sendNotification(notification);
        } catch (Exception e) {
            log.error("发送评论通知失败: senderId={}, userId={}", senderId, userId, e);
        }
    }

    @Override
    public void publishReplyNotification(Long senderId, Long userId, Long commentId, String content, BusinessType notificationBusinessType) {
        try {
            String senderName = userService.getNicknameById(senderId);
            NotificationAggregate notification = notificationFactory.createReplyNotification(
                    commentId,  // replyId
                    senderId,
                    userId,    // receiverId
                    content,
                    commentId,  // parentCommentId
                    notificationBusinessType
            );
            notificationService.sendNotification(notification);
        } catch (Exception e) {
            log.error("发送回复通知失败: senderId={}, userId={}", senderId, userId, e);
        }
    }

    @Override
    public void publishFollowNotification(Long senderId, Long userId, BusinessType notificationBusinessType) {
        try {
            String senderName = userService.getNicknameById(senderId);
            NotificationAggregate notification = notificationFactory.createFollowNotification(
                    senderId, userId, senderName, notificationBusinessType);
            notificationService.sendNotification(notification);
        } catch (Exception e) {
            log.error("发送关注通知失败: senderId={}, userId={}", senderId, userId, e);
        }
    }
} 