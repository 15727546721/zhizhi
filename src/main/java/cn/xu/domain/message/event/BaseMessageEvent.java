package cn.xu.domain.message.event;

import cn.xu.domain.message.model.entity.MessageType;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@Data
@SuperBuilder
public abstract class BaseMessageEvent {
    private MessageType type;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private Long targetId;
    private Date occurredTime;

    // 验证事件数据
    public void validate() {
        if (type == null) {
            throw new IllegalArgumentException("消息类型不能为空");
        }
        if (receiverId == null) {
            throw new IllegalArgumentException("接收者不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }
} 