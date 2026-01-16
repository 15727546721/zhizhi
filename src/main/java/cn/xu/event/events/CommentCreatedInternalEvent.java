package cn.xu.event.events;

import cn.xu.model.dto.comment.SaveCommentRequest;
import lombok.Getter;
import org.springframework.context.ApplicationEvent;

/**
 * 评论创建内部事件（Spring ApplicationEvent）
 * <p>用于事务提交后的异步处理，如发送通知、清除缓存等</p>
 */
@Getter
public class CommentCreatedInternalEvent extends ApplicationEvent {

    private final SaveCommentRequest request;
    private final Long commentId;

    public CommentCreatedInternalEvent(Object source, SaveCommentRequest request, Long commentId) {
        super(source);
        this.request = request;
        this.commentId = commentId;
    }
}
