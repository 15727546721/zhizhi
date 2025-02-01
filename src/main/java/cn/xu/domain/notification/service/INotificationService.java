package cn.xu.domain.notification.service;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.template.AbstractNotificationTemplate;
import cn.xu.domain.notification.model.valueobject.NotificationType;

import java.util.List;

/**
 * 通知服务接口
 */
public interface INotificationService {
    /**
     * 获取用户通知列表
     */
    List<NotificationAggregate> getUserNotifications(Long userId, NotificationType type, int page, int size);

    /**
     * 获取用户未读通知数量
     */
    long getUnreadCount(Long userId);

    /**
     * 标记通知为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 标记用户所有通知为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    void deleteNotification(Long notificationId);

    /**
     * 发送通知
     */
    void sendNotification(NotificationAggregate notification);

    /**
     * 使用模板发送通知
     */
    void sendNotificationFromTemplate(AbstractNotificationTemplate template);

    /**
     * 发送系统通知
     */
    void sendSystemNotification(String title, String content, Long userId);
} 