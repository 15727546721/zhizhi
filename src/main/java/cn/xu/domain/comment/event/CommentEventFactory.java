package cn.xu.domain.comment.event;

import com.lmax.disruptor.EventFactory;

public class CommentEventFactory implements EventFactory<CommentEvent> {
    @Override
    public CommentEvent newInstance() {
        return new CommentEvent();
    }
}
