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
 * @author xu
 * @since 2025-11-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final INotificationRepository notificationRepository;

    /**
     * 获取用户通知列表
     */
    @Transactional(readOnly = true)
    public List<Notification> getUserNotifications(Long userId, Integer type, int page, int size) {
        return notificationRepository.findByUserIdAndType(userId, type, PageRequest.of(page - 1, size));
    }

    /**
     * 获取用户未读通知数量
     */
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    /**
     * 标记通知为已读
     */
    @Transactional
    public void markAsRead(Long notificationId) {
        if (notificationId != null) {
            notificationRepository.markAsRead(notificationId);
        }
    }

    /**
     * 标记用户所有通知为已读
     */
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    /**
     * 删除通知
     */
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (notificationId != null) {
            notificationRepository.delete(notificationId);
        }
    }
    
    /**
     * 发送通知
     */
    @Transactional
    public void sendNotification(Notification notification) {
        notificationRepository.save(notification);
        log.debug("通知已发送: type={}, receiver={}", notification.getType(), notification.getReceiverId());
    }
    
    /**
     * 获取各类型未读通知数量
     */
    @Transactional(readOnly = true)
    public Map<Integer, Long> getUnreadCountByType(Long userId) {
        return notificationRepository.getUnreadCountByType(userId);
    }
    
    /**
     * 批量删除通知
     */
    @Transactional
    public void batchDeleteNotifications(List<Long> notificationIds) {
        notificationRepository.batchDelete(notificationIds);
    }
}
