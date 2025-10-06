package cn.xu.application.service;

import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.model.aggregate.LikeAggregate;
import cn.xu.domain.like.repository.ILikeAggregateRepository;
import cn.xu.domain.like.service.LikeDataConsistencyService;
import cn.xu.infrastructure.cache.LikeCacheRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
            
            // 只有在确实执行了点赞操作时才增加Redis中的点赞数
            if (shouldIncrementCache) {
                // 增加Redis中的点赞数
                likeCacheRepository.incrementLikeCount(targetId, type, 1);
                
                // 添加用户点赞关系缓存
                likeCacheRepository.addUserLikeRelation(userId, targetId, type);
                
                log.info("[点赞应用服务] 更新缓存成功，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
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
                        likeCacheRepository.incrementLikeCount(targetId, type, -1);
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
                    likeCacheRepository.incrementLikeCount(targetId, type, -1);
                    log.info("[点赞应用服务] 清理不一致的缓存数据（无数据库记录），用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
                }
                return; // 没有记录，直接返回
            }
            
            // 无论数据库中是否存在点赞记录，都尝试更新缓存以确保一致性
            if (shouldDecrementCache) {
                // 减少Redis中的点赞数
                likeCacheRepository.incrementLikeCount(targetId, type, -1);
                
                // 移除用户点赞关系缓存
                likeCacheRepository.removeUserLikeRelation(userId, targetId, type);
                
                log.info("[点赞应用服务] 更新缓存成功，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
            }
            
            log.info("[点赞应用服务] 取消点赞操作成功完成");
        } catch (Exception e) {
            log.error("[点赞应用服务] 取消点赞操作失败，用户: {}, 目标: {}, 类型: {}", userId, targetId, type, e);
            throw e;
        }
    }
    
    /**
     * 检查用户是否点赞某个目标
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
     * 检查并修复指定用户和目标的点赞数据一致性
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param type 点赞类型
     */
    public void checkAndRepairLikeConsistency(Long userId, Long targetId, LikeType type) {
        likeDataConsistencyService.checkAndRepairLikeConsistency(userId, targetId, type);
    }
}