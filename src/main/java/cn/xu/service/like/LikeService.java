package cn.xu.service.like;

import cn.xu.cache.LikeCacheRepository;
import cn.xu.event.events.LikeEvent;
import cn.xu.model.entity.Like;
import cn.xu.model.entity.Like.LikeType;
import cn.xu.repository.mapper.CommentMapper;
import cn.xu.repository.mapper.LikeMapper;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.repository.mapper.UserMapper;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 点赞服务
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
    private final UserMapper userMapper;

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
            // 2. 先检查是否已点赞（避免重复增加计数）
            Like existingLike = likeMapper.findByUserIdAndTypeAndTargetId(userId, type, targetId);

            if (existingLike != null && existingLike.isLiked()) {
                // 已点赞，幂等返回（不报错，不重复增加计数）
                log.info("[点赞服务] 用户已点赞，幂等返回 - userId: {}, targetId: {}, type: {}", userId, targetId, type);
                return;
            }

            // 3. 保存或更新点赞记录
            if (existingLike == null) {
                // 新增记录
                Like likeRecord = Like.createLike(userId, targetId, type);
                likeMapper.save(likeRecord);
            } else {
                // 更新状态（从取消变为点赞）
                existingLike.like();
                likeMapper.update(existingLike);
            }
            log.info("[点赞服务] 点赞记录保存成功 - userId: {}, targetId: {}, type: {}", userId, targetId, type);

            // 4. 更新目标表的点赞数（Post或Comment）
            updateTargetLikeCount(type, targetId, 1L);

            // 5. 在事务提交后更新Redis缓存（缓存失效策略）
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 5.1 删除点赞数缓存（下次查询时从 DB 重新加载）
                        likeCacheRepository.deleteLikeCount(targetId, LikeType.fromCode(type));

                        // 5.2 更新用户点赞关系缓存
                        likeCacheRepository.addUserLikeRelation(userId, targetId, LikeType.fromCode(type));

                        log.info("[点赞服务] 点赞缓存更新成功 - userId: {}, targetId: {}", userId, targetId);
                    } catch (Exception e) {
                        log.error("[点赞服务] 点赞缓存更新失败 - userId: {}, targetId: {}", userId, targetId, e);
                    }
                }
            });

            // 6. 发布点赞事件
            publishLikeEvent(userId, targetId, type, true);

        } catch (BusinessException e) {
            log.error("[点赞服务] 点赞操作失败 - userId: {}, targetId: {}, type: {}, error: {}",
                    userId, targetId, type, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[点赞服务] 点赞操作失败 - userId: {}, targetId: {}, type: {}", userId, targetId, type, e);
            throw new BusinessException("点赞操作失败，请稍后再试");
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
            // 2. 查询是否有点赞记录
            Like existingLike = likeMapper.findByUserIdAndTypeAndTargetId(userId, type, targetId);

            // 如果没有点赞记录或已取消点赞
            if (existingLike == null || !existingLike.isLiked()) {
                log.info("[点赞服务] 用户未点赞或已取消点赞，直接返回 - userId: {}, targetId: {}", userId, targetId);
                return;
            }

            // 3. 执行取消点赞操作
            existingLike.unlike(); // PO层取消点赞
            likeMapper.update(existingLike);
            log.info("[点赞服务] 取消点赞成功 - userId: {}, targetId: {}, type: {}", userId, targetId, type);

            // 4. 更新目标的点赞数
            updateTargetLikeCount(type, targetId, -1L);

            // 5. 事务提交后清除Redis缓存（所有缓存操作都在事务提交后执行，避免回滚不一致）
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 5.1 删除目标的点赞数缓存
                        likeCacheRepository.deleteLikeCount(targetId, LikeType.fromCode(type));
                        // 5.2 移除用户点赞关系缓存
                        likeCacheRepository.removeUserLikeRelation(userId, targetId, LikeType.fromCode(type));
                        log.info("[点赞服务] 取消点赞缓存清除成功 - userId: {}, targetId: {}", userId, targetId);
                    } catch (Exception e) {
                        log.error("[点赞服务] 取消点赞缓存清除失败 - targetId: {}", targetId, e);
                    }
                }
            });

            // 7. 发布取消点赞事件
            publishLikeEvent(userId, targetId, type, false);

        } catch (BusinessException e) {
            log.error("[点赞服务] 取消点赞操作失败 - userId: {}, targetId: {}, error: {}",
                    userId, targetId, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[点赞服务] 取消点赞操作失败 - userId: {}, targetId: {}", userId, targetId, e);
            throw new BusinessException("取消点赞操作失败，请稍后再试");
        }
    }

    /**
     * 检查用户是否已点赞某目标
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
            // 1. 检查缓存中是否已有用户点赞记录
            boolean cachedResult = likeCacheRepository.checkUserLikeRelation(userId, targetId, LikeType.fromCode(type));

            if (cachedResult) {
                log.debug("[点赞服务] 缓存中找到用户点赞记录，返回 true - userId: {}, targetId: {}", userId, targetId);
                return true;
            }

            // 2. 查询数据库
            Integer status = likeMapper.checkStatus(userId, type, targetId);
            boolean isLiked = (status != null && status == Like.STATUS_LIKED);

            // 3. 更新缓存
            if (isLiked) {
                likeCacheRepository.addUserLikeRelation(userId, targetId, LikeType.fromCode(type));
                log.debug("[点赞服务] 用户点赞状态已同步到缓存 - userId: {}, targetId: {}", userId, targetId);
            } else {
                log.debug("[点赞服务] 用户未点赞，返回 false - userId: {}, targetId: {}", userId, targetId);
            }

            return isLiked;

        } catch (Exception e) {
            log.error("[点赞服务] 获取用户点赞状态失败 - userId: {}, targetId: {}", userId, targetId, e);
            return likeMapper.checkStatus(userId, type, targetId) == Like.STATUS_LIKED;
        }
    }

    /**
     * 获取目标的点赞数
     * <p>策略：缓存优先，未命中则查DB并回写缓存</p>
     *
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    public long getLikeCount(Long targetId, Integer type) {
        LikeType likeType = LikeType.fromCode(type);
        try {
            // 1. 尝试从缓存获取
            Long cachedCount = likeCacheRepository.getLikeCount(targetId, likeType);
            if (cachedCount != null && cachedCount > 0) {
                return cachedCount;
            }
            
            // 2. 缓存未命中，从目标表获取真实点赞数
            long dbCount = getTargetLikeCountFromDB(type, targetId);
            
            // 3. 回写缓存
            if (dbCount >= 0) {
                likeCacheRepository.setLikeCount(targetId, likeType, dbCount);
            }
            
            return dbCount;
        } catch (Exception e) {
            log.error("[点赞服务] 获取点赞数失败 - targetId: {}", targetId, e);
            // 降级：直接查DB
            return getTargetLikeCountFromDB(type, targetId);
        }
    }
    
    /**
     * 从目标表获取点赞数（统一数据来源）
     */
    private long getTargetLikeCountFromDB(Integer type, Long targetId) {
        try {
            switch (LikeType.fromCode(type)) {
                case POST:
                    // 从post表获取like_count字段
                    Long postCount = postMapper.getLikeCount(targetId);
                    return postCount != null ? postCount : 0L;
                case COMMENT:
                    // 从comment表获取like_count字段
                    Integer commentCount = commentMapper.getLikeCount(targetId);
                    return commentCount != null ? commentCount : 0L;
                default:
                    return 0L;
            }
        } catch (Exception e) {
            log.error("[点赞服务] 从目标表获取点赞数失败 - targetId: {}, type: {}", targetId, type, e);
            return 0L;
        }
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证参数
     */
    private void validateParams(Long userId, Integer type, Long targetId) {
        if (userId == null || type == null || targetId == null) {
            throw new BusinessException("请求参数无效");
        }
    }

    /**
     * 更新目标的点赞数和作者的获赞数
     */
    private void updateTargetLikeCount(Integer type, Long targetId, Long countChange) {
        try {
            Long authorId = null;
            switch (LikeType.fromCode(type)) {
                case POST:
                    postMapper.updateLikeCount(targetId, countChange);
                    // 获取帖子作者ID
                    authorId = postMapper.getAuthorId(targetId);
                    break;
                case COMMENT:
                    commentMapper.updateLikeCount(targetId, countChange.intValue());
                    // 获取评论作者ID
                    authorId = commentMapper.getAuthorId(targetId);
                    break;
                default:
                    break;
            }
            
            // 更新作者的获赞数
            if (authorId != null) {
                if (countChange > 0) {
                    userMapper.increaseLikeCount(authorId);
                    log.debug("[点赞服务] 增加作者获赞数 - authorId: {}", authorId);
                } else {
                    userMapper.decreaseLikeCount(authorId);
                    log.debug("[点赞服务] 减少作者获赞数 - authorId: {}", authorId);
                }
            }
            
            log.debug("[点赞服务] 更新目标的点赞数成功 - targetId: {}, countChange: {}", targetId, countChange);
        } catch (Exception e) {
            log.error("[点赞服务] 更新目标点赞数失败 - targetId: {}", targetId, e);
        }
    }

    /**
     * 发布点赞事件
     */
    private void publishLikeEvent(Long userId, Long targetId, Integer type, boolean isLike) {
        LikeEvent.LikeType likeType = (type == LikeType.POST.getCode()) ? LikeEvent.LikeType.POST : LikeEvent.LikeType.COMMENT;
        LikeEvent event = new LikeEvent(userId, targetId, likeType, isLike);
        eventPublisher.publishEvent(event);
        log.debug("[点赞服务] 发布点赞事件成功 - userId: {}, targetId: {}, type: {}", userId, targetId, type);
    }
    
    // ==================== 批量查询方法 ====================
    
    /**
     * 批量检查点赞状态
     *
     * @param userId 用户ID
     * @param type 点赞类型
     * @param targetIds 目标ID列表
     * @return 已点赞的目标ID集合
     */
    public java.util.Set<Long> batchCheckStatus(Long userId, int type, java.util.List<Long> targetIds) {
        if (userId == null || targetIds == null || targetIds.isEmpty()) {
            return new java.util.HashSet<>();
        }
        try {
            return likeMapper.batchCheckStatus(userId, type, targetIds);
        } catch (Exception e) {
            log.error("[点赞服务] 批量检查点赞状态失败 - userId: {}", userId, e);
            return new java.util.HashSet<>();
        }
    }
    
    /**
     * 获取用户的点赞记录
     *
     * @param userId 用户ID
     * @param page 页码
     * @param size 每页数量
     * @return 点赞记录列表
     */
    public java.util.List<Like> getUserLikes(Long userId, Integer page, Integer size) {
        if (userId == null) {
            return new java.util.ArrayList<>();
        }
        int offset = (page - 1) * size;
        return likeMapper.findByUserId(userId, offset, size);
    }
    
    /**
     * 统计用户点赞数
     *
     * @param userId 用户ID
     * @return 点赞数量
     */
    public long countUserLikes(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long count = likeMapper.countByUserId(userId);
        return count != null ? count : 0L;
    }
}
