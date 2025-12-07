package cn.xu.event.events;

import cn.xu.event.core.BaseEvent;
import lombok.Getter;

/**
 * 评论事件
 *
 *
 */
@Getter
public class CommentEvent extends BaseEvent {
    
    /** 帖子ID */
    private final Long postId;
    
    /** 评论ID */
    private final Long commentId;
    
    /** 父评论ID（回复时有用） */
    private final Long parentId;
    
    /** 被回复用户ID */
    private final Long replyUserId;
    
    /** 评论内容 */
    private final String content;
    
    /** 是否为根评论 */
    private final boolean rootComment;
    
    private CommentEvent(Builder builder) {
        super(builder.operatorId, builder.action);
        this.postId = builder.postId;
        this.commentId = builder.commentId;
        this.parentId = builder.parentId;
        this.replyUserId = builder.replyUserId;
        this.content = builder.content;
        this.rootComment = builder.rootComment;
    }
    
    public static Builder builder() {
        return new Builder();
    }
    
    public static class Builder {
        private Long operatorId;
        private EventAction action;
        private Long postId;
        private Long commentId;
        private Long parentId;
        private Long replyUserId;
        private String content;
        private boolean rootComment = true;
        
        public Builder operatorId(Long operatorId) {
            this.operatorId = operatorId;
            return this;
        }
        
        public Builder action(EventAction action) {
            this.action = action;
            return this;
        }
        
        public Builder postId(Long postId) {
            this.postId = postId;
            return this;
        }
        
        public Builder commentId(Long commentId) {
            this.commentId = commentId;
            return this;
        }
        
        public Builder parentId(Long parentId) {
            this.parentId = parentId;
            return this;
        }
        
        public Builder replyUserId(Long replyUserId) {
            this.replyUserId = replyUserId;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder rootComment(boolean rootComment) {
            this.rootComment = rootComment;
            return this;
        }
        
        public CommentEvent build() {
            return new CommentEvent(this);
        }
    }
}
