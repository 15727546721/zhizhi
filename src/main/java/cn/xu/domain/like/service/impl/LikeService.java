package cn.xu.domain.like.service.impl;

import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.like.event.LikeEventPublisher;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.domain.like.service.LikeDomainService;
import cn.xu.domain.like.service.LikeTargetRepositoryManager;
import cn.xu.domain.post.repository.IPostRepository;
import cn.xu.infrastructure.cache.LikeCacheRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 点赞应用服务
 * 负责处理应用层逻辑，如参数校验、事务管理等
 */
@Slf4j
@Service
public class LikeService implements ILikeService {

    @Autowired
    private LikeDomainService likeDomainService;
    
    @Autowired
    private LikeEventPublisher likeEventPublisher;
    
    @Autowired
    private IPostRepository postRepository;
    
    @Autowired
    private LikeCacheRepository likeCacheRepository;
    
    @Autowired
    private LikeTargetRepositoryManager likeTargetRepositoryManager;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void like(Long userId, LikeType type, Long targetId) {
        checkParams(userId, type, targetId);
        
        try {
            // 先检查当前状态，避免重复操作
            boolean currentStatus = likeDomainService.checkLikeStatus(userId, targetId, type);
            if (currentStatus) {
                log.warn("用户已点赞，无需重复操作 - userId: {}, targetId: {}, type: {}", userId, targetId, type);
                throw new BusinessException("您已点赞，请勿重复操作");
            }
            
            likeDomainService.doLike(userId, targetId, type);
            // 发布点赞事件
            likeEventPublisher.publish(userId, targetId, type, true);
            
            // 使用策略模式更新对应表的点赞数（根据类型自动选择对应的仓储）
            try {
                likeTargetRepositoryManager.updateLikeCount(type, targetId, 1L);
                log.info("[点赞服务] 更新数据库点赞数成功，目标: {}, 类型: {}, 增量: +1", targetId, type);
            } catch (Exception e) {
                log.error("[点赞服务] 更新数据库点赞数失败，目标: {}, 类型: {}, 增量: +1", targetId, type, e);
                // 如果该类型不需要更新数据库中的点赞数，可以忽略异常
                if (!type.isPost() && !type.isEssay() && !type.isComment()) {
                    log.warn("[点赞服务] 点赞类型 {} 无需更新数据库点赞数，仅更新缓存", type);
                } else {
                    throw e;
                }
            }
            
            // 使用事务同步管理器，在事务提交后执行Redis更新
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 1. 删除点赞数缓存（Cache Aside）
                        likeCacheRepository.deleteLikeCount(targetId, type);
                        
                        // 2. 添加用户点赞关系缓存
                        likeCacheRepository.addUserLikeRelation(userId, targetId, type);
                        
                        log.info("[点赞服务] 事务提交后更新缓存成功，用户: {}, 目标: {}, 类型: {}", userId, targetId, type);
                    } catch (Exception e) {
                        log.error("[点赞服务] 事务提交后更新缓存失败，用户: {}, 目标: {}, 类型: {}", userId, targetId, type, e);
                    }
                }
            });
        } catch (BusinessException e) {
            log.error("点赞失败: userId={}, targetId={}, type={}", userId, targetId, type, e);
            throw e;
        } catch (Exception e) {
            log.error("点赞失败: userId={}, targetId={}, type={}", userId, targetId, type, e);
            throw new BusinessException("点赞失败，请稍后再试！");
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long userId, LikeType type, Long targetId) {
        checkParams(userId, type, targetId);
        
        try {
            // 先检查当前状态，避免重复操作
            boolean currentStatus = likeDomainService.checkLikeStatus(userId, targetId, type);
            if (!currentStatus) {
                log.warn("用户未点赞，无法取消 - userId: {}, targetId: {}, type: {}", userId, targetId, type);
                throw new BusinessException("您尚未点赞，无法取消");
            }
            
            likeDomainService.cancelLike(userId, targetId, type);
            // 发布取消点赞事件
            likeEventPublisher.publish(userId, targetId, type, false);
            
            // 使用策略模式更新对应表的点赞数（根据类型自动选择对应的仓储）
            try {
                likeTargetRepositoryManager.updateLikeCount(type, targetId, -1L);
                log.info("[点赞服务] 更新数据库点赞数成功，目标: {}, 类型: {}, 增量: -1", targetId, type);
            } catch (Exception e) {
                log.error("[点赞服务] 更新数据库点赞数失败，目标: {}, 类型: {}, 增量: -1", targetId, type, e);
                if (!type.isPost() && !type.isEssay() && !type.isComment()) {
                    log.warn("[点赞服务] 点赞类型 {} 无需更新数据库点赞数，仅更新缓存", type);
                } else {
                    throw e;
                }
            }
            
            // 立即移除用户点赞关系缓存（防止False Positive）
            likeCacheRepository.removeUserLikeRelation(userId, targetId, type);
            
            // 使用事务同步管理器，在事务提交后执行点赞数缓存清理
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 删除点赞数缓存
                        likeCacheRepository.deleteLikeCount(targetId, type);
                        log.info("[点赞服务] 事务提交后清理计数缓存成功，目标: {}, 类型: {}", targetId, type);
                    } catch (Exception e) {
                        log.error("[点赞服务] 事务提交后清理计数缓存失败，目标: {}, 类型: {}", targetId, type, e);
                    }
                }
            });
        } catch (BusinessException e) {
            log.error("取消点赞失败: userId={}, targetId={}, type={}", userId, targetId, type, e);
            throw e;
        } catch (Exception e) {
            log.error("取消点赞失败: userId={}, targetId={}, type={}", userId, targetId, type, e);
            throw new BusinessException("取消点赞失败，请稍后再试！");
        }
    }

    @Override
    public boolean checkStatus(Long userId, LikeType type, Long targetId) {
        if (userId == null || type == null || targetId == null) {
            // 未登录用户默认未点赞
            return false;
        }
        
        try {
            // 优先从缓存检查用户点赞关系
            boolean cachedResult = likeCacheRepository.checkUserLikeRelation(userId, targetId, type);
            if (cachedResult) {
                log.debug("[点赞服务] 从缓存中获取到点赞状态：已点赞");
                return true;
            }
            
            // 缓存未命中，从数据库检查
            boolean dbResult = likeDomainService.checkLikeStatus(userId, targetId, type);
            
            // 如果数据库中有点赞记录，同步到缓存
            if (dbResult) {
                likeCacheRepository.addUserLikeRelation(userId, targetId, type);
                log.debug("[点赞服务] 从数据库中获取到点赞状态：已点赞，并同步到缓存");
            } else {
                log.debug("[点赞服务] 从数据库中获取到点赞状态：未点赞");
            }
            
            return dbResult;
        } catch (Exception e) {
            log.error("[点赞服务] 检查点赞状态失败，用户: {}, 目标: {}, 类型: {}", userId, targetId, type, e);
            // 出错时降级到数据库查询
            return likeDomainService.checkLikeStatus(userId, targetId, type);
        }
    }
    
    @Override
    public long getLikeCount(Long targetId, LikeType type) {
        try {
            // 优先从缓存获取点赞数
            Long cachedCount = likeCacheRepository.getLikeCount(targetId, type);
            if (cachedCount != null) {
                log.debug("[点赞服务] 从缓存中获取到点赞数: {}", cachedCount);
                return cachedCount;
            }
            
            // 缓存未命中，从数据库统计
            long dbCount = likeDomainService.getLikeCount(targetId, type);
            
            // 同步到缓存
            likeCacheRepository.setLikeCount(targetId, type, dbCount);
            log.debug("[点赞服务] 从数据库中获取到点赞数: {}，并同步到缓存", dbCount);
            
            return dbCount;
        } catch (Exception e) {
            log.error("[点赞服务] 获取点赞数失败，目标: {}, 类型: {}", targetId, type, e);
            // 出错时降级到数据库查询
            return likeDomainService.getLikeCount(targetId, type);
        }
    }
    
    @Override
    public void checkAndRepairLikeConsistency(Long userId, Long targetId, LikeType type) {
        likeDomainService.checkAndRepairLikeConsistency(userId, targetId, type);
    }

    /**
     * 校验入参
     */
    private void checkParams(Long userId, LikeType type, Long targetId) {
        if (userId == null || type == null || targetId == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), ResponseCode.NULL_PARAMETER.getMessage());
        }
    }
    
}