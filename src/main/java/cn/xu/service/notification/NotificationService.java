package cn.xu.service.notification;

import cn.xu.model.entity.Notification;
import cn.xu.repository.INotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/**
 * 通知服务
 * 
 * <p>提供通知的查询、已读标记、删除等功能
 *
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final INotificationRepository notificationRepository;

    // ==================== 查询方法 ====================

    /**
     * 获取用户通知列表（支持多类型）
     * 
     * @param typeStr 类型字符串，支持逗号分隔，如"1,2"
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId, String typeStr, int page, int size) {
        List<Integer> types = parseTypes(typeStr);
        return notificationRepository.findByUserIdAndTypes(userId, types, PageRequest.of(page - 1, size));
    }

    /**
     * 统计用户通知总数（支持多类型）
     */
    @Transactional(readOnly = true)
    public long countByUserIdAndType(Long userId, String typeStr) {
        List<Integer> types = parseTypes(typeStr);
        return notificationRepository.countByUserIdAndTypes(userId, types);
    }

    /**
     * 解析类型字符串为List
     */
    private List<Integer> parseTypes(String typeStr) {
        if (typeStr == null || typeStr.trim().isEmpty()) {
            return null;
        }
        try {
            String[] parts = typeStr.split(",");
            List<Integer> types = new java.util.ArrayList<>();
            for (String part : parts) {
                types.add(Integer.parseInt(part.trim()));
            }
            return types;
        } catch (NumberFormatException e) {
            log.warn("[通知] 解析类型参数失败: {}", typeStr);
            return null;
        }
    }

    /**
     * 获取用户未读通知数量
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 获取各类型未读通知数量
     */
    @Transactional(readOnly = true)
    public Map<Integer, Long> getUnreadCountByType(Long userId) {
        return notificationRepository.getUnreadCountByType(userId);
    }

    // ==================== 已读操作 ====================

    /**
     * 标记单个通知为已读（带权限校验）
     */
    @Transactional
    public void markAsRead(Long userId, Long notificationId) {
        if (notificationId == null) {
            return;
        }
        Notification notification = notificationRepository.findById(notificationId);
        if (notification != null && notification.belongsToUser(userId)) {
            notificationRepository.markAsRead(notificationId);
            log.debug("[通知] 标记已读: id={}", notificationId);
        }
    }

    /**
     * 标记用户所有通知为已读（可按类型）
     */
    @Transactional
    public void markAllAsRead(Long userId, Integer type) {
        notificationRepository.markAllAsRead(userId, type);
        log.debug("[通知] 全部标记已读: userId={}, type={}", userId, type);
    }

    // ==================== 删除操作 ====================

    /**
     * 删除单个通知（带权限校验）
     */
    @Transactional
    public void delete(Long userId, Long notificationId) {
        if (notificationId == null) {
            return;
        }
        Notification notification = notificationRepository.findById(notificationId);
        if (notification != null && notification.belongsToUser(userId)) {
            notificationRepository.delete(notificationId);
            log.debug("[通知] 删除: id={}", notificationId);
        }
    }

    /**
     * 批量删除通知（带权限校验）
     */
    @Transactional
    public void batchDelete(Long userId, List<Long> notificationIds) {
        if (notificationIds == null || notificationIds.isEmpty()) {
            return;
        }
        // 过滤只删除属于当前用户的通知
        notificationRepository.batchDeleteByUserIdAndIds(userId, notificationIds);
        log.debug("[通知] 批量删除: userId={}, count={}", userId, notificationIds.size());
    }

    // ==================== 发送通知（内部调用） ====================

    /**
     * 发送通知
     */
    @Transactional
    public void sendNotification(Notification notification) {
        notification.validate();
        notificationRepository.save(notification);
        log.debug("[通知] 已发送: type={}, receiver={}", notification.getType(), notification.getReceiverId());
    }
}
