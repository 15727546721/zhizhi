package cn.xu.domain.like.model.aggregate;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.like.model.LikeStatus;
import cn.xu.domain.like.model.LikeType;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 点赞聚合根
 * 管理点赞关系的一致性边界
 * 封装点赞相关的业务逻辑和领域事件
 */
@Data
@Builder
@Slf4j
public class LikeAggregate {
    
    /**
     * 聚合根ID（与点赞实体ID相同）
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 目标ID
     */
    private Long targetId;
    
    /**
     * 点赞类型：1-文章，2-话题，3-评论等
     */
    private LikeType type;
    
    /**
     * 是否点赞，1-点赞，0-取消点赞
     */
    private LikeStatus status;
    
    /**
     * 点赞时间
     */
    private LocalDateTime createTime;
    
    /**
     * 领域事件列表
     */
    private List<Object> domainEvents = new ArrayList<>();
    
    // ==================== 聚合根业务方法 ====================
    
    /**
     * 创建新的点赞聚合根
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞聚合根
     */
    public static LikeAggregate create(Long userId, Long targetId, LikeType type) {
        // 验证点赞关系的有效性
        validateLikeRelation(userId, targetId, type);
        
        LikeAggregate aggregate = LikeAggregate.builder()
                .userId(userId)
                .targetId(targetId)
                .type(type)
                .status(LikeStatus.LIKED) // 表示点赞
                .createTime(LocalDateTime.now())
                .domainEvents(new ArrayList<>())
                .build();
        
        // 设置聚合根ID
        aggregate.id = aggregate.generateId();
        
        // 添加点赞事件
        aggregate.addDomainEvent(new LikedEvent(
            aggregate.id,
            userId,
            targetId,
            type,
            LocalDateTime.now()
        ));
        
        return aggregate;
    }
    
    /**
     * 从持久化数据恢复聚合根
     * 
     * @param id 点赞ID
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     * @param status 点赞状态
     * @param createTime 创建时间
     * @param updateTime 更新时间
     * @return 点赞聚合根
     */
    public static LikeAggregate restore(Long id, Long userId, Long targetId, 
                                      LikeType type, LikeStatus status, 
                                      LocalDateTime createTime, LocalDateTime updateTime) {
        LikeAggregate aggregate = LikeAggregate.builder()
                .id(id)
                .userId(userId)
                .targetId(targetId)
                .type(type)
                .status(status)
                .createTime(createTime)
                .domainEvents(new ArrayList<>())
                .build();
        
        return aggregate;
    }
    
    /**
     * 点赞操作
     */
    public void like() {
        // 如果已经是点赞状态，直接返回
        if (isLiked()) {
            log.debug("点赞操作：用户 {} 已经点赞目标 {}，无需重复操作", userId, targetId);
            return;
        }
        
        // 执行点赞操作
        this.status = LikeStatus.LIKED;
        this.createTime = LocalDateTime.now();
        
        // 添加点赞事件
        addDomainEvent(new LikedEvent(
            this.id,
            this.userId,
            this.targetId,
            this.type,
            LocalDateTime.now()
        ));
        
        log.info("点赞操作成功：用户 {} 点赞目标 {}", userId, targetId);
    }
    
    /**
     * 取消点赞操作
     */
    public void unlike() {
        // 如果已经是取消点赞状态，直接返回
        if (!isLiked()) {
            log.debug("取消点赞操作：用户 {} 尚未点赞目标 {}，无需取消", userId, targetId);
            return;
        }
        
        // 执行取消点赞操作
        this.status = LikeStatus.UNLIKED;
        
        // 添加取消点赞事件
        addDomainEvent(new UnlikedEvent(
            this.id,
            this.userId,
            this.targetId,
            this.type,
            LocalDateTime.now()
        ));
        
        log.info("取消点赞操作成功：用户 {} 取消点赞目标 {}", userId, targetId);
    }
    
    /**
     * 验证点赞关系的有效性
     */
    public void validate() {
        validateLikeRelation(this.userId, this.targetId, this.type);
    }
    
    /**
     * 验证点赞关系的有效性
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public static void validateLikeRelation(Long userId, Long targetId, LikeType type) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("目标ID不能为空");
        }
        if (type == null) {
            throw new BusinessException("点赞类型不能为空");
        }
    }
    
    /**
     * 获取聚合根ID
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return this.userId;
    }
    
    /**
     * 获取目标ID
     */
    public Long getTargetId() {
        return this.targetId;
    }
    
    /**
     * 获取点赞类型
     */
    public LikeType getType() {
        return this.type;
    }
    
    /**
     * 判断是否已点赞
     */
    public boolean isLiked() {
        return this.status != null && this.status == LikeStatus.LIKED;
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
    
    /**
     * 生成聚合根ID（简单实现，实际项目中可能需要更复杂的ID生成策略）
     */
    private Long generateId() {
        // 这里只是一个示例实现，实际项目中应该使用更合适的ID生成策略
        // 由于数据库使用自增ID，这里返回null，让数据库生成ID
        return null;
    }
    
    // ==================== 内部事件类定义 ====================
    
    /**
     * 点赞事件
     */
    public static class LikedEvent {
        private final Long likeId;
        private final Long userId;
        private final Long targetId;
        private final LikeType type;
        private final LocalDateTime likeTime;
        
        public LikedEvent(Long likeId, Long userId, Long targetId, LikeType type, LocalDateTime likeTime) {
            this.likeId = likeId;
            this.userId = userId;
            this.targetId = targetId;
            this.type = type;
            this.likeTime = likeTime;
        }
        
        // getters
        public Long getLikeId() { return likeId; }
        public Long getUserId() { return userId; }
        public Long getTargetId() { return targetId; }
        public LikeType getType() { return type; }
        public LocalDateTime getLikeTime() { return likeTime; }
    }
    
    /**
     * 取消点赞事件
     */
    public static class UnlikedEvent {
        private final Long likeId;
        private final Long userId;
        private final Long targetId;
        private final LikeType type;
        private final LocalDateTime unlikeTime;
        
        public UnlikedEvent(Long likeId, Long userId, Long targetId, LikeType type, LocalDateTime unlikeTime) {
            this.likeId = likeId;
            this.userId = userId;
            this.targetId = targetId;
            this.type = type;
            this.unlikeTime = unlikeTime;
        }
        
        // getters
        public Long getLikeId() { return likeId; }
        public Long getUserId() { return userId; }
        public Long getTargetId() { return targetId; }
        public LikeType getType() { return type; }
        public LocalDateTime getUnlikeTime() { return unlikeTime; }
    }
}