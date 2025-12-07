package cn.xu.event.events;

import cn.xu.event.core.BaseEvent;
import lombok.Getter;

/**
 * 收藏事件
 *
 * 
 */
@Getter
public class FavoriteEvent extends BaseEvent {
    
    /** 帖子ID */
    private final Long postId;
    
    /** 是否收藏，true-收藏，false-取消 */
    private final boolean favorited;
    
    public FavoriteEvent(Long operatorId, Long postId, boolean favorited) {
        super(operatorId, favorited ? EventAction.CREATE : EventAction.DELETE);
        this.postId = postId;
        this.favorited = favorited;
    }
    
    public static FavoriteEvent favorite(Long userId, Long postId) {
        return new FavoriteEvent(userId, postId, true);
    }
    
    public static FavoriteEvent unfavorite(Long userId, Long postId) {
        return new FavoriteEvent(userId, postId, false);
    }
}
