package cn.xu.domain.message.event;

import cn.xu.domain.message.model.entity.MessageType;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class SystemMessageEvent extends BaseMessageEvent {
    public static SystemMessageEvent of(String title, String content, Long receiverId) {
        return SystemMessageEvent.builder()
                .type(MessageType.SYSTEM)
                .title(title)
                .content(content)
                .receiverId(receiverId)
                .occurredTime(new Date())
                .build();
    }
} 