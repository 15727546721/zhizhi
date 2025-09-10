package cn.xu.domain.follow.event;

import cn.xu.domain.follow.model.valueobject.FollowStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FollowEvent {
    /**
     *关注者ID
     */
    private Long followerId;
    /**
     * 被关注者ID
     */
    private Long followeeId;

    /**
     * status 1:关注 0:取消关注
     */
    private FollowStatus status;
}
