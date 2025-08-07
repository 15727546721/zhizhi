package cn.xu.domain.like.event;

import cn.xu.domain.like.model.LikeType;
import com.lmax.disruptor.RingBuffer;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class LikeEventPublisher {

    private final RingBuffer<LikeEvent> ringBuffer;

    public LikeEventPublisher(RingBuffer<LikeEvent> ringBuffer) {
        this.ringBuffer = ringBuffer;
    }

    public void publish(Long userId, Long targetId, LikeType targetType, Boolean likeStatus) {
        long seq = ringBuffer.next();
        try {
            LikeEvent event = ringBuffer.get(seq);
            event.setUserId(userId);
            event.setTargetId(targetId);
            event.setStatus(likeStatus);
            event.setType(targetType);
            event.setCreateTime(LocalDateTime.now());
        } finally {
            ringBuffer.publish(seq);
        }
    }
}

