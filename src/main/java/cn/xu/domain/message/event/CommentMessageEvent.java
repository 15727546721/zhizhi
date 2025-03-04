package cn.xu.domain.message.event;

import cn.xu.domain.message.model.entity.MessageType;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

import java.util.Date;

@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class CommentMessageEvent extends BaseMessageEvent {
    public static CommentMessageEvent of(Long senderId, Long receiverId, String content, Long articleId) {
        return CommentMessageEvent.builder()
                .type(MessageType.COMMENT)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .targetId(articleId)
                .occurredTime(new Date())
                .build();
    }
} 