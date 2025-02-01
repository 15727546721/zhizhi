package cn.xu.domain.message.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 私信领域模型
 */
@Data
@Accessors(chain = true)
public class PrivateMessage {
    private Long id;
    private Long senderId;
    private Long receiverId;
    private String content;
    private Boolean isRead;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    public static final int TYPE_PRIVATE_MESSAGE = 2;

    public void markAsRead() {
        this.isRead = true;
        this.updateTime = LocalDateTime.now();
    }

    public boolean isUnread() {
        return !isRead;
    }

    public boolean isParticipant(Long userId) {
        return userId.equals(senderId) || userId.equals(receiverId);
    }

    public Long getOtherParticipant(Long userId) {
        return userId.equals(senderId) ? receiverId : senderId;
    }
} 