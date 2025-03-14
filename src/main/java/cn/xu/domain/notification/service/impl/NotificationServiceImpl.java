package cn.xu.domain.notification.service.impl;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.repository.INotificationRepository;
import cn.xu.domain.notification.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl implements INotificationService {

    private final INotificationRepository notificationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<NotificationAggregate> getUserNotifications(Long userId, NotificationType type, int page, int size) {
        return notificationRepository.findByUserIdAndType(
                userId,
                type,
                PageRequest.of(page - 1, size)
        );
    }

    @Override
    @Transactional(readOnly = true)
    public long getUnreadCount(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Override
    @Transactional
    public void markAsRead(Long notificationId) {
        if (notificationId != null) {
            notificationRepository.markAsRead(notificationId);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        notificationRepository.markAllAsRead(userId);
    }

    @Override
    @Transactional
    public void deleteNotification(Long notificationId) {
        if (notificationId != null) {
            notificationRepository.delete(notificationId);
        }
    }

} 