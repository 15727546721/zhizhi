package cn.xu.domain.notification.service.impl;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.template.AbstractNotificationTemplate;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.repository.INotificationRepository;
import cn.xu.domain.notification.service.INotificationService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
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
    private final NotificationProcessService notificationProcessService;
    private final IUserService userService;

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
        NotificationAggregate notification = notificationRepository.findById(notificationId);
        if (notification != null) {
            notification.markAsRead();
            notificationRepository.save(notification);
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
        NotificationAggregate notification = notificationRepository.findById(notificationId);
        if (notification != null) {
            notification.markAsDeleted();
            notificationRepository.save(notification);
        }
    }

    @Override
    @Transactional
    public void sendNotification(NotificationAggregate notification) {
        // 1. 验证通知
        notification.validate();
        
        // 2. 异步处理通知
        notificationProcessService.processNotificationAsync(notification);
    }

    @Override
    @Transactional
    public void sendNotificationFromTemplate(AbstractNotificationTemplate template) {
        // 1. 从模板构建通知
        NotificationAggregate notification = template.build();
        
        // 2. 补充发送者信息
        if (notification.getSender().isUser()) {
            UserEntity sender = userService.getUserById(notification.getSender().getSenderId());

        }

        // 3. 发送通知
        sendNotification(notification);
    }

    @Override
    @Transactional
    public void sendSystemNotification(String title, String content, Long userId) {
        NotificationAggregate notification = NotificationAggregate.createSystemNotification(title, content, userId);
        sendNotification(notification);
    }
} 