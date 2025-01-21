package cn.xu.domain.message.event.handler;

import cn.xu.domain.message.event.BaseMessageEvent;
import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.service.IMessageService;
import com.lmax.disruptor.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessageEventHandler implements EventHandler<BaseMessageEvent> {
    
    private final IMessageService messageService;

    @Override
    public void onEvent(BaseMessageEvent event, long sequence, boolean endOfBatch) {
        MessageEntity message = MessageEntity.builder()
                .type(event.getType())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .title(event.getTitle())
                .content(event.getContent())
                .targetId(event.getTargetId())
                .isRead(false)
                .createTime(event.getOccurredTime())
                .updateTime(event.getOccurredTime())
                .build();
                
        messageService.sendMessage(message);
    }
} 