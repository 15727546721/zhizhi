package cn.xu.domain.notification.event;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;
import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import cn.xu.domain.notification.model.valueobject.SenderType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 通知事件基类
 * 用于在领域内传递通知相关的事件信息
 *
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
    private BusinessType notificationBusinessType;
    
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

        return NotificationEvent.builder()
                .notificationId(aggregate.getNotification().getId())
                .type(aggregate.getNotification().getType())
                .senderId(aggregate.getNotification().getSenderId())
                .senderType(aggregate.getNotification().getSenderType())
                .receiverId(aggregate.getNotification().getReceiverId())
                .content(aggregate.getNotification().getContent())
                .notificationBusinessType(aggregate.getNotification().getNotificationBusinessType())
                .businessId(aggregate.getNotification().getBusinessId())
                .extraInfo(aggregate.getNotification().getExtraInfo())
                .createdTime(aggregate.getNotification().getCreatedTime())
                .build();
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
            .businessType(this.notificationBusinessType)
            .businessId(this.businessId)
            .extraInfo(this.extraInfo)
            .createdTime(this.createdTime)
            .read(false)
            .status(true)
            .build();
    }
} 