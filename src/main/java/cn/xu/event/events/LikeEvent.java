package cn.xu.event.events;

import cn.xu.event.core.BaseEvent;
import lombok.Getter;

/**
 * 点赞事件
 *
 *
 */
@Getter
public class LikeEvent extends BaseEvent {
    
    /** 目标ID（帖子ID或评论ID） */
    private final Long targetId;
    
    /** 点赞类型 */
    private final LikeType type;
    
    /** 点赞状态：true-点赞，false-取消 */
    private final boolean liked;
    
    public enum LikeType {
        POST,
        COMMENT
    }
    
    public LikeEvent(Long operatorId, Long targetId, LikeType type, boolean liked) {
        super(operatorId, liked ? EventAction.CREATE : EventAction.DELETE);
        this.targetId = targetId;
        this.type = type;
        this.liked = liked;
    }
    
    public static LikeEvent likePost(Long userId, Long postId) {
        return new LikeEvent(userId, postId, LikeType.POST, true);
    }
    
    public static LikeEvent unlikePost(Long userId, Long postId) {
        return new LikeEvent(userId, postId, LikeType.POST, false);
    }
    
    public static LikeEvent likeComment(Long userId, Long commentId) {
        return new LikeEvent(userId, commentId, LikeType.COMMENT, true);
    }
    
    public static LikeEvent unlikeComment(Long userId, Long commentId) {
        return new LikeEvent(userId, commentId, LikeType.COMMENT, false);
    }
}
