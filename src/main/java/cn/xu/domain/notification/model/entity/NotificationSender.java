package cn.xu.domain.notification.model.entity;

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
    private final String senderName;
    private final String senderAvatar;

    public static NotificationSender of(Long senderId, String senderName, String senderAvatar) {
        return NotificationSender.builder()
                .senderId(senderId)
                .senderName(senderName)
                .senderAvatar(senderAvatar)
                .build();
    }

    /**
     * 创建系统发送者
     *
     * @return 系统发送者实例
     */
    public static NotificationSender system() {
        return NotificationSender.builder()
                .senderId(0L)
                .senderName("系统")
                .senderAvatar(null)
                .build();
    }

} 