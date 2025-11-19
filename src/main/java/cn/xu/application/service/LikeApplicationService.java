package cn.xu.application.service;

import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.model.aggregate.LikeAggregate;
import cn.xu.domain.like.repository.ILikeAggregateRepository;
import cn.xu.domain.like.service.LikeDataConsistencyService;
import cn.xu.domain.like.service.LikeTargetRepositoryManager;
import cn.xu.infrastructure.cache.LikeCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 点赞应用服务
 * 作为领域层和应用层的适配器
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LikeApplicationService {
    
    private final ILikeAggregateRepository likeAggregateRepository;
    private final LikeCacheRepository likeCacheRepository;
    private final LikeDataConsistencyService likeDataConsistencyService;
    private final LikeTargetRepositoryManager likeTargetRepositoryManager;
    
    /**
     * 点赞操作
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void doLike(Long userId, Long targetId, LikeType type) {
        try {
            log.info("[点赞应用服务] 开始点赞操作，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
            
            // 查找现有的点赞关系
            Optional<LikeAggregate> existingLikeOpt = likeAggregateRepository.findByUserAndTarget(userId, type, targetId);
            
            boolean shouldIncrementCache = false;
            
            if (existingLikeOpt.isPresent()) {
                // 如果已存在点赞关系，更新状态
                LikeAggregate likeAggregate = existingLikeOpt.get();
                // 只有当当前状态为未点赞时，才执行点赞操作
                if (!likeAggregate.isLiked()) {
                    likeAggregate.like();
                    likeAggregateRepository.update(likeAggregate);
                    shouldIncrementCache = true;
                    log.info("[点赞应用服务] 更新现有点赞记录，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
                } else {
                    log.warn("[点赞应用服务] 用户已点赞，无需重复操作，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
                    return; // 已经点赞，直接返回
                }
            } else {
                // 如果不存在点赞关系，创建新的点赞
                LikeAggregate likeAggregate = LikeAggregate.create(userId, targetId, type);
                likeAggregateRepository.save(likeAggregate);
                shouldIncrementCache = true;
                log.info("[点赞应用服务] 创建新点赞记录，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
            }
            
            // 只有在确实执行了点赞操作时才更新缓存和数据库
            if (shouldIncrementCache) {
                // 先更新数据库中的点赞数（确保数据一致性）
                try {
                    likeTargetRepositoryManager.updateLikeCount(type, targetId, 1L);
                    log.info("[点赞应用服务] 更新数据库点赞数成功，目标: {}, 类型: {}, 增量: +1", targetId, type);
                } catch (Exception e) {
                    log.error("[点赞应用服务] 更新数据库点赞数失败，目标: {}, 类型: {}, 增量: +1", targetId, type, e);
                    // 如果该类型不支持更新数据库点赞数，记录警告但不中断流程
                    if (!type.isPost() && !type.isEssay() && !type.isComment()) {
                        log.warn("[点赞应用服务] 点赞类型 {} 不支持更新数据库点赞数，仅更新缓存", type);
                    } else {
                        throw e; // 数据库更新失败，抛出异常，事务回滚
                    }
                }
                
                // 使用事务同步管理器，在事务提交后执行Redis更新，确保DB和Redis的一致性
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            // 1. 删除点赞数缓存（Cache Aside模式：更新DB后删除缓存）
                            // 下次读取时会从DB加载最新值，避免并发导致的脏数据
                            likeCacheRepository.deleteLikeCount(targetId, type);
                            
                            // 2. 添加用户点赞关系缓存
                            // 在事务提交后执行，避免"Redis成功但DB回滚"导致的数据不一致
                            likeCacheRepository.addUserLikeRelation(userId, targetId, type);
                            
                            log.info("[点赞应用服务] 事务提交后更新缓存成功，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
                        } catch (Exception e) {
                            log.error("[点赞应用服务] 事务提交后更新缓存失败，用户: {}, 目标: {}, 类型: {}", userId, targetId, type, e);
                            // 这里虽然缓存更新失败，但DB已提交，数据是持久化的
                            // 后续可以通过定时任务或下一次读取来修复缓存
                        }
                    }
                });
            }
            
            log.info("[点赞应用服务] 点赞操作成功完成");
        } catch (Exception e) {
            log.error("[点赞应用服务] 点赞操作失败，用户: {}, 目标: {}, 类型: {}", userId, targetId, type, e);
            throw e;
        }
    }
    
    /**
     * 取消点赞操作
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelLike(Long userId, Long targetId, LikeType type) {
        try {
            log.info("[点赞应用服务] 开始取消点赞操作，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
            
            // 查找现有的点赞关系
            Optional<LikeAggregate> existingLikeOpt = likeAggregateRepository.findByUserAndTarget(userId, type, targetId);
            
            boolean shouldDecrementCache = false;
            
            if (existingLikeOpt.isPresent()) {
                // 如果存在点赞关系，检查当前状态
                LikeAggregate likeAggregate = existingLikeOpt.get();
                // 只有当当前状态为已点赞时，才执行取消点赞操作
                if (likeAggregate.isLiked()) {
                    likeAggregate.unlike();
                    likeAggregateRepository.update(likeAggregate);
                    shouldDecrementCache = true;
                    log.info("[点赞应用服务] 更新现有点赞记录为取消状态，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
                } else {
                    log.warn("[点赞应用服务] 用户未点赞，无需取消操作，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
                    // 即使数据库状态为未点赞，也要确保缓存一致性
                    boolean cachedLiked = likeCacheRepository.checkUserLikeRelation(userId, targetId, type);
                    if (cachedLiked) {
                        likeCacheRepository.removeUserLikeRelation(userId, targetId, type);
                        likeCacheRepository.deleteLikeCount(targetId, type); // 删除缓存
                        log.info("[点赞应用服务] 清理不一致的缓存数据，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
                    }
                    return; // 已经取消点赞，直接返回
                }
            } else {
                log.warn("[点赞应用服务] 未找到点赞记录，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
                // 如果数据库中没有记录，但缓存中标记为已点赞，则清理缓存
                boolean cachedLiked = likeCacheRepository.checkUserLikeRelation(userId, targetId, type);
                if (cachedLiked) {
                    likeCacheRepository.removeUserLikeRelation(userId, targetId, type);
                    likeCacheRepository.deleteLikeCount(targetId, type); // 删除缓存
                    log.info("[点赞应用服务] 清理不一致的缓存数据（无数据库记录），用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
                }
                return; // 没有记录，直接返回
            }
            
            // 只有在确实执行了取消点赞操作时才更新缓存和数据库
            if (shouldDecrementCache) {
                // 先更新数据库中的点赞数（确保数据一致性）
                try {
                    likeTargetRepositoryManager.updateLikeCount(type, targetId, -1L);
                    log.info("[点赞应用服务] 更新数据库点赞数成功，目标: {}, 类型: {}, 增量: -1", targetId, type);
                } catch (Exception e) {
                    log.error("[点赞应用服务] 更新数据库点赞数失败，目标: {}, 类型: {}, 增量: -1", targetId, type, e);
                    // 如果该类型不支持更新数据库点赞数，记录警告但不中断流程
                    if (!type.isPost() && !type.isEssay() && !type.isComment()) {
                        log.warn("[点赞应用服务] 点赞类型 {} 不支持更新数据库点赞数，仅更新缓存", type);
                    } else {
                        throw e; // 数据库更新失败，抛出异常，事务回滚
                    }
                }
                
                // 立即移除用户点赞关系缓存（在事务提交前）
                likeCacheRepository.removeUserLikeRelation(userId, targetId, type);
                
                // 使用事务同步管理器，在事务提交后执行点赞数缓存清理
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        try {
                            // 删除点赞数缓存（Cache Aside模式）
                            likeCacheRepository.deleteLikeCount(targetId, type);
                            
                            log.info("[点赞应用服务] 事务提交后清理计数缓存成功，目标: {}, 类型: {}", targetId, type);
                        } catch (Exception e) {
                            log.error("[点赞应用服务] 事务提交后清理计数缓存失败，目标: {}, 类型: {}", targetId, type, e);
                        }
                    }
                });
            }
            
            log.info("[点赞应用服务] 取消点赞操作成功完成");
        } catch (Exception e) {
            log.error("[点赞应用服务] 取消点赞操作失败，用户: {}, 目标: {}, 类型: {}", userId, targetId, type, e);
            throw e;
        }
    }
    
    /**
     * 检查并修复指定用户和目标的点赞数据一致性
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void checkLikeDataConsistency(Long userId, Long targetId, LikeType type) {
        likeDataConsistencyService.checkAndRepairLikeConsistency(userId, targetId, type);
    }
    
    /**
     * 检查点赞状态
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 是否点赞
     */
    public boolean checkLikeStatus(Long userId, Long targetId, LikeType type) {
        try {
            log.info("[点赞应用服务] 开始检查点赞状态，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
            
            // 优先从缓存检查用户点赞关系
            boolean cachedResult = likeCacheRepository.checkUserLikeRelation(userId, targetId, type);
            if (cachedResult) {
                log.debug("[点赞应用服务] 从缓存中获取到点赞状态：已点赞");
                return true;
            }
            
            // 缓存未命中，从数据库检查
            Optional<LikeAggregate> existingLikeOpt = likeAggregateRepository.findByUserAndTarget(userId, type, targetId);
            boolean dbResult = existingLikeOpt.isPresent() && existingLikeOpt.get().isLiked();
            
            // 如果数据库中有点赞记录，同步到缓存
            if (dbResult) {
                likeCacheRepository.addUserLikeRelation(userId, targetId, type);
                log.debug("[点赞应用服务] 从数据库中获取到点赞状态：已点赞，并同步到缓存");
            } else {
                log.debug("[点赞应用服务] 从数据库中获取到点赞状态：未点赞");
            }
            
            return dbResult;
        } catch (Exception e) {
            log.error("[点赞应用服务] 检查点赞状态失败，用户: {}, 目标: {}, 类型: {}", userId, targetId, type, e);
            return false;
        }
    }
    
    /**
     * 获取目标的点赞数
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    public long getLikeCount(Long targetId, LikeType type) {
        try {
            log.info("[点赞应用服务] 开始获取点赞数，目标: {}, 类型: {}", targetId, type);
            
            // 从缓存获取点赞数
            Long cachedCount = likeCacheRepository.getLikeCount(targetId, type);
            if (cachedCount != null) {
                log.debug("[点赞应用服务] 从缓存中获取到点赞数: {}", cachedCount);
                return cachedCount;
            }
            
            // 缓存未命中，从数据库统计
            long dbCount = likeAggregateRepository.countByTarget(targetId, type);
            
            // 同步到缓存
            likeCacheRepository.setLikeCount(targetId, type, dbCount);
            log.debug("[点赞应用服务] 从数据库中获取到点赞数: {}，并同步到缓存", dbCount);
            
            return dbCount;
        } catch (Exception e) {
            log.error("[点赞应用服务] 获取点赞数失败，目标: {}, 类型: {}", targetId, type, e);
            return 0;
        }
    }
    
    /**
     * 批量查询用户对多个目标的点赞状态
     * 
     * @param userId 用户ID（可为null，未登录时返回全false）
     * @param targetIds 目标ID列表
     * @param type 点赞类型
     * @return 点赞状态Map，key为目标ID，value为是否点赞
     */
    public java.util.Map<Long, Boolean> batchCheckLikeStatus(Long userId, List<Long> targetIds, LikeType type) {
        java.util.Map<Long, Boolean> resultMap = new java.util.HashMap<>();
        
        if (targetIds == null || targetIds.isEmpty()) {
            // 目标列表为空，直接返回空Map
            return resultMap;
        }
        
        if (userId == null) {
            // 用户未登录，返回全false
            targetIds.forEach(targetId -> resultMap.put(targetId, false));
            return resultMap;
        }
        
        try {
            log.info("[点赞应用服务] 批量查询点赞状态，用户: {}, 目标数量: {}, 类型: {}", userId, targetIds.size(), type);
            
            // 1. 优先从缓存批量查询
            java.util.Map<Long, Boolean> cacheResult = likeCacheRepository.batchCheckUserLikeRelations(userId, targetIds, type);
            
            // 2. 找出缓存未命中的目标ID
            List<Long> missedTargetIds = new ArrayList<>();
            for (Long targetId : targetIds) {
                if (!cacheResult.containsKey(targetId)) {
                    missedTargetIds.add(targetId);
                }
            }
            
            // 3. 如果有缓存未命中的，从数据库批量查询
            if (!missedTargetIds.isEmpty()) {
                log.debug("[点赞应用服务] 缓存未命中数量: {}, 从数据库查询", missedTargetIds.size());
                java.util.Map<Long, Boolean> dbResult = likeAggregateRepository.batchCheckLikeStatus(userId, missedTargetIds, type);
                
                // 4. 将数据库查询结果同步到缓存，并合并到最终结果
                for (Long targetId : missedTargetIds) {
                    Boolean isLiked = dbResult.getOrDefault(targetId, false);
                    resultMap.put(targetId, isLiked);
                    
                    // 同步到缓存
                    if (isLiked) {
                        likeCacheRepository.addUserLikeRelation(userId, targetId, type);
                    }
                }
            }
            
            // 5. 合并缓存结果
            resultMap.putAll(cacheResult);
            
            log.debug("[点赞应用服务] 批量查询点赞状态完成，结果数量: {}", resultMap.size());
            return resultMap;
        } catch (Exception e) {
            log.error("[点赞应用服务] 批量查询点赞状态失败，用户: {}, 目标数量: {}, 类型: {}", userId, targetIds.size(), type, e);
            // 出错时返回全false
            targetIds.forEach(targetId -> resultMap.put(targetId, false));
            return resultMap;
        }
    }
}