package cn.xu.domain.notification.event;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 通知已读事件
 *
 */
@Getter
public class NotificationReadEvent extends NotificationEvent {
    
    /**
     * 阅读时间
     */
    private LocalDateTime readTime;
    
    public static NotificationReadEvent from(NotificationAggregate aggregate) {
        NotificationReadEvent event = new NotificationReadEvent();
        NotificationEvent baseEvent = NotificationEvent.from(aggregate);
        
        // 复制基础事件的属性
        event.setNotificationId(baseEvent.getNotificationId());
        event.setType(baseEvent.getType());
        event.setSenderId(baseEvent.getSenderId());
        event.setSenderType(baseEvent.getSenderType());
        event.setReceiverId(baseEvent.getReceiverId());
        event.setContent(baseEvent.getContent());
        event.setNotificationBusinessType(baseEvent.getNotificationBusinessType());
        event.setBusinessId(baseEvent.getBusinessId());
        event.setExtraInfo(baseEvent.getExtraInfo());
        event.setCreatedTime(baseEvent.getCreatedTime());
        
        // 设置阅读时间
        event.readTime = LocalDateTime.now();
        
        return event;
    }
} 