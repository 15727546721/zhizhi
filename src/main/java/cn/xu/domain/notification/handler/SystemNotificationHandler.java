package cn.xu.domain.notification.handler;

import cn.xu.domain.notification.event.NotificationEvent;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.service.INotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 系统通知处理器
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class SystemNotificationHandler extends AbstractNotificationHandler {

    private final INotificationService notificationService;

    @Override
    protected boolean supports(NotificationType type) {
        return NotificationType.SYSTEM.equals(type);
    }

    @Override
    protected void doHandle(NotificationEvent event) {
        log.info("[通知服务] 处理系统通知: receiverId={}", event.getReceiverId());
        // 系统通知的特殊处理逻辑
//        notificationService.sendSystemNotification();
    }
} 