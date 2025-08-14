package cn.xu.domain.message.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageEntity {
    private Long id;
    private MessageType type;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private Long targetId;
    private Boolean isRead;
    private Date createTime;
    private Date updateTime;

    public void markAsRead() {
        this.isRead = true;
        this.updateTime = new Date();
    }

    public boolean isSystemMessage() {
        return MessageType.SYSTEM.equals(this.type);
    }

    public void validate() {
        if (this.type == null) {
            throw new IllegalArgumentException("消息类型不能为空");
        }
        if (this.receiverId == null) {
            throw new IllegalArgumentException("接收者不能为空");
        }
        if (this.content == null || this.content.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }
} 