package cn.xu.domain.notification.model.valueobject;

import lombok.Getter;

/**
 * 通知发送者值对象
 */
@Getter
public class NotificationSender {
    private final Long senderId;
    private final SenderType senderType;
    private final String senderName;
    private final String senderAvatar;

    private NotificationSender(Long senderId, SenderType senderType, String senderName, String senderAvatar) {
        this.senderId = senderId;
        this.senderType = senderType;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
    }

    public static NotificationSender of(Long senderId, SenderType senderType, String senderName, String senderAvatar) {
        return new NotificationSender(senderId, senderType, senderName, senderAvatar);
    }

    public static NotificationSender system() {
        return new NotificationSender(null, SenderType.SYSTEM, "系统", null);
    }

    public boolean isUser() {
        return SenderType.USER.equals(senderType);
    }

    public boolean isSystem() {
        return SenderType.SYSTEM.equals(senderType);
    }
} 