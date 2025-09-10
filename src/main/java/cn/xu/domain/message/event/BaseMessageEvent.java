package cn.xu.domain.message.event;

import cn.xu.domain.message.model.entity.MessageType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@EqualsAndHashCode
public abstract class BaseMessageEvent {
    private MessageType type;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private Long targetId;
    private Date occurredTime;

    public BaseMessageEvent(Long senderId, Long receiverId, String content, Long targetId, MessageType type) {
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.content = content;
        this.targetId = targetId;
        this.type = type;
        this.occurredTime = new Date();
    }

    public abstract void validate();
}