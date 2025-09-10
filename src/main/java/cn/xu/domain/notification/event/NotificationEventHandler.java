package cn.xu.domain.notification.event;

import cn.xu.domain.notification.model.entity.NotificationEntity;
import cn.xu.domain.notification.repository.INotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * 监听通知事件并处理（保存通知实体）
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventHandler {

    private final INotificationRepository notificationRepository;

    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        log.info("[通知服务] 接收到通知事件: {}", event);

        NotificationEntity entity = NotificationEntity.builder()
                .type(event.getType())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .content(event.getContent())
                .businessType(event.getBusinessType())
                .businessId(event.getBusinessId())
                .createTime(event.getCreateTime())
                .build();

        notificationRepository.save(entity);
    }
}
