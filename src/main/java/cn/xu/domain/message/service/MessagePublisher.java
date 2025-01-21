package cn.xu.domain.message.service;

import cn.xu.domain.message.event.MessageEvent;
import com.lmax.disruptor.RingBuffer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MessagePublisher {

    private final RingBuffer<MessageEvent> messageRingBuffer;

    public void publishMessage(MessageEvent event) {
        long sequence = messageRingBuffer.next();
        try {
            MessageEvent messageEvent = messageRingBuffer.get(sequence);
            messageEvent.setType(event.getType());
            messageEvent.setSenderId(event.getSenderId());
            messageEvent.setReceiverId(event.getReceiverId());
            messageEvent.setTitle(event.getTitle());
            messageEvent.setContent(event.getContent());
            messageEvent.setTargetId(event.getTargetId());
        } finally {
            messageRingBuffer.publish(sequence);
        }
    }
} 