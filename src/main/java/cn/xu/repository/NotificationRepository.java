package cn.xu.repository;

import cn.xu.model.entity.Notification;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Map;

/**
 * 通知仓储接口
 */
public interface NotificationRepository {

    /**
     * 保存通知
     */
    Notification save(Notification notification);

    /**
     * 根据ID查询通知
     */
    Notification findById(Long id);

    /**
     * 根据用户ID和类型分页查询通知
     */
    List<Notification> findByUserIdAndType(Long userId, Integer type, Pageable pageable);

    /**
     * 根据用户ID和多类型分页查询通知
     */
    List<Notification> findByUserIdAndTypes(Long userId, List<Integer> types, Pageable pageable);

    /**
     * 根据用户ID和通知类型分页查询通知
     */
    List<Notification> findByUserIdAndType(Long userId, Integer type, int offset, int limit);

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
     * 将用户的所有通知标记为已读（可按类型）
     */
    void markAllAsRead(Long userId, Integer type);

    /**
     * 删除通知
     */
    void delete(Long notificationId);

    /**
     * 统计用户通知总数（按类型）
     */
    long countByUserIdAndType(Long userId, Integer type);

    /**
     * 统计用户通知总数（按多类型）
     */
    long countByUserIdAndTypes(Long userId, List<Integer> types);

    /**
     * 检查通知是否存在
     */
    boolean exists(Long id);

    /**
     * 根据接收者ID分页查询通知，按创建时间降序
     */
    List<Notification> findByReceiverIdOrderByCreatedTimeDesc(Long receiverId, int offset, int limit);

    /**
     * 统计接收者未读通知数量
     */
    long countByReceiverIdAndReadFalse(Long receiverId);

    /**
     * 获取各类型未读通知数量
     */
    Map<Integer, Long> getUnreadCountByType(Long userId);

    /**
     * 批量删除通知
     */
    void batchDelete(List<Long> notificationIds);

    /**
     * 批量删除通知（带用户ID校验，只删除属于该用户的通知）
     */
    void batchDeleteByUserIdAndIds(Long userId, List<Long> notificationIds);
}
