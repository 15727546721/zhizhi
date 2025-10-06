package cn.xu.domain.follow.model.aggregate;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.follow.model.entity.FollowRelationEntity;
import cn.xu.domain.follow.model.valueobject.FollowStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 关注关系聚合根
 * 管理关注关系的完整生命周期和业务一致性
 */
@Data
@Slf4j
public class FollowAggregate {
    
    /**
     * 关注关系实体（聚合根实体）
     */
    private FollowRelationEntity followRelation;
    
    /**
     * 领域事件列表
     */
    private List<Object> domainEvents = new ArrayList<>();
    
    /**
     * 私有构造函数
     */
    private FollowAggregate() {
        this.domainEvents = new ArrayList<>();
    }
    
    /**
     * 创建新的关注关系聚合根
     * 
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 关注关系聚合根
     */
    public static FollowAggregate create(Long followerId, Long followedId) {
        // 验证关注关系的有效性
        FollowRelationEntity.validateFollowRelation(followerId, followedId);
        
        // 创建关注关系实体
        FollowRelationEntity followRelation = FollowRelationEntity.createFollow(followerId, followedId);
        
        FollowAggregate aggregate = new FollowAggregate();
        aggregate.followRelation = followRelation;
        
        // 添加关注事件
        aggregate.addDomainEvent(new FollowCreatedEvent(
            followRelation.getId(), 
            followerId, 
            followedId, 
            LocalDateTime.now()
        ));
        
        return aggregate;
    }
    
    /**
     * 从持久化数据恢复聚合根
     * 
     * @param id 关注关系ID
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @param status 关注状态
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @return 关注关系聚合根
     */
    public static FollowAggregate restore(Long id, Long followerId, Long followedId, 
                                         FollowStatus status, LocalDateTime createTime, 
                                         LocalDateTime updateTime) {
        FollowRelationEntity followRelation = FollowRelationEntity.builder()
                .id(id)
                .followerId(followerId)
                .followedId(followedId)
                .status(status)
                .createTime(createTime)
                .updateTime(updateTime)
                .build();
        
        FollowAggregate aggregate = new FollowAggregate();
        aggregate.followRelation = followRelation;
        
        return aggregate;
    }
    
    /**
     * 关注操作
     */
    public void follow() {
        if (followRelation == null) {
            throw new BusinessException("关注关系实体不能为空");
        }
        
        // 如果已经是关注状态，直接返回
        if (followRelation.isFollowed()) {
            return;
        }
        
        // 执行关注操作
        followRelation.follow();
        
        // 添加关注事件
        addDomainEvent(new FollowedEvent(
            followRelation.getId(),
            followRelation.getFollowerId(),
            followRelation.getFollowedId(),
            LocalDateTime.now()
        ));
    }
    
    /**
     * 取消关注操作
     */
    public void unfollow() {
        if (followRelation == null) {
            throw new BusinessException("关注关系实体不能为空");
        }
        
        // 如果已经是取消关注状态，直接返回
        if (!followRelation.isFollowed()) {
            return;
        }
        
        // 执行取消关注操作
        followRelation.unfollow();
        
        // 添加取消关注事件
        addDomainEvent(new UnfollowedEvent(
            followRelation.getId(),
            followRelation.getFollowerId(),
            followRelation.getFollowedId(),
            LocalDateTime.now()
        ));
    }
    
    /**
     * 验证关注关系的有效性
     */
    public void validate() {
        if (followRelation == null) {
            throw new BusinessException("关注关系实体不能为空");
        }
        
        if (!followRelation.isValid()) {
            throw new BusinessException("关注关系无效");
        }
    }
    
    /**
     * 获取聚合根ID
     */
    public Long getId() {
        return followRelation != null ? followRelation.getId() : null;
    }
    
    /**
     * 获取关注者ID
     */
    public Long getFollowerId() {
        return followRelation != null ? followRelation.getFollowerId() : null;
    }
    
    /**
     * 获取被关注者ID
     */
    public Long getFollowedId() {
        return followRelation != null ? followRelation.getFollowedId() : null;
    }
    
    /**
     * 判断是否已关注
     */
    public boolean isFollowed() {
        return followRelation != null && followRelation.isFollowed();
    }
    
    /**
     * 添加领域事件
     */
    private void addDomainEvent(Object event) {
        if (this.domainEvents == null) {
            this.domainEvents = new ArrayList<>();
        }
        this.domainEvents.add(event);
        log.debug("添加领域事件: {}", event.getClass().getSimpleName());
    }
    
    /**
     * 获取并清空领域事件
     */
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }
    
    // ==================== 内部事件类定义 ====================
    
    /**
     * 关注关系创建事件
     */
    public static class FollowCreatedEvent {
        private final Long followId;
        private final Long followerId;
        private final Long followedId;
        private final LocalDateTime createTime;
        
        public FollowCreatedEvent(Long followId, Long followerId, Long followedId, LocalDateTime createTime) {
            this.followId = followId;
            this.followerId = followerId;
            this.followedId = followedId;
            this.createTime = createTime;
        }
        
        // getters
        public Long getFollowId() { return followId; }
        public Long getFollowerId() { return followerId; }
        public Long getFollowedId() { return followedId; }
        public LocalDateTime getCreateTime() { return createTime; }
    }
    
    /**
     * 关注事件
     */
    public static class FollowedEvent {
        private final Long followId;
        private final Long followerId;
        private final Long followedId;
        private final LocalDateTime followTime;
        
        public FollowedEvent(Long followId, Long followerId, Long followedId, LocalDateTime followTime) {
            this.followId = followId;
            this.followerId = followerId;
            this.followedId = followedId;
            this.followTime = followTime;
        }
        
        // getters
        public Long getFollowId() { return followId; }
        public Long getFollowerId() { return followerId; }
        public Long getFollowedId() { return followedId; }
        public LocalDateTime getFollowTime() { return followTime; }
    }
    
    /**
     * 取消关注事件
     */
    public static class UnfollowedEvent {
        private final Long followId;
        private final Long followerId;
        private final Long followedId;
        private final LocalDateTime unfollowTime;
        
        public UnfollowedEvent(Long followId, Long followerId, Long followedId, LocalDateTime unfollowTime) {
            this.followId = followId;
            this.followerId = followerId;
            this.followedId = followedId;
            this.unfollowTime = unfollowTime;
        }
        
        // getters
        public Long getFollowId() { return followId; }
        public Long getFollowerId() { return followerId; }
        public Long getFollowedId() { return followedId; }
        public LocalDateTime getUnfollowTime() { return unfollowTime; }
    }
}