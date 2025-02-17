package cn.xu.domain.notification.event;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import lombok.Getter;

/**
 * 通知创建事件
 *
 */
@Getter
public class NotificationCreatedEvent extends NotificationEvent {
    
    public static NotificationCreatedEvent from(NotificationAggregate aggregate) {
        NotificationCreatedEvent event = new NotificationCreatedEvent();
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
        
        return event;
    }
} 