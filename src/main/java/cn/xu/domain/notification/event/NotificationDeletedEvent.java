package cn.xu.domain.notification.event;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 通知删除事件
 *
 */
@Getter
public class NotificationDeletedEvent extends NotificationEvent {
    
    /**
     * 删除时间
     */
    private LocalDateTime deletedTime;
    
    /**
     * 删除原因
     */
    private String deleteReason;
    
    public static NotificationDeletedEvent from(NotificationAggregate aggregate, String reason) {
        NotificationDeletedEvent event = new NotificationDeletedEvent();
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
        
        // 设置删除相关信息
        event.deletedTime = LocalDateTime.now();
        event.deleteReason = reason;
        
        return event;
    }
} 