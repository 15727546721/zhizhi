package cn.xu.domain.comment.event;

import com.lmax.disruptor.EventFactory;

public class CommentEventFactory implements EventFactory<CommentCountEvent> {
    @Override
    public CommentCountEvent newInstance() {
        return new CommentCountEvent();
    }
}
