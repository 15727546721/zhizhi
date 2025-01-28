package cn.xu.domain.notification.model.entity;

import cn.xu.domain.notification.model.valueobject.SenderType;
import lombok.Builder;
import lombok.Getter;

/**
 * 通知发送者值对象
 * 封装发送者的相关属性，确保发送者信息的不可变性
 */
@Getter
@Builder
public class NotificationSender {
    
    private final Long senderId;
    private final SenderType senderType;
    private final String senderName;
    private final String senderAvatar;

    public static NotificationSender of(Long senderId, SenderType senderType, String senderName, String senderAvatar) {
        return NotificationSender.builder()
                .senderId(senderId)
                .senderType(senderType)
                .senderName(senderName)
                .senderAvatar(senderAvatar)
                .build();
    }

    /**
     * 创建系统发送者
     * @return 系统发送者实例
     */
    public static NotificationSender system() {
        return NotificationSender.builder()
                .senderId(0L)
                .senderType(SenderType.SYSTEM)
                .senderName("系统")
                .senderAvatar(null)
                .build();
    }

    /**
     * 验证发送者信息是否有效
     * @return 是否有效
     */
    public boolean isValid() {
        return senderId != null && senderType != null;
    }
} 