package cn.xu.event.follow;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关注事件
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowEvent {
    /**
     * 关注者ID
     */
    private Long followerId;
    
    /**
     * 被关注者ID
     */
    private Long followeeId;

    /**
     * 关注状态：1-关注，0-取消关注
     */
    private Integer status;
}
