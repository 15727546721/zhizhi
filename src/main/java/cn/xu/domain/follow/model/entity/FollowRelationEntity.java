package cn.xu.domain.follow.model.entity;

import cn.xu.domain.follow.model.valueobject.FollowStatus;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Accessors(chain = true)
public class FollowRelationEntity {
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
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建新的关注关系
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 关注关系实体
     */
    public static FollowRelationEntity createFollow(Long followerId, Long followedId) {
        validateFollowRelation(followerId, followedId);
        
        return FollowRelationEntity.builder()
                .followerId(followerId)
                .followedId(followedId)
                .status(FollowStatus.FOLLOWED)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 关注操作
     */
    public void follow() {
        this.status = FollowStatus.FOLLOWED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 取消关注操作
     */
    public void unfollow() {
        this.status = FollowStatus.UNFOLLOWED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 判断是否已关注
     * 
     * @return true表示已关注，false表示未关注
     */
    public boolean isFollowed() {
        return this.status != null && this.status == FollowStatus.FOLLOWED;
    }
    
    /**
     * 判断关注关系是否有效
     * 
     * @return true表示有效，false表示无效
     */
    public boolean isValid() {
        return followerId != null && followerId > 0 
            && followedId != null && followedId > 0
            && !followerId.equals(followedId);
    }
    
    /**
     * 验证关注关系的有效性
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     */
    public static void validateFollowRelation(Long followerId, Long followedId) {
        if (followerId == null || followerId <= 0) {
            throw new BusinessException("关注者ID不能为空");
        }
        if (followedId == null || followedId <= 0) {
            throw new BusinessException("被关注者ID不能为空");
        }
        if (followerId.equals(followedId)) {
            throw new BusinessException("不能关注自己");
        }
    }
    
    /**
     * 获取关注关系的简化信息（用于日志）
     */
    public String getSimpleInfo() {
        return String.format("Follow[%d:%d->%d:%s]", id, followerId, followedId, status);
    }
}