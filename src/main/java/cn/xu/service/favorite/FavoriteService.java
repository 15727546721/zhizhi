package cn.xu.service.favorite;

import cn.xu.cache.FavoriteCacheRepository;
import cn.xu.event.publisher.SocialEventPublisher;
import cn.xu.model.entity.Favorite;
import cn.xu.model.entity.Favorite.FavoriteType;
import cn.xu.repository.IFavoriteRepository;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 收藏服务
 *
 * <p>提供收藏、取消收藏、查询收藏等功能
 */
@Slf4j
@Service("favoriteService")
@RequiredArgsConstructor
public class FavoriteService {

    private final IFavoriteRepository favoriteRepository;
    private final PostMapper postMapper;
    private final FavoriteCacheRepository favoriteCacheRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final SocialEventPublisher socialEventPublisher;

    // ==================== 核心业务方法 ====================

    /**
     * 收藏操作
     *
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型：post-帖子
     */
    @Transactional(rollbackFor = Exception.class)
    public void favorite(Long userId, Long targetId, String targetType) {
        try {
            // 1. 验证参数和类型
            FavoriteType.fromCode(targetType);

            log.info("[收藏] 开始收藏 - userId: {}, targetId: {}, type: {}",
                    userId, targetId, targetType);

            // 2. 查询现有收藏记录，检查是否已收藏（幂等处理）
            Favorite existingFavorite = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);

            if (existingFavorite != null && existingFavorite.isFavorited()) {
                // 已收藏，幂等返回（不报错，不重复增加计数）
                log.info("[收藏] 用户已收藏 - userId: {}, targetId: {}", userId, targetId);
                return;
            }

            // 3. 创建新收藏记录
            if (existingFavorite == null) {
                // 新增记录
                Favorite newFavorite = Favorite.createFavorite(userId, targetId, targetType, null);
                favoriteRepository.save(newFavorite);
                log.info("[收藏服务] 创建收藏记录成功");
            } else {
                // 更新已收藏记录
                existingFavorite.favorite();
                favoriteRepository.save(existingFavorite);
                log.info("[收藏服务] 更新收藏记录成功");
            }

            // 4. 更新目标项的收藏计数（如帖子）
            updateTargetFavoriteCount(targetId, targetType, true);

            // 5. 事务提交后更新缓存
            final String finalTargetType = targetType;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 删除收藏计数缓存
                        favoriteCacheRepository.deleteFavoriteCount(targetId, finalTargetType);

                        // 增加用户收藏缓存
                        favoriteCacheRepository.addUserFavoriteRelation(userId, targetId, finalTargetType);

                        // 增加用户的收藏计数缓存
                        favoriteCacheRepository.incrementUserFavoriteCount(userId, finalTargetType);

                        log.info("[收藏服务] 事务提交后更新缓存成功");

                        // 发布收藏事件
                        socialEventPublisher.publishFavorited(userId, targetId);
                        log.debug("[收藏服务] 发布收藏事件 - userId: {}, targetId: {}", userId, targetId);
                    } catch (Exception e) {
                        log.error("[收藏服务] 事务提交后更新缓存失败", e);
                    }
                }
            });

            log.info("[收藏服务] 收藏操作完成");

        } catch (BusinessException e) {
            log.error("[收藏服务] 收藏失败 - error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[收藏服务] 收藏失败", e);
            throw new BusinessException("收藏失败，请稍后再试");
        }
    }

    /**
     * 取消收藏操作
     *
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     */
    @Transactional(rollbackFor = Exception.class)
    public void unfavorite(Long userId, Long targetId, String targetType) {
        try {
            // 1. 验证参数和类型
            FavoriteType.fromCode(targetType);

            log.info("[收藏服务] 开始取消收藏 - userId: {}, targetId: {}, type: {}",
                    userId, targetId, targetType);

            // 2. 查询现有收藏记录
            Favorite existingFavorite = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);

            // 目标未收藏，直接返回
            if (existingFavorite == null || !existingFavorite.isFavorited()) {
                log.info("[收藏服务] 用户未收藏该目标，取消收藏操作返回 - userId: {}, targetId: {}", userId, targetId);
                return;
            }

            // 3. 执行取消收藏操作
            existingFavorite.unfavorite(); // 更新PO状态
            favoriteRepository.save(existingFavorite);
            log.info("[收藏服务] 取消收藏操作成功");

            // 4. 更新目标项的收藏计数
            updateTargetFavoriteCount(targetId, targetType, false);

            // 5. 事务提交后更新缓存
            final String finalTargetType = targetType;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 删除收藏计数缓存
                        favoriteCacheRepository.deleteFavoriteCount(targetId, finalTargetType);

                        // 移除用户收藏关系缓存（修复：之前缺失这一步）
                        favoriteCacheRepository.removeUserFavoriteRelation(userId, targetId, finalTargetType);

                        // 减少用户收藏计数缓存
                        favoriteCacheRepository.decrementUserFavoriteCount(userId, finalTargetType);

                        log.info("[收藏服务] 取消收藏缓存更新成功 - userId: {}, targetId: {}", userId, targetId);

                        // 发布取消收藏事件
                        socialEventPublisher.publishUnfavorited(userId, targetId);
                    } catch (Exception e) {
                        log.error("[收藏服务] 取消收藏缓存更新失败", e);
                    }
                }
            });

            log.info("[收藏服务] 取消收藏操作完成");

        } catch (BusinessException e) {
            log.error("[收藏服务] 取消收藏失败 - error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[收藏服务] 取消收藏失败", e);
            throw new BusinessException("取消收藏失败，请稍后再试");
        }
    }

    /**
     * 检查用户是否收藏了指定目标
     *
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return true-已收藏，false-未收藏
     */
    public boolean isFavorited(Long userId, Long targetId, String targetType) {
        if (userId == null || targetId == null || targetType == null) {
            return false;
        }

        try {
            // 1. 查询缓存，快速返回已收藏信息
            Boolean cachedResult = favoriteCacheRepository.checkUserFavoriteRelation(userId, targetId, targetType);
            if (Boolean.TRUE.equals(cachedResult)) {
                log.debug("[收藏服务] 缓存检查用户收藏状态 - 用户已收藏");
                return true;
            }

            // 2. 查询数据库，检查是否已收藏
            Favorite favorite = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);
            boolean isFavorited = (favorite != null && favorite.isFavorited());

            // 3. 如果已收藏，更新缓存
            if (isFavorited) {
                favoriteCacheRepository.addUserFavoriteRelation(userId, targetId, targetType);
                log.debug("[收藏服务] 数据库检查收藏状态，更新缓存成功");
            } else {
                log.debug("[收藏服务] 数据库检查收藏状态，用户未收藏");
            }

            return isFavorited;

        } catch (Exception e) {
            log.error("[收藏服务] 检查收藏状态失败", e);
            // 发生异常时，直接查询数据库
            Favorite favorite = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);
            return (favorite != null && favorite.isFavorited());
        }
    }

    // ==================== 辅助方法 ====================

    /**
     * 更新目标项的收藏计数
     *
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param isIncrease 是否增加收藏计数
     */
    private void updateTargetFavoriteCount(Long targetId, String targetType, boolean isIncrease) {
        FavoriteType type = FavoriteType.fromCode(targetType);
        if (type == FavoriteType.POST) {
            // 更新帖子类型的收藏计数
            if (isIncrease) {
                postMapper.increaseFavoriteCount(targetId);
                log.info("[收藏服务] 增加帖子收藏计数 - postId: {}", targetId);
            } else {
                postMapper.decreaseFavoriteCount(targetId);
                log.info("[收藏服务] 减少帖子收藏计数 - postId: {}", targetId);
            }
        }
    }
    
    /**
     * 统计用户收藏数
     *
     * @param userId 用户ID
     * @return 收藏数量
     */
    public long countByUserId(Long userId) {
        if (userId == null) {
            return 0;
        }
        return favoriteRepository.countByUserId(userId);
    }
    
    /**
     * 分页获取用户收藏的目标ID列表
     *
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param page 页码
     * @param size 每页数量
     * @return 目标ID列表
     */
    public java.util.List<Long> getFavoritedTargetIdsWithPage(Long userId, String targetType, int page, int size) {
        if (userId == null) {
            return new java.util.ArrayList<>();
        }
        // 确保page至少为1，避免产生负数offset
        if (page < 1) page = 1;
        if (size < 1) size = 10;
        int offset = (page - 1) * size;
        return favoriteRepository.findFavoritedTargetIdsByUserIdWithPage(userId, targetType, null, offset, size);
    }
    
    /**
     * 统计用户收藏的目标数量
     *
     * @param userId 用户ID
     * @param targetType 目标类型
     * @return 收藏数量
     */
    public int countFavoritedItems(Long userId, String targetType) {
        if (userId == null) {
            return 0;
        }
        return favoriteRepository.countFavoritedItemsByUserId(userId, targetType);
    }
    
    /**
     * 统计目标被收藏的次数
     *
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 被收藏次数
     */
    public int countFavoritedItemsByTarget(Long targetId, String targetType) {
        if (targetId == null || targetType == null) {
            return 0;
        }
        return favoriteRepository.countFavoritedItemsByTarget(targetId, targetType);
    }
    
    /**
     * 获取目标的收藏数
     * <p>策略：缓存优先，未命中则查DB并回写缓存</p>
     *
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 收藏数
     */
    public long getFavoriteCount(Long targetId, String targetType) {
        if (targetId == null || targetType == null) {
            return 0L;
        }
        
        try {
            // 1. 尝试从缓存获取
            Long cachedCount = favoriteCacheRepository.getFavoriteCount(targetId, targetType);
            if (cachedCount != null && cachedCount > 0) {
                return cachedCount;
            }
            
            // 2. 缓存未命中，从目标表获取真实收藏数
            long dbCount = getTargetFavoriteCountFromDB(targetId, targetType);
            
            // 3. 回写缓存
            if (dbCount >= 0) {
                favoriteCacheRepository.setFavoriteCount(targetId, targetType, dbCount);
            }
            
            return dbCount;
        } catch (Exception e) {
            log.error("[收藏服务] 获取收藏数失败 - targetId: {}", targetId, e);
            // 降级：直接查DB
            return getTargetFavoriteCountFromDB(targetId, targetType);
        }
    }
    
    /**
     * 从目标表获取收藏数（统一数据来源）
     */
    private long getTargetFavoriteCountFromDB(Long targetId, String targetType) {
        try {
            FavoriteType type = FavoriteType.fromCode(targetType);
            if (type == FavoriteType.POST) {
                // 从post表获取favorite_count字段
                Long postCount = postMapper.getFavoriteCount(targetId);
                return postCount != null ? postCount : 0L;
            }
            return 0L;
        } catch (Exception e) {
            log.error("[收藏服务] 从目标表获取收藏数失败 - targetId: {}, type: {}", targetId, targetType, e);
            return 0L;
        }
    }
}
