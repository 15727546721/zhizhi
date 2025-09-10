package cn.xu.domain.message.event;

import cn.xu.domain.message.model.entity.MessageType;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MessageEvent {
    private MessageType type;
    private Long senderId;
    private Long receiverId;
    private String title;
    private String content;
    private Long targetId;

    public static MessageEvent createCommentEvent(Long senderId, Long receiverId, String content, Long articleId) {
        return MessageEvent.builder()
                .type(MessageType.COMMENT)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .targetId(articleId)
                .build();
    }

    public static MessageEvent createLikeEvent(Long senderId, Long receiverId, String content, Long targetId) {
        return MessageEvent.builder()
                .type(MessageType.LIKE)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .targetId(targetId)
                .build();
    }

    public static MessageEvent createFollowEvent(Long senderId, Long receiverId) {
        return MessageEvent.builder()
                .type(MessageType.FOLLOW)
                .senderId(senderId)
                .receiverId(receiverId)
                .content("关注了你")
                .build();
    }
} 