package cn.xu.service.like;

import cn.xu.cache.LikeCacheRepository;
import cn.xu.event.like.LikeEvent;
import cn.xu.model.entity.Like;
import cn.xu.model.entity.Like.LikeType;
import cn.xu.repository.mapper.CommentMapper;
import cn.xu.repository.mapper.LikeMapper;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 点赞服务
 *
 * @author xu
 */
@Slf4j
@Service("likeDomainService")
@RequiredArgsConstructor
public class LikeService {
    
    private final LikeMapper likeMapper;
    private final LikeCacheRepository likeCacheRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final CommentMapper commentMapper;
    private final PostMapper postMapper;
    
    // ==================== 核心业务方法 ====================
    
    /**
     * 点赞操作
     * 
     * @param userId 用户ID
     * @param type 点赞类型：1-帖子，2-评论
     * @param targetId 目标ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void like(Long userId, Integer type, Long targetId) {
        // 1. 参数验证
        validateParams(userId, type, targetId);
        
        try {
            // 2. 使用原子操作保存或更新点赞记录（解决并发竞态条件）
            // 使用 INSERT ON DUPLICATE KEY UPDATE，无需先查询再判断
            Like likeRecord = Like.createLike(userId, targetId, type);
            likeRecord.like(); // 设置状态为已点赞
            likeMapper.saveOrUpdate(likeRecord);
            log.info("[点赞服务] 原子保存点赞记录成功 - userId: {}, targetId: {}, type: {}", userId, targetId, type);
            
            // 3. 更新目标表的点赞数（Post或Comment）
            updateTargetLikeCount(type, targetId, 1L);
            
            // 4. 在事务提交后更新Redis缓存
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 4.1 删除点赞数缓存（Cache Invalidation策略）
                        likeCacheRepository.deleteLikeCount(targetId, LikeType.fromCode(type));
                        
                        // 4.2 添加用户点赞关系缓存
                        likeCacheRepository.addUserLikeRelation(userId, targetId, LikeType.fromCode(type));
                        
                        log.info("[点赞服务] 事务提交后更新缓存成功 - userId: {}, targetId: {}", userId, targetId);
                    } catch (Exception e) {
                        log.error("[点赞服务] 事务提交后更新缓存失败 - userId: {}, targetId: {}", userId, targetId, e);
                    }
                }
            });
            
            // 5. 发布点赞事件
            publishLikeEvent(userId, targetId, type, true);
            
        } catch (BusinessException e) {
            log.error("[点赞服务] 点赞失败 - userId: {}, targetId: {}, type: {}, error: {}", 
                userId, targetId, type, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[点赞服务] 点赞失败 - userId: {}, targetId: {}, type: {}", userId, targetId, type, e);
            throw new BusinessException("点赞失败，请稍后再试");
        }
    }
    
    /**
     * 取消点赞操作
     * 
     * @param userId 用户ID
     * @param type 点赞类型
     * @param targetId 目标ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void unlike(Long userId, Integer type, Long targetId) {
        // 1. 参数验证
        validateParams(userId, type, targetId);
        
        try {
            // 2. 查询现有记录
            Like existingLike = likeMapper.findByUserIdAndTypeAndTargetId(userId, type, targetId);
            
            if (existingLike == null) {
                log.warn("[点赞服务] 取消点赞失败，记录不存在 - userId: {}, targetId: {}", userId, targetId);
                throw new BusinessException("您尚未点赞，无法取消");
            }
            
            // 3. 执行取消点赞操作
            existingLike.unlike(); // PO业务方法
            likeMapper.update(existingLike);
            log.info("[点赞服务] 取消点赞成功 - userId: {}, targetId: {}, type: {}", userId, targetId, type);
            
            // 4. 更新目标表的点赞数
            updateTargetLikeCount(type, targetId, -1L);
            
            // 5. 立即移除用户点赞关系缓存（防止False Positive）
            likeCacheRepository.removeUserLikeRelation(userId, targetId, LikeType.fromCode(type));
            
            // 6. 在事务提交后删除点赞数缓存
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 删除点赞数缓存
                        likeCacheRepository.deleteLikeCount(targetId, LikeType.fromCode(type));
                        log.info("[点赞服务] 事务提交后清理计数缓存成功 - targetId: {}", targetId);
                    } catch (Exception e) {
                        log.error("[点赞服务] 事务提交后清理计数缓存失败 - targetId: {}", targetId, e);
                    }
                }
            });
            
            // 7. 发布取消点赞事件
            publishLikeEvent(userId, targetId, type, false);
            
        } catch (BusinessException e) {
            log.error("[点赞服务] 取消点赞失败 - userId: {}, targetId: {}, error: {}", 
                userId, targetId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[点赞服务] 取消点赞失败 - userId: {}, targetId: {}", userId, targetId, e);
            throw new BusinessException("取消点赞失败，请稍后再试");
        }
    }
    
    /**
     * 检查点赞状态（带缓存自愈能力）
     * 
     * @param userId 用户ID
     * @param type 点赞类型
     * @param targetId 目标ID
     * @return true-已点赞，false-未点赞
     */
    public boolean checkStatus(Long userId, Integer type, Long targetId) {
        if (userId == null || type == null || targetId == null) {
            return false;
        }
        
        try {
            // 1. 优先从缓存检查
            boolean cachedResult = likeCacheRepository.checkUserLikeRelation(
                userId, targetId, LikeType.fromCode(type));
            
            if (cachedResult) {
                log.debug("[点赞服务] 从缓存命中点赞状态：已点赞");
                return true;
            }
            
            // 2. 缓存未命中，回源数据库
            Integer status = likeMapper.checkStatus(userId, type, targetId);
            boolean isLiked = (status != null && status == Like.STATUS_LIKED);
            
            // 3. 自愈：如果数据库中有点赞记录，同步到缓存
            if (isLiked) {
                likeCacheRepository.addUserLikeRelation(userId, targetId, LikeType.fromCode(type));
                log.debug("[点赞服务] 从数据库获取点赞状态并自愈缓存：已点赞");
            } else {
                log.debug("[点赞服务] 从数据库获取点赞状态：未点赞");
            }
            
            return isLiked;
            
        } catch (Exception e) {
            log.error("[点赞服务] 检查点赞状态失败 - userId: {}, targetId: {}", userId, targetId, e);
            // 降级：直接查数据库
            Integer status = likeMapper.checkStatus(userId, type, targetId);
            return (status != null && status == Like.STATUS_LIKED);
        }
    }
    
