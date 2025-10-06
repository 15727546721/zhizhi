package cn.xu.domain.like.service;

import cn.xu.common.exception.BusinessException;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.model.aggregate.LikeAggregate;
import cn.xu.domain.like.repository.ILikeAggregateRepository;
import cn.xu.infrastructure.transaction.TransactionParticipant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * 点赞领域服务
 * 处理点赞相关的业务逻辑，实现事务参与者接口以支持分布式事务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeDomainService implements TransactionParticipant {
    
    private final ILikeAggregateRepository likeAggregateRepository;
    private final LikeStatisticsService likeStatisticsService;
    private final LikeHotDataService likeHotDataService;
    private final LikeDataConsistencyService likeDataConsistencyService;
    
    // 用于事务管理的临时存储
    private final ThreadLocal<LikeOperation> tempOperation = new ThreadLocal<>();
    
    /**
     * 执行点赞操作
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void doLike(Long userId, Long targetId, LikeType type) {
        // 1. 查找现有的点赞关系
        Optional<LikeAggregate> existingLikeOpt = likeAggregateRepository.findByUserAndTarget(userId, type, targetId);
        
        if (existingLikeOpt.isPresent()) {
            // 2. 如果已存在点赞关系，检查当前状态
            LikeAggregate likeAggregate = existingLikeOpt.get();
            
            // 如果已经是点赞状态，抛出异常
            if (likeAggregate.isLiked()) {
                log.warn("[点赞操作] 用户已点赞，无需重复操作 - userId: {}, targetId: {}", userId, targetId);
                throw new BusinessException("您已点赞，请勿重复操作");
            }
            
            // 执行点赞操作
            likeAggregate.like();
            
            // 如果在事务中，暂存操作
            if (tempOperation.get() != null) {
                tempOperation.get().setOperation(LikeOperationType.LIKE);
                tempOperation.get().setLikeAggregate(likeAggregate);
                log.debug("在事务中暂存点赞操作: userId={}, targetId={}, type={}", userId, targetId, type);
                return;
            }
            
            likeAggregateRepository.update(likeAggregate);
        } else {
            // 3. 如果不存在点赞关系，创建新的点赞
            LikeAggregate likeAggregate = LikeAggregate.create(userId, targetId, type);
            
            // 如果在事务中，暂存操作
            if (tempOperation.get() != null) {
                tempOperation.get().setOperation(LikeOperationType.LIKE);
                tempOperation.get().setLikeAggregate(likeAggregate);
                log.debug("在事务中暂存新点赞操作: userId={}, targetId={}, type={}", userId, targetId, type);
                return;
            }
            
            likeAggregateRepository.save(likeAggregate);
        }
        
        // 4. 更新统计信息
        updateLikeStatistics(targetId, type, 1L);
    }
    
    /**
     * 取消点赞操作
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void cancelLike(Long userId, Long targetId, LikeType type) {
        // 1. 查找已点赞的记录
        Optional<LikeAggregate> existingLikeOpt = likeAggregateRepository.findByUserAndTarget(userId, type, targetId);
        if (!existingLikeOpt.isPresent()) {
            log.warn("[取消点赞] 找不到点赞记录 - userId: {}, targetId: {}", userId, targetId);
            throw new BusinessException("您尚未点赞，无法取消");
        }
        
        LikeAggregate likeAggregate = existingLikeOpt.get();
        
        // 如果已经是取消点赞状态，抛出异常
        if (!likeAggregate.isLiked()) {
            log.warn("[取消点赞] 用户未点赞，无法取消 - userId: {}, targetId: {}", userId, targetId);
            throw new BusinessException("您尚未点赞，无法取消");
        }
        
        // 2. 取消点赞
        likeAggregate.unlike();
        
        // 如果在事务中，暂存操作
        if (tempOperation.get() != null) {
            tempOperation.get().setOperation(LikeOperationType.UNLIKE);
            tempOperation.get().setLikeAggregate(likeAggregate);
            log.debug("在事务中暂存取消点赞操作: userId={}, targetId={}, type={}", userId, targetId, type);
            return;
        }
        
        likeAggregateRepository.update(likeAggregate);
        
        // 3. 更新统计信息
        updateLikeStatistics(targetId, type, -1L);
    }
    
    /**
     * 检查用户是否点赞某个目标
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 是否已点赞
     */
    public boolean checkLikeStatus(Long userId, Long targetId, LikeType type) {
        Optional<LikeAggregate> existingLikeOpt = likeAggregateRepository.findByUserAndTarget(userId, type, targetId);
        return existingLikeOpt.isPresent() && existingLikeOpt.get().isLiked();
    }
    
    /**
     * 获取目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    public Long getLikeCount(Long targetId, LikeType type) {
        return likeStatisticsService.getLikeCount(targetId, type);
    }
    
    /**
     * 检查并修复指定用户和目标的点赞数据一致性
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void checkAndRepairLikeConsistency(Long userId, Long targetId, LikeType type) {
        likeDataConsistencyService.checkAndRepairLikeConsistency(userId, targetId, type);
    }
    
    /**
     * 更新点赞统计信息
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @param delta 变化量
     */
    private void updateLikeStatistics(Long targetId, LikeType type, Long delta) {
        try {
            // 更新热点数据
            Long currentCount = likeStatisticsService.getLikeCount(targetId, type);
            likeHotDataService.updateHotData(targetId, type, currentCount + delta);
        } catch (Exception e) {
            log.error("更新点赞统计信息失败 - targetId: {}, type: {}, delta: {}", targetId, type, delta, e);
        }
    }
    
    @Override
    public void commit() throws Exception {
        LikeOperation operation = tempOperation.get();
        if (operation != null) {
            if (operation.getOperation() == LikeOperationType.LIKE) {
                likeAggregateRepository.save(operation.getLikeAggregate());
                log.info("提交点赞操作: userId={}, targetId={}, type={}", 
                        operation.getLikeAggregate().getUserId(), 
                        operation.getLikeAggregate().getTargetId(), 
                        operation.getLikeAggregate().getType());
            } else if (operation.getOperation() == LikeOperationType.UNLIKE) {
                likeAggregateRepository.update(operation.getLikeAggregate());
                log.info("提交取消点赞操作: userId={}, targetId={}, type={}", 
                        operation.getLikeAggregate().getUserId(), 
                        operation.getLikeAggregate().getTargetId(), 
                        operation.getLikeAggregate().getType());
            }
            tempOperation.remove();
        }
    }
    
    @Override
    public void rollback() throws Exception {
        LikeOperation operation = tempOperation.get();
        if (operation != null) {
            log.info("回滚点赞操作");
            tempOperation.remove();
        }
    }
    
    /**
     * 开始事务
     */
    public void beginTransaction() {
        tempOperation.set(new LikeOperation());
        log.debug("开始点赞事务");
    }
    
    /**
     * 点赞操作类型枚举
     */
    private enum LikeOperationType {
        LIKE, UNLIKE
    }
    
    /**
     * 点赞操作封装类
     */
    private static class LikeOperation {
        private LikeOperationType operation;
        private LikeAggregate likeAggregate;
        
        public LikeOperationType getOperation() {
            return operation;
        }
        
        public void setOperation(LikeOperationType operation) {
            this.operation = operation;
        }
        
        public LikeAggregate getLikeAggregate() {
            return likeAggregate;
        }
        
        public void setLikeAggregate(LikeAggregate likeAggregate) {
            this.likeAggregate = likeAggregate;
        }
    }
}