package cn.xu.domain.message.event;

import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.service.IMessageService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class MessageEventListener {

    private final IMessageService messageService;

    public MessageEventListener(IMessageService messageService) {
        this.messageService = messageService;
    }

    @EventListener
    public void handleCommentMessageEvent(CommentMessageEvent event) {
        event.validate();
        MessageEntity message = MessageEntity.builder()
                .senderId(event.getSenderId())
                .type(event.getType())
                .receiverId(event.getReceiverId())
                .targetId(event.getTargetId())
                .title(event.getTitle())
                .content(event.getContent())
                .isRead(false)
                .createTime(event.getOccurredTime())
                .updateTime(event.getOccurredTime())
                .build();

        messageService.sendMessage(message);
    }

    @EventListener
    public void handleLikeMessageEvent(LikeMessageEvent event) {
        event.validate();
        MessageEntity message = MessageEntity.builder()
                .senderId(event.getSenderId())
                .type(event.getType())
                .receiverId(event.getReceiverId())
                .targetId(event.getTargetId())
                .title(event.getTitle())
                .content(event.getContent())
                .isRead(false)
                .createTime(event.getOccurredTime())
                .updateTime(event.getOccurredTime())
                .build();

        messageService.sendMessage(message);
    }

    @EventListener
    public void handleSystemMessageEvent(SystemMessageEvent event) {
        event.validate();
        MessageEntity message = MessageEntity.builder()
                .senderId(null)
                .type(event.getType())
                .receiverId(event.getReceiverId())
                .targetId(event.getTargetId())
                .title(event.getTitle())
                .content(event.getContent())
                .isRead(false)
                .createTime(event.getOccurredTime())
                .updateTime(event.getOccurredTime())
                .build();

        messageService.sendMessage(message);
    }
}
