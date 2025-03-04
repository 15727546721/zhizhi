package cn.xu.domain.like.event.factory;

import cn.xu.domain.like.event.LikeEvent;
import com.lmax.disruptor.EventFactory;

/**
 * 事件工厂
 */
public class LikeEventFactory implements EventFactory<LikeEvent> {
    @Override
    public LikeEvent newInstance() {
         return LikeEvent.builder()
                .userId(null)
                .targetId(null)
                .type(null)
                .liked(false)
                .occurredTime(null)
                .build();
    }
}
