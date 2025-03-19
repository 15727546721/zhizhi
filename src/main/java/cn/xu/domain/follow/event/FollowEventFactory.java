package cn.xu.domain.follow.event;

import com.lmax.disruptor.EventFactory;

public class FollowEventFactory implements EventFactory<FollowEvent> {
    @Override
    public FollowEvent newInstance() {
        return new FollowEvent();
    }
}
