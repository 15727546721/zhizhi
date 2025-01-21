package cn.xu.domain.message.event.handler;

import cn.xu.domain.message.event.MessageEvent;
import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.service.IMessageService;
import com.lmax.disruptor.EventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
@RequiredArgsConstructor
public class MessageEventHandler implements EventHandler<MessageEvent> {
    
    private final IMessageService messageService;

    @Override
    public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) {
        MessageEntity message = MessageEntity.builder()
                .type(event.getType())
                .senderId(event.getSenderId())
                .receiverId(event.getReceiverId())
                .title(event.getTitle())
                .content(event.getContent())
                .targetId(event.getTargetId())
                .isRead(false)
                .createTime(new Date())
                .updateTime(new Date())
                .build();
                
        messageService.sendMessage(message);
    }
} 