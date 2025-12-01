package cn.xu.model.entity;

import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 关注关系实体
 *
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Follow implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ==================== 状态常量 ====================
    
    /** 已关注状态 */
    public static final int STATUS_FOLLOWED = 1;
    /** 取消关注状态 */
    public static final int STATUS_UNFOLLOWED = 0;
    
    // ==================== 字段 ====================
    
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
     * 关注状态（0-取消关注，1-已关注）
     */
    private Integer status;

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
     */
    public static Follow createFollow(Long followerId, Long followedId) {
        validateFollowRelation(followerId, followedId);
        
        LocalDateTime now = LocalDateTime.now();
        return Follow.builder()
                .followerId(followerId)
                .followedId(followedId)
                .status(STATUS_FOLLOWED)
                .createTime(now)
                .updateTime(now)
                .build();
    }
    
    /**
     * 关注操作
     */
    public void follow() {
        if (isFollowed()) {
            throw new BusinessException("已经关注，无需重复操作");
        }
        this.status = STATUS_FOLLOWED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 取消关注操作
     */
    public void unfollow() {
        if (!isFollowed()) {
            throw new BusinessException("尚未关注，无法取消");
        }
        this.status = STATUS_UNFOLLOWED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 判断是否已关注
     */
    public boolean isFollowed() {
        return STATUS_FOLLOWED == this.status;
    }
    
    /**
     * 判断关注关系是否有效
     */
    public boolean isValid() {
        return followerId != null && followerId > 0 
            && followedId != null && followedId > 0
            && !followerId.equals(followedId);
    }
    
    /**
     * 验证关注关系的有效性
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
     * 验证业务规则
     */
    public void validate() {
        validateFollowRelation(this.followerId, this.followedId);
    }
    
    /**
     * 获取关注关系的简化信息（用于日志）
     */
    public String getSimpleInfo() {
        return String.format("Follow[%d:%d->%d:%s]", id, followerId, followedId, 
            isFollowed() ? "FOLLOWED" : "UNFOLLOWED");
    }
}