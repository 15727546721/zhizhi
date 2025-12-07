package cn.xu.event.events;

import cn.xu.event.core.BaseEvent;
import lombok.Getter;

/**
 * 关注事件
 *
 *
 */
@Getter
public class FollowEvent extends BaseEvent {
    
    /** 被关注用户ID */
    private final Long followeeId;
    
    /** 是否关注，true-关注，false-取消关注 */
    private final boolean followed;
    
    public FollowEvent(Long followerId, Long followeeId, boolean followed) {
        super(followerId, followed ? EventAction.CREATE : EventAction.DELETE);
        this.followeeId = followeeId;
        this.followed = followed;
    }
    
    /**
     * 获取关注者ID
     */
    public Long getFollowerId() {
        return getOperatorId();
    }
    
    public static FollowEvent follow(Long followerId, Long followeeId) {
        return new FollowEvent(followerId, followeeId, true);
    }
    
    public static FollowEvent unfollow(Long followerId, Long followeeId) {
        return new FollowEvent(followerId, followeeId, false);
    }
}
