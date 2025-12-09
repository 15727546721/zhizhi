package cn.xu.event;

import cn.xu.model.entity.Notification;
import cn.xu.service.notification.NotificationPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * 通知事件监听器
 * 
 * <p>监听NotificationEvent，异步处理WebSocket推送</p>
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationEventListener {

    private final NotificationPushService pushService;

    /**
     * 处理通知事件
     * 
     * <p>异步执行，不阻塞主业务流程</p>
     */
    @Async
    @EventListener
    public void handleNotificationEvent(NotificationEvent event) {
        Notification notification = event.getNotification();
        
        if (notification == null) {
            log.warn("[通知事件] 收到空通知事件");
            return;
        }
        
        log.debug("[通知事件] 收到通知事件: type={}, receiverId={}", 
                  event.getType(), event.getReceiverId());
        
        try {
            // 推送给指定用户
            pushService.pushToUser(event.getReceiverId(), notification);
        } catch (Exception e) {
            log.error("[通知事件] 处理失败: type={}, error={}", event.getType(), e.getMessage(), e);
        }
    }
}