    /**
     * 获取点赞数（带缓存自愈能力）
     * 
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    public long getLikeCount(Long targetId, Integer type) {
        if (targetId == null || type == null) {
            return 0L;
        }
        
        try {
            // 1. 优先从缓存获取
            Long cachedCount = likeCacheRepository.getLikeCount(targetId, LikeType.fromCode(type));
            if (cachedCount != null) {
                log.debug("[点赞服务] 从缓存获取点赞数：{}", cachedCount);
                return cachedCount;
            }
            
            // 2. 缓存未命中，统计数据库
            long dbCount = likeMapper.countByTargetIdAndType(targetId, type);
            
            // 3. 自愈：同步到缓存
            likeCacheRepository.setLikeCount(targetId, LikeType.fromCode(type), dbCount);
            log.debug("[点赞服务] 从数据库统计点赞数并自愈缓存：{}", dbCount);
            
            return dbCount;
            
        } catch (Exception e) {
            log.error("[点赞服务] 获取点赞数失败 - targetId: {}, type: {}", targetId, type, e);
            // 降级：直接查数据库
            return likeMapper.countByTargetIdAndType(targetId, type);
        }
    }
    
    /**
     * 批量检查点赞状态
     * 
     * @param userId 用户ID
     * @param type 点赞类型
     * @param targetIds 目标ID列表
     * @return 已点赞的目标ID列表
     */
    public List<Long> batchCheckStatus(Long userId, Integer type, List<Long> targetIds) {
        if (userId == null || type == null || targetIds == null || targetIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            List<Like> likedList = likeMapper.findByUserIdAndTargetIds(userId, type, targetIds);
            return likedList.stream()
                    .filter(Like::isLiked)
                    .map(Like::getTargetId)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("[点赞服务] 批量检查点赞状态失败 - userId: {}, targetIds: {}", userId, targetIds, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 分页查询用户的点赞列表
     * 
     * @param userId 用户ID
     * @param pageNo 页码（从1开始）
     * @param pageSize 每页大小
     * @return 点赞列表
     */
    public List<Like> getUserLikes(Long userId, Integer pageNo, Integer pageSize) {
        if (userId == null) {
            log.warn("[点赞服务] 查询用户点赞列表失败 - 用户ID为空");
            return Collections.emptyList();
        }
        
        if (pageNo == null || pageNo < 1) {
            pageNo = 1;
        }
        if (pageSize == null || pageSize < 1) {
            pageSize = 10;
        }
        if (pageSize > 100) {
            pageSize = 100; // 限制最大每页100条
        }
        
        try {
            int offset = (pageNo - 1) * pageSize;
            List<Like> likes = likeMapper.findByUserId(userId, offset, pageSize);
            log.debug("[点赞服务] 查询用户点赞列表成功 - userId: {}, pageNo: {}, pageSize: {}, size: {}", 
                    userId, pageNo, pageSize, likes.size());
            return likes;
        } catch (Exception e) {
            log.error("[点赞服务] 查询用户点赞列表失败 - userId: {}, pageNo: {}, pageSize: {}", 
                    userId, pageNo, pageSize, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 统计用户的点赞总数
     * 
     * @param userId 用户ID
     * @return 点赞总数
     */
    public long countUserLikes(Long userId) {
        if (userId == null) {
            return 0L;
        }
        
        try {
            return likeMapper.countByUserId(userId);
        } catch (Exception e) {
            log.error("[点赞服务] 统计用户点赞总数失败 - userId: {}", userId, e);
            return 0L;
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 验证参数
     */
    private void validateParams(Long userId, Integer type, Long targetId) {
        if (userId == null) {
            throw new BusinessException("用户ID不能为空");
        }
        if (type == null) {
            throw new BusinessException("点赞类型不能为空");
        }
        if (targetId == null) {
            throw new BusinessException("目标ID不能为空");
        }
        // 验证类型是否有效
        try {
            Like.LikeType.fromCode(type);
        } catch (Exception e) {
            throw new BusinessException("无效的点赞类型: " + type);
        }
    }
    
    /**
     * 更新目标表的点赞数
     * 
     * 注意：此方法在事务内执行，更新失败会抛出异常触发事务回滚，
     * 确保Like表和目标表的计数保持一致
     */
    private void updateTargetLikeCount(Integer type, Long targetId, Long delta) {
        LikeType likeType = LikeType.fromCode(type);
        switch (likeType) {
            case POST:
                // 更新帖子点赞数
                postMapper.updateLikeCount(targetId, delta);
                log.info("[点赞服务] 更新帖子点赞数成功 - postId: {}, delta: {}", targetId, delta);
                break;
            case ESSAY:
                // 随笔点赞数通过like表统计，不需要在essay表冗余保存
                log.debug("[点赞服务] 随笔点赞数通过统计获取 - essayId: {}", targetId);
                break;
            case COMMENT:
                // 更新评论点赞数
                commentMapper.updateLikeCount(targetId, delta.intValue());
                log.info("[点赞服务] 更新评论点赞数成功 - commentId: {}, delta: {}", targetId, delta);
                break;
            default:
                log.warn("[点赞服务] 未知的点赞类型: {}", type);
                break;
        }
        // 不捕获异常：让异常向上传播，触发事务回滚，保证数据一致性
    }
    
    /**
     * 发布点赞事件
     */
    private void publishLikeEvent(Long userId, Long targetId, Integer type, boolean isLike) {
        try {
            LikeEvent event = LikeEvent.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .type(LikeType.fromCode(type))
                    .status(isLike)
                    .createTime(LocalDateTime.now())
                    .build();
            
            eventPublisher.publishEvent(event);
            log.debug("[点赞服务] 发布点赞事件成功 - userId: {}, targetId: {}, isLike: {}", 
                userId, targetId, isLike);
        } catch (Exception e) {
            log.error("[点赞服务] 发布点赞事件失败 - userId: {}, targetId: {}", userId, targetId, e);
            // 不抛出异常，避免影响主流程
        }
    }
}
