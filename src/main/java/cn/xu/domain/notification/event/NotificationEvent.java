package cn.xu.domain.notification.event;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知事件基类
 * 用于在领域内传递通知相关的事件信息
 *
 * @author xuhh
 * @date 2024/03/21
 */
@Data
@NoArgsConstructor
public class NotificationEvent {
    /**
     * 通知ID
     */
    private Long notificationId;
    
    /**
     * 通知类型
     */
    private NotificationType type;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 发送者类型
     */
    private SenderType senderType;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 业务类型
     */
    private BusinessType businessType;
    
    /**
     * 业务ID
     */
    private Long businessId;
    
    /**
     * 额外信息
     */
    private Map<String, Object> extraInfo;
    
    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 从通知聚合根创建事件
     */
    public static NotificationEvent from(NotificationAggregate aggregate) {
        NotificationEvent event = new NotificationEvent();
        event.setNotificationId(aggregate.getNotification().getId());
        event.setType(aggregate.getNotification().getType());
        event.setSenderId(aggregate.getNotification().getSenderId());
        event.setSenderType(aggregate.getNotification().getSenderType());
        event.setReceiverId(aggregate.getNotification().getReceiverId());
        event.setContent(aggregate.getNotification().getContent());
        event.setBusinessType(aggregate.getNotification().getBusinessType());
        event.setBusinessId(aggregate.getNotification().getBusinessId());
        event.setExtraInfo(aggregate.getNotification().getExtraInfo());
        event.setCreatedTime(aggregate.getNotification().getCreatedTime());
        return event;
    }

    /**
     * 将通知事件转换为通知聚合根
     */
    public NotificationAggregate toAggregate() {
        return NotificationAggregate.builder()
            .id(this.notificationId)
            .type(this.type)
            .senderId(this.senderId)
            .senderType(this.senderType)
            .receiverId(this.receiverId)
            .content(this.content)
            .businessType(this.businessType)
            .businessId(this.businessId)
            .extraInfo(this.extraInfo)
            .createdTime(this.createdTime)
            .read(false)
            .status(true)
            .build();
    }
} 