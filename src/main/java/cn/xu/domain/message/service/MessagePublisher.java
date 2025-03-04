package cn.xu.domain.message.service;

import cn.xu.domain.message.event.BaseMessageEvent;
import com.lmax.disruptor.RingBuffer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessagePublisher {

    private final RingBuffer<BaseMessageEvent> messageRingBuffer;

    public void publishMessage(BaseMessageEvent event) {
        // 验证事件数据
        event.validate();
        
        long sequence = messageRingBuffer.next();
        try {
            BaseMessageEvent messageEvent = messageRingBuffer.get(sequence);
            // 复制事件数据
            messageEvent.setType(event.getType());
            messageEvent.setSenderId(event.getSenderId());
            messageEvent.setReceiverId(event.getReceiverId());
            messageEvent.setTitle(event.getTitle());
            messageEvent.setContent(event.getContent());
            messageEvent.setTargetId(event.getTargetId());
            messageEvent.setOccurredTime(event.getOccurredTime());
        } finally {
            messageRingBuffer.publish(sequence);
        }
    }
} 