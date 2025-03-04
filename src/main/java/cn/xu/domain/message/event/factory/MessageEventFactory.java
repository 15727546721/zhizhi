package cn.xu.domain.message.event.factory;

import cn.xu.domain.message.event.BaseMessageEvent;
import cn.xu.domain.message.event.CommentMessageEvent;
import cn.xu.domain.message.event.LikeMessageEvent;
import cn.xu.domain.message.event.SystemMessageEvent;
import com.lmax.disruptor.EventFactory;
import org.springframework.stereotype.Component;

@Component
public class MessageEventFactory implements EventFactory<BaseMessageEvent> {
    
    public BaseMessageEvent createCommentEvent(Long senderId, Long receiverId, String content, Long articleId) {
        return CommentMessageEvent.of(senderId, receiverId, content, articleId);
    }
    
    public BaseMessageEvent createLikeEvent(Long senderId, Long receiverId, String content, Long targetId) {
        return LikeMessageEvent.of(senderId, receiverId, content, targetId);
    }
    
    public BaseMessageEvent createSystemEvent(String title, String content, Long receiverId) {
        return SystemMessageEvent.of(title, content, receiverId);
    }

    @Override
    public BaseMessageEvent newInstance() {
        return SystemMessageEvent.builder()
                .build();
    }
}