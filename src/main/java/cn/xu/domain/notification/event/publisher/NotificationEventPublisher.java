package cn.xu.domain.notification.event.publisher;

import cn.xu.domain.notification.event.NotificationEvent;
import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import com.lmax.disruptor.RingBuffer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 通知事件发布器
 * 负责将通知事件发布到 Disruptor 的 RingBuffer 中
 */
@Slf4j
@Component
public class NotificationEventPublisher {

    @Resource
    private RingBuffer<NotificationEvent> notificationRingBuffer;

    /**
     * 发布通知事件
     */
    public void publish(NotificationAggregate notification) {
        // 获取下一个序列号
        long sequence = notificationRingBuffer.next();
        try {
            // 获取该序列号对应的事件对象
            NotificationEvent event = notificationRingBuffer.get(sequence);
            
            // 设置事件属性
            event.setNotificationId(notification.getNotification().getId());
            event.setType(notification.getNotification().getType());
            event.setSenderId(notification.getNotification().getSenderId());
            event.setSenderType(notification.getNotification().getSenderType());
            event.setReceiverId(notification.getNotification().getReceiverId());
            event.setContent(notification.getNotification().getContent());
            event.setNotificationBusinessType(notification.getNotification().getNotificationBusinessType());
            event.setBusinessId(notification.getNotification().getBusinessId());
            event.setExtraInfo(notification.getNotification().getExtraInfo());
            event.setCreatedTime(notification.getNotification().getCreatedTime());
            
            log.info("[通知服务] 发布通知事件: type={}, receiverId={}, sequence={}", 
                    event.getType(), event.getReceiverId(), sequence);
        } finally {
            // 发布事件
            notificationRingBuffer.publish(sequence);
        }
    }
} 