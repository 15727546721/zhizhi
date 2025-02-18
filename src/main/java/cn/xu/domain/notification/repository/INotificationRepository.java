package cn.xu.domain.notification.repository;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.entity.NotificationEntity;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 通知仓储接口
 */
public interface INotificationRepository {

    /**
     * 保存通知
     */
    NotificationAggregate save(NotificationEntity notification);

    /**
     * 根据ID查询通知
     */
    NotificationEntity findById(Long id);

    /**
     * 根据用户ID和类型分页查询通知
     */
    List<NotificationAggregate> findByUserIdAndType(Long userId, NotificationType type, Pageable pageable);

    /**
     * 根据用户ID和通知类型分页查询通知
     */
    List<NotificationAggregate> findByUserIdAndType(Long userId, NotificationType type, int offset, int limit);

    /**
     * 统计用户未读通知数量
     */
    long countByUserIdAndIsReadFalse(Long userId);

    /**
     * 统计用户各个类型未读通知数量
     */
    long countUnreadByUserId(Long userId);

    /**
     * 将通知标记为已读
     */
    void markAsRead(Long notificationId);

    /**
     * 将用户的所有通知标记为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 删除通知
     */
    void delete(Long notificationId);

    /**
     * 检查通知是否存在
     */
    boolean exists(Long id);

    /**
     * 根据接收者ID分页查询通知，按创建时间降序
     */
    List<NotificationAggregate> findByReceiverIdOrderByCreatedTimeDesc(Long receiverId, int offset, int limit);

    /**
     * 统计接收者未读通知数量
     */
    long countByReceiverIdAndReadFalse(Long receiverId);
} 