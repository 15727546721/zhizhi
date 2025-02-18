package cn.xu.domain.notification.model.valueobject;

import lombok.Getter;

/**
 * 通知发送者值对象
 */
@Getter
public class NotificationSender {
    private final Long senderId;
    private final String senderName;
    private final String senderAvatar;

    private NotificationSender(Long senderId, String senderName, String senderAvatar) {
        this.senderId = senderId;
        this.senderName = senderName;
        this.senderAvatar = senderAvatar;
    }

    public static NotificationSender of(Long senderId, String senderName, String senderAvatar) {
        return new NotificationSender(senderId, senderName, senderAvatar);
    }

    public static NotificationSender system() {
        return new NotificationSender(null, "系统", null);
    }

}