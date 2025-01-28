package cn.xu.domain.notification.service;

import cn.xu.domain.notification.model.valueobject.BusinessType;

public interface NotificationPublisher {
    /**
     * 发送系统通知
     */
    void publishSystemNotification(String title, String content, Long userId);

    /**
     * 发送点赞通知
     */
    void publishLikeNotification(Long senderId, Long userId, Long businessId, BusinessType businessType);

    /**
     * 发送收藏通知
     */
    void publishFavoriteNotification(Long senderId, Long userId, Long articleId);

    /**
     * 发送评论通知
     */
    void publishCommentNotification(Long senderId, Long userId, Long articleId, String content);

    /**
     * 发送回复通知
     */
    void publishReplyNotification(Long senderId, Long userId, Long commentId, String content);

    /**
     * 发送关注通知
     */
    void publishFollowNotification(Long senderId, Long userId);
} 