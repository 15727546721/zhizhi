package cn.xu.event.events;

import cn.xu.event.core.BaseEvent;
import lombok.Getter;

/**
 * 帖子事件
 *
 *
 */
@Getter
public class PostEvent extends BaseEvent {
    
    /** 帖子ID */
    private final Long postId;
    
    /** 帖子标题 */
    private final String title;
    
    public PostEvent(Long operatorId, Long postId, String title, EventAction action) {
        super(operatorId, action);
        this.postId = postId;
        this.title = title;
    }
    
    public static PostEvent created(Long userId, Long postId, String title) {
        return new PostEvent(userId, postId, title, EventAction.CREATE);
    }
    
    public static PostEvent updated(Long userId, Long postId, String title) {
        return new PostEvent(userId, postId, title, EventAction.UPDATE);
    }
    
    public static PostEvent deleted(Long userId, Long postId) {
        return new PostEvent(userId, postId, null, EventAction.DELETE);
    }
}
