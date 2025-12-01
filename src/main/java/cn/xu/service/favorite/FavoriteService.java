package cn.xu.service.favorite;

import cn.xu.cache.FavoriteCacheRepository;
import cn.xu.model.entity.Favorite;
import cn.xu.model.entity.Favorite.FavoriteType;
import cn.xu.model.entity.FavoriteFolder;
import cn.xu.repository.IFavoriteFolderRepository;
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

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

/**
 * 收藏服务
 *
 * @author xu
 */
@Slf4j
@Service("favoriteService")
@RequiredArgsConstructor
public class FavoriteService {
    
    private final IFavoriteRepository favoriteRepository;
    private final IFavoriteFolderRepository favoriteFolderRepository;
    private final PostMapper postMapper;
    private final FavoriteCacheRepository favoriteCacheRepository;
    private final ApplicationEventPublisher eventPublisher;
    
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
        favorite(userId, targetId, targetType, null);
    }
    
    /**
     * 收藏到指定收藏夹
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param folderId 收藏夹ID（可选）
     */
    @Transactional(rollbackFor = Exception.class)
    public void favorite(Long userId, Long targetId, String targetType, Long folderId) {
        try {
            // 1. 验证参数和类型
            FavoriteType.fromCode(targetType);
            
            log.info("[收藏服务] 开始收藏 - userId: {}, targetId: {}, type: {}, folderId: {}", 
                userId, targetId, targetType, folderId);
            
            // 2. 如果没有指定收藏夹，使用默认收藏夹
            if (folderId == null) {
                folderId = getOrCreateDefaultFolder(userId);
            }
            
            // 3. 查询现有收藏记录
            Favorite existingFavorite = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);
            
            if (existingFavorite != null) {
                // 2.1 已存在记录，执行收藏操作
                existingFavorite.favorite(); // PO业务方法
                if (folderId != null) {
                    existingFavorite.setFolderId(folderId);
                }
                favoriteRepository.save(existingFavorite);
                log.info("[收藏服务] 更新收藏记录成功");
            } else {
                // 2.2 不存在记录，创建新记录
                Favorite newFavorite = Favorite.createFavorite(userId, targetId, targetType, folderId); // PO工厂方法
                favoriteRepository.save(newFavorite);
                log.info("[收藏服务] 创建收藏记录成功");
            }
            
            // 3. 更新目标表的收藏数（Post）
            updateTargetFavoriteCount(targetId, targetType, true);
            
            // 4. 更新收藏夹内容数量
            if (folderId != null) {
                updateFolderCount(folderId, 1);
            }
            
            // 5. 在事务提交后更新Redis缓存
            final String finalTargetType = targetType;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 删除收藏数缓存（Cache Invalidation）
                        favoriteCacheRepository.deleteFavoriteCount(targetId, finalTargetType);
                        
                        // 添加用户收藏关系缓存
                        favoriteCacheRepository.addUserFavoriteRelation(userId, targetId, finalTargetType);
                        
                        // 更新用户收藏数量缓存
                        favoriteCacheRepository.incrementUserFavoriteCount(userId, finalTargetType);
                        
                        log.info("[收藏服务] 事务提交后更新缓存成功");
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
            
            if (existingFavorite == null) {
                log.warn("[收藏服务] 取消收藏失败，记录不存在");
                throw new BusinessException("您尚未收藏");
            }
            
            // 3. 执行取消收藏操作
            Long folderId = existingFavorite.getFolderId();
            existingFavorite.unfavorite(); // PO业务方法
            favoriteRepository.save(existingFavorite);
            log.info("[收藏服务] 取消收藏成功");
            
            // 4. 更新目标表的收藏数
            updateTargetFavoriteCount(targetId, targetType, false);
            
            // 5. 更新收藏夹内容数量
            if (folderId != null) {
                updateFolderCount(folderId, -1);
            }
            
            // 6. 立即移除用户收藏关系缓存（防止False Positive）
            favoriteCacheRepository.removeUserFavoriteRelation(userId, targetId, targetType);
            
            // 7. 在事务提交后删除收藏数缓存
            final String finalTargetType = targetType;
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    try {
                        // 删除收藏数缓存
                        favoriteCacheRepository.deleteFavoriteCount(targetId, finalTargetType);
                        
                        // 更新用户收藏数量缓存
                        favoriteCacheRepository.decrementUserFavoriteCount(userId, finalTargetType);
                        
                        log.info("[收藏服务] 事务提交后清理缓存成功");
                    } catch (Exception e) {
                        log.error("[收藏服务] 事务提交后清理缓存失败", e);
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
     * 检查收藏状态（带缓存自愈能力）
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
            // 1. 优先从缓存检查（注意：返回值可能为null）
            Boolean cachedResult = favoriteCacheRepository.checkUserFavoriteRelation(userId, targetId, targetType);
            if (Boolean.TRUE.equals(cachedResult)) {
                log.debug("[收藏服务] 从缓存命中收藏状态：已收藏");
                return true;
            }
            
            // 2. 缓存未命中，查询数据库
            Favorite favorite = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);
            boolean isFavorited = (favorite != null && favorite.isFavorited());
            
            // 3. 自愈：如果数据库中有收藏记录，同步到缓存
            if (isFavorited) {
                favoriteCacheRepository.addUserFavoriteRelation(userId, targetId, targetType);
                log.debug("[收藏服务] 从数据库查询收藏状态并自愈缓存：已收藏");
            } else {
                log.debug("[收藏服务] 从数据库查询收藏状态：未收藏");
            }
            
            return isFavorited;
            
        } catch (Exception e) {
            log.error("[收藏服务] 检查收藏状态失败", e);
            // 降级：直接查数据库
            Favorite favorite = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);
            return (favorite != null && favorite.isFavorited());
        }
    }
    
    /**
     * 获取用户收藏的目标ID列表
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     * @return 目标ID列表
     */
    public List<Long> getFavoritedTargetIds(Long userId, String targetType) {
        if (userId == null || targetType == null) {
            return Collections.emptyList();
        }
        
        try {
            return favoriteRepository.findFavoritedTargetIdsByUserId(userId, targetType);
        } catch (Exception e) {
            log.error("[收藏服务] 获取收藏列表失败 - userId: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取用户收藏的目标ID列表（分页）
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     * @param folderId 收藏夹ID（可选）
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 目标ID列表
     */
    public List<Long> getFavoritedTargetIdsWithPage(Long userId, String targetType, Long folderId, 
                                                     int offset, int limit) {
        if (userId == null || targetType == null) {
            return Collections.emptyList();
        }
        
        try {
            return favoriteRepository.findFavoritedTargetIdsByUserIdWithPage(
                userId, targetType, folderId, offset, limit);
        } catch (Exception e) {
            log.error("[收藏服务] 获取分页收藏列表失败 - userId: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 统计用户收藏数量
     * 
     * @param userId 用户ID
     * @param targetType 目标类型
     * @return 收藏数量
     */
    public int countFavoritedItems(Long userId, String targetType) {
        if (userId == null || targetType == null) {
            return 0;
        }
        
        try {
            return favoriteRepository.countFavoritedItemsByUserId(userId, targetType);
        } catch (Exception e) {
            log.error("[收藏服务] 统计用户收藏数量失败 - userId: {}", userId, e);
            return 0;
        }
    }
    
    /**
     * 统计目标被收藏的次数
     */
    public int countFavoritedItemsByTarget(Long targetId, String targetType) {
        if (targetId == null || targetType == null) {
            return 0;
        }
        
        try {
            return favoriteRepository.countFavoritedItemsByTarget(targetId, targetType);
        } catch (Exception e) {
            log.error("[收藏服务] 统计目标收藏数量失败 - targetId: {}", targetId, e);
            return 0;
        }
    }
    
    /**
     * 获取收藏夹中的内容列表
     */
    public List<Favorite> findTargetsInFolder(Long userId, Long folderId) {
        if (userId == null || folderId == null) {
            return Collections.emptyList();
        }
        
        try {
            return favoriteRepository.findTargetsInFolder(userId, folderId);
        } catch (Exception e) {
            log.error("[收藏服务] 获取收藏夹内容失败 - userId: {}, folderId: {}", userId, folderId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 移动到指定收藏夹
     * 
     * @param userId 用户ID
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param folderId 收藏夹ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void moveToFolder(Long userId, Long targetId, String targetType, Long folderId) {
        try {
            Favorite favorite = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);
            
            if (favorite == null) {
                throw new BusinessException("未找到收藏记录");
            }
            
            Long oldFolderId = favorite.getFolderId();
            favorite.moveToFolder(folderId); // PO业务方法
            favoriteRepository.save(favorite);
            
            // 更新收藏夹数量
            if (oldFolderId != null) {
                updateFolderCount(oldFolderId, -1);
            }
            if (folderId != null) {
                updateFolderCount(folderId, 1);
            }
            
            log.info("[收藏服务] 移动到收藏夹成功 - targetId: {}, oldFolderId: {}, newFolderId: {}", 
                targetId, oldFolderId, folderId);
                
        } catch (BusinessException e) {
            log.error("[收藏服务] 移动到收藏夹失败 - error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[收藏服务] 移动到收藏夹失败", e);
            throw new BusinessException("移动失败，请稍后再试");
        }
    }
    
    // ==================== 收藏夹管理方法 ====================
    
    /**
     * 创建收藏夹
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createFolder(Long userId, String name, String description) {
        try {
            FavoriteFolder folder = FavoriteFolder.builder()
                    .userId(userId)
                    .name(name)
                    .description(description)
                    .isPublic(0)
                    .contentCount(0)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();

            return favoriteFolderRepository.save(folder);
        } catch (Exception e) {
            log.error("[收藏服务] 创建收藏夹失败 - userId: {}, name: {}", userId, name, e);
            throw new BusinessException("创建收藏夹失败");
        }
    }
    
    /**
     * 获取用户的收藏夹列表
     */
    public List<FavoriteFolder> getFoldersByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        
        try {
            return favoriteFolderRepository.findByUserId(userId);
        } catch (Exception e) {
            log.error("[收藏服务] 获取收藏夹列表失败 - userId: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 删除收藏夹（默认收藏夹不能删除）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteFolder(Long userId, Long folderId) {
        try {
            FavoriteFolder folder = favoriteFolderRepository.findById(folderId)
                    .orElseThrow(() -> new BusinessException("收藏夹不存在"));
            
            if (!folder.getUserId().equals(userId)) {
                throw new BusinessException("无权限删除此收藏夹");
            }
            
            // 默认收藏夹不能删除
            if (folder.getIsDefault() != null && folder.getIsDefault() == 1) {
                throw new BusinessException("默认收藏夹不能删除");
            }
            
            // 移除收藏夹中的所有内容（设置folderId为null）
            List<Favorite> targets = favoriteRepository.findTargetsInFolder(userId, folderId);
            for (Favorite target : targets) {
                target.setFolderId(null);
                favoriteRepository.save(target);
            }
            log.info("[收藏服务] 已将收藏夹中的{}条内容移出 - folderId: {}", targets.size(), folderId);
            
            // 删除收藏夹
            favoriteFolderRepository.deleteById(folderId);
            
            // 清理缓存
            favoriteCacheRepository.deleteFolderContentCount(folderId);
            
            log.info("[收藏服务] 删除收藏夹成功 - folderId: {}", folderId);
            
        } catch (BusinessException e) {
            log.error("[收藏服务] 删除收藏夹失败 - error: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("[收藏服务] 删除收藏夹失败 - folderId: {}", folderId, e);
            throw new BusinessException("删除收藏夹失败");
        }
    }
    
    /**
     * 从收藏夹中移除内容
     */
    @Transactional(rollbackFor = Exception.class)
    public void removeTargetFromFolder(Long userId, Long targetId, String targetType, Long folderId) {
        try {
            Favorite favorite = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);
            if (favorite != null && favorite.getFolderId() != null && favorite.getFolderId().equals(folderId)) {
                favorite.setFolderId(null);
                favoriteRepository.save(favorite);
                
                // 更新收藏夹数量
                updateFolderCount(folderId, -1);
                
                log.info("[收藏服务] 从收藏夹移除内容成功 - targetId: {}, folderId: {}", targetId, folderId);
            }
        } catch (Exception e) {
            log.error("[收藏服务] 从收藏夹移除内容失败 - targetId: {}, folderId: {}", targetId, folderId, e);
            throw new BusinessException("移除失败，请稍后再试");
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    /**
     * 获取或创建用户的默认收藏夹
     * 
     * @param userId 用户ID
     * @return 默认收藏夹ID
     */
    private Long getOrCreateDefaultFolder(Long userId) {
        try {
            // 1. 查找已存在的默认收藏夹
            return favoriteFolderRepository.findDefaultFolderByUserId(userId)
                    .map(FavoriteFolder::getId)
                    .orElseGet(() -> {
                        // 2. 不存在则创建默认收藏夹
                        FavoriteFolder defaultFolder = FavoriteFolder.builder()
                                .userId(userId)
                                .name("默认收藏夹")
                                .description("系统自动创建的默认收藏夹")
                                .isDefault(1)  // 标记为默认收藏夹
                                .isPublic(0)
                                .contentCount(0)
                                .sort(0)
                                .createTime(LocalDateTime.now())
                                .updateTime(LocalDateTime.now())
                                .build();
                        Long folderId = favoriteFolderRepository.save(defaultFolder);
                        log.info("[收藏服务] 自动创建默认收藏夹 - userId: {}, folderId: {}", userId, folderId);
                        return folderId;
                    });
        } catch (Exception e) {
            log.error("[收藏服务] 获取或创建默认收藏夹失败 - userId: {}", userId, e);
            return null;  // 返回null，收藏仍可继续但不关联收藏夹
        }
    }
    
    /**
     * 更新目标表的收藏数
     * 
     * 注意：此方法在事务内执行，更新失败会抛出异常触发事务回滚，
     * 确保Favorite表和目标表的计数保持一致
     */
    private void updateTargetFavoriteCount(Long targetId, String targetType, boolean isIncrease) {
        FavoriteType type = FavoriteType.fromCode(targetType);
        if (type == FavoriteType.POST) {
            // 使用postMapper更新帖子收藏数
            if (isIncrease) {
                postMapper.increaseFavoriteCount(targetId);
                log.info("[收藏服务] 增加帖子收藏数成功 - postId: {}", targetId);
            } else {
                postMapper.decreaseFavoriteCount(targetId);
                log.info("[收藏服务] 减少帖子收藏数成功 - postId: {}", targetId);
            }
        }
        // 不捕获异常：让异常向上传播，触发事务回滚，保证数据一致性
    }
    
    /**
     * 更新收藏夹内容数量
     */
    private void updateFolderCount(Long folderId, int delta) {
        try {
            FavoriteFolder folder = favoriteFolderRepository.findById(folderId).orElse(null);
            if (folder != null) {
                int newCount = Math.max(0, folder.getContentCount() + delta);
                folder.setContentCount(newCount);
                favoriteFolderRepository.update(folder);
                
                // 更新缓存
                if (delta > 0) {
                    favoriteCacheRepository.incrementFolderContentCount(folderId);
                } else {
                    favoriteCacheRepository.decrementFolderContentCount(folderId);
                }
                
                log.info("[收藏服务] 更新收藏夹数量成功 - folderId: {}, newCount: {}", folderId, newCount);
            }
        } catch (Exception e) {
            log.error("[收藏服务] 更新收藏夹数量失败 - folderId: {}", folderId, e);
            // 不抛出异常，避免影响主流程
        }
    }
    
    /**
     * 统计用户收藏数量（所有类型）
     * @param userId 用户ID
     * @return 收藏总数
     */
    public long countByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        // 统计帖子收藏数量
        return favoriteRepository.countFavoritedItemsByUserId(userId, "post");
    }
    
}
