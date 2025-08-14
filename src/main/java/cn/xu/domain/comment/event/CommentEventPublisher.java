package cn.xu.domain.comment.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommentEventPublisher {

    private final ApplicationEventPublisher publisher;

    public void publishCommentCountEvent(CommentCountEvent event) {
        publisher.publishEvent(event);
    }

    public void publishCommentCreatedEvent(CommentCreatedEvent event) {
        publisher.publishEvent(event);
    }

    public void publishCommentLikedEvent(CommentLikedEvent event) {
        publisher.publishEvent(event);
    }

    public void publishCommentEvent(CommentEvent event) {
        publisher.publishEvent(event);
    }
}

