package cn.xu.domain.message.event;

import cn.xu.domain.message.model.entity.MessageType;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class LikeMessageEvent extends BaseMessageEvent {
    public static LikeMessageEvent of(Long senderId, Long receiverId, String content, Long targetId) {
        return LikeMessageEvent.builder()
                .type(MessageType.LIKE)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .targetId(targetId)
                .occurredTime(new Date())
                .build();
    }
} 