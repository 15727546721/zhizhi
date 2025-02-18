package cn.xu.domain.notification.event.publisher;

import cn.xu.domain.notification.event.NotificationEvent;
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
     * 发布通知事件 (其他领域服务调用该方法即可)
     */
    public void publishEvent(NotificationEvent notificationEvent) {
        // 获取下一个序列号
        long sequence = notificationRingBuffer.next();
        try {
            // 获取该序列号对应的事件对象
            NotificationEvent event = notificationRingBuffer.get(sequence);

            // 设置事件属性
            event.setType(notificationEvent.getType());
            event.setSenderId(notificationEvent.getSenderId());
            event.setReceiverId(notificationEvent.getReceiverId());
            event.setContent(notificationEvent.getContent());
            event.setBusinessType(notificationEvent.getBusinessType());
            event.setBusinessId(notificationEvent.getBusinessId());
            event.setCreateTime(notificationEvent.getCreateTime());

            log.info("[通知服务] 发布通知事件: type={}, receiverId={}, sequence={}",
                    event.getType(), event.getReceiverId(), sequence);
        } finally {
            // 发布事件
            notificationRingBuffer.publish(sequence);
        }
    }
} 