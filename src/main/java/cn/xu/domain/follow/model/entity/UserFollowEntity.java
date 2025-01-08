package cn.xu.domain.follow.model.entity;

import cn.xu.domain.follow.model.valueobject.FollowStatus;
import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Accessors(chain = true)
public class UserFollowEntity {
    /**
     * 关注关系ID
     */
    private Long id;

    /**
     * 关注者ID
     */
    private Long followerId;

    /**
     * 被关注者ID
     */
    private Long followedId;

    /**
     * 关注状态
     */
    private FollowStatus status;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 