package cn.xu.domain.favorite.service.impl;

import cn.xu.domain.favorite.model.entity.FavoriteEntity;
import cn.xu.domain.favorite.model.entity.FavoriteFolderEntity;
import cn.xu.domain.favorite.model.valobj.TargetType;
import cn.xu.domain.favorite.repository.IFavoriteFolderRepository;
import cn.xu.domain.favorite.repository.IFavoriteRepository;
import cn.xu.domain.favorite.service.IFavoriteService;
import cn.xu.infrastructure.cache.FavoriteCacheRepository;
import cn.xu.infrastructure.persistent.repository.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 收藏服务实现类
 */
@Slf4j
@Service
public class FavoriteServiceImpl implements IFavoriteService {
    
    private final IFavoriteRepository favoriteRepository;
    private final IFavoriteFolderRepository favoriteFolderRepository;
    private final PostRepository postRepositoryHelper;
    private final FavoriteCacheRepository favoriteCacheRepository;
    
    // 添加构造函数来记录依赖注入状态
    public FavoriteServiceImpl(IFavoriteRepository favoriteRepository,
                              IFavoriteFolderRepository favoriteFolderRepository,
                              @org.springframework.beans.factory.annotation.Qualifier("postRepositoryHelper") PostRepository postRepositoryHelper,
                              FavoriteCacheRepository favoriteCacheRepository) {
        this.favoriteRepository = favoriteRepository;
        this.favoriteFolderRepository = favoriteFolderRepository;
        this.postRepositoryHelper = postRepositoryHelper;
        this.favoriteCacheRepository = favoriteCacheRepository;
        
        log.info("[收藏服务] FavoriteServiceImpl初始化，postRepositoryHelper: {}", 
                postRepositoryHelper != null ? "已注入" : "未注入");
        if (postRepositoryHelper != null) {
            log.info("[收藏服务] postRepositoryHelper类型: {}", postRepositoryHelper.getClass().getName());
        } else {
            log.error("[收藏服务] postRepositoryHelper未注入！这会导致收藏数无法更新！");
        }
    }
    
    @Override
    @Transactional
    public void favorite(Long userId, Long targetId, String targetType) {
        try {
            // 将字符串转换为枚举
            TargetType targetTypeEnum = TargetType.fromCode(targetType);
            log.info("[收藏服务] 开始收藏操作，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
            
            // 使用枚举的数据库代码进行查询（兼容现有数据库存储）
            String dbTargetType = targetTypeEnum.getDbCode();
            FavoriteEntity favoriteEntity = favoriteRepository.findByUserIdAndTargetId(userId, targetId, dbTargetType);
            boolean shouldIncrementCache = false;
            
            if (favoriteEntity == null) {
                // 创建新收藏记录
                favoriteEntity = FavoriteEntity.builder()
                        .userId(userId)
                        .targetId(targetId)
                        .targetType(dbTargetType) // 存储为小写
                        .status(1)
                        .createTime(LocalDateTime.now())
                        .updateTime(LocalDateTime.now())
                        .build();
                favoriteRepository.save(favoriteEntity);
                shouldIncrementCache = true;
                log.info("[收藏服务] 创建新收藏记录，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
            } else {
                // 如果已存在收藏关系，检查当前状态
                if (favoriteEntity.getStatus() != 1) {
                    // 更新状态为已收藏
                    favoriteEntity.setStatus(1);
                    favoriteEntity.setUpdateTime(LocalDateTime.now());
                    favoriteRepository.save(favoriteEntity);
                    shouldIncrementCache = true;
                    log.info("[收藏服务] 更新现收藏记录，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
                } else {
                    log.warn("[收藏服务] 用户已收藏，无需重复操作，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
                    return; // 已经收藏，直接返回
                }
            }
            
            // 只有在确实执行了收藏操作时才更新缓存和数据库
            if (shouldIncrementCache) {
                // 先更新数据库中的收藏数（确保数据一致性）
                updateTargetFavoriteCount(targetId, targetTypeEnum, true);
                
                // 然后更新Redis缓存（数据库更新成功后再更新缓存）
                favoriteCacheRepository.incrementFavoriteCount(targetId, dbTargetType, 1);
                
                // 添加用户收藏关系缓存
                favoriteCacheRepository.addUserFavoriteRelation(userId, targetId, dbTargetType);
                
                log.info("[收藏服务] 更新缓存成功，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
            }
            
            log.info("[收藏服务] 收藏操作成功完成");
        } catch (Exception e) {
            log.error("[收藏服务] 收藏操作失败，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetType, e);
            throw e;
        }
    }
    
    @Override
    @Transactional
    public void unfavorite(Long userId, Long targetId, String targetType) {
        try {
            // 将字符串转换为枚举
            TargetType targetTypeEnum = TargetType.fromCode(targetType);
            log.info("[收藏服务] 开始取消收藏操作，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
            
            // 使用枚举的数据库代码进行查询（兼容现有数据库存储）
            String dbTargetType = targetTypeEnum.getDbCode();
            FavoriteEntity favoriteEntity = favoriteRepository.findByUserIdAndTargetId(userId, targetId, dbTargetType);
            boolean shouldDecrementCache = false;
            
            if (favoriteEntity != null && favoriteEntity.getStatus() == 1) {
                // 更新状态为未收藏（而不是删除记录，保持数据一致性）
                favoriteEntity.setStatus(0);
                favoriteEntity.setUpdateTime(LocalDateTime.now());
                favoriteRepository.save(favoriteEntity);
                shouldDecrementCache = true;
                log.info("[收藏服务] 更新收藏记录状态为未收藏，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
            } else {
                log.warn("[收藏服务] 用户未收藏，无需取消操作，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
                // 即使数据库状态为未收藏，也要确保缓存一致性
                boolean cachedFavorited = favoriteCacheRepository.checkUserFavoriteRelation(userId, targetId, dbTargetType);
                if (cachedFavorited) {
                    favoriteCacheRepository.removeUserFavoriteRelation(userId, targetId, dbTargetType);
                    favoriteCacheRepository.incrementFavoriteCount(targetId, dbTargetType, -1);
                    log.info("[收藏服务] 清理不一致的缓存数据，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
                }
                return; // 已经取消收藏，直接返回
            }
            
            // 只有在确实执行了取消收藏操作时才更新缓存和数据库
            if (shouldDecrementCache) {
                // 先更新数据库中的收藏数（确保数据一致性）
                updateTargetFavoriteCount(targetId, targetTypeEnum, false);
                
                // 然后更新Redis缓存（数据库更新成功后再更新缓存）
                favoriteCacheRepository.incrementFavoriteCount(targetId, dbTargetType, -1);
                
                // 移除用户收藏关系缓存
                favoriteCacheRepository.removeUserFavoriteRelation(userId, targetId, dbTargetType);
                
                log.info("[收藏服务] 更新缓存成功，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
            }
            
            log.info("[收藏服务] 取消收藏操作成功完成");
        } catch (Exception e) {
            log.error("[收藏服务] 取消收藏操作失败，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetType, e);
            throw e;
        }
    }
    
    @Override
    public boolean isFavorited(Long userId, Long targetId, String targetType) {
        try {
            // 将字符串转换为枚举
            TargetType targetTypeEnum = TargetType.fromCode(targetType);
            String dbTargetType = targetTypeEnum.getDbCode();
            log.info("[收藏服务] 开始检查收藏状态，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetTypeEnum);
            
            // 优先从缓存检查用户收藏关系
            boolean cachedResult = favoriteCacheRepository.checkUserFavoriteRelation(userId, targetId, dbTargetType);
            if (cachedResult) {
                log.debug("[收藏服务] 从缓存中获取到收藏状态：已收藏");
                return true;
            }
            
            // 缓存未命中，从数据库检查
            FavoriteEntity favoriteEntity = favoriteRepository.findByUserIdAndTargetId(userId, targetId, dbTargetType);
            boolean dbResult = favoriteEntity != null && favoriteEntity.getStatus() == 1;
            
            // 如果数据库中有收藏记录，同步到缓存
            if (dbResult) {
                favoriteCacheRepository.addUserFavoriteRelation(userId, targetId, dbTargetType);
                log.debug("[收藏服务] 从数据库中获取到收藏状态：已收藏，并同步到缓存");
            } else {
                log.debug("[收藏服务] 从数据库中获取到收藏状态：未收藏");
            }
            
            return dbResult;
        } catch (Exception e) {
            log.error("[收藏服务] 检查收藏状态失败，用户: {}, 目标: {}, 类型: {}", userId, targetId, targetType, e);
            return false;
        }
    }
    
    @Override
    public List<Long> getFavoritedTargetIds(Long userId, String targetType) {
        TargetType targetTypeEnum = TargetType.fromCode(targetType);
        return favoriteRepository.findFavoritedTargetIdsByUserId(userId, targetTypeEnum.getDbCode());
    }
    
    @Override
    public int countFavoritedItems(Long userId, String targetType) {
        TargetType targetTypeEnum = TargetType.fromCode(targetType);
        return favoriteRepository.countFavoritedItemsByUserId(userId, targetTypeEnum.getDbCode());
    }
    
    @Override
    @Transactional
    public Long createFolder(Long userId, String name, String description) {
        FavoriteFolderEntity folderEntity = FavoriteFolderEntity.builder()
                .userId(userId)
                .name(name)
                .description(description)
                .isPublic(0) // 默认不公开
                .contentCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        return favoriteFolderRepository.save(folderEntity);
    }
    
    @Override
    @Transactional
    public void updateFolder(Long folderId, String name, String description) {
        FavoriteFolderEntity folderEntity = favoriteFolderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("收藏夹不存在"));
        folderEntity.setName(name);
        folderEntity.setDescription(description);
        folderEntity.setUpdateTime(LocalDateTime.now());
        favoriteFolderRepository.update(folderEntity);
    }
    
    @Override
    @Transactional
    public void deleteFolder(Long userId, Long folderId) {
        // 先验证收藏夹是否属于该用户
        FavoriteFolderEntity folderEntity = favoriteFolderRepository.findById(folderId)
                .orElseThrow(() -> new IllegalArgumentException("收藏夹不存在"));
        
        if (!folderEntity.getUserId().equals(userId)) {
            throw new IllegalArgumentException("无权限删除此收藏夹");
        }
        
        // 从收藏夹中移除所有内容
        List<FavoriteEntity> targets = favoriteRepository.findTargetsInFolder(userId, folderId);
        for (FavoriteEntity target : targets) {
            favoriteRepository.removeTargetFromFolder(userId, target.getTargetId(), target.getTargetType(), folderId);
        }
        
        // 删除收藏夹
        favoriteFolderRepository.deleteById(folderId);
    }
    
    @Override
    public List<FavoriteFolderEntity> getFoldersByUserId(Long userId) {
        return favoriteFolderRepository.findByUserId(userId);
    }
    
    @Override
    @Transactional
    public void addTargetToFolder(Long userId, Long targetId, String targetType, Long folderId) {
        TargetType targetTypeEnum = TargetType.fromCode(targetType);
        favoriteRepository.addTargetToFolder(userId, targetId, targetTypeEnum.getDbCode(), folderId);
    }
    
    @Override
    @Transactional
    public void removeTargetFromFolder(Long userId, Long targetId, String targetType, Long folderId) {
        TargetType targetTypeEnum = TargetType.fromCode(targetType);
        favoriteRepository.removeTargetFromFolder(userId, targetId, targetTypeEnum.getDbCode(), folderId);
    }
    
    @Override
    public List<FavoriteEntity> getTargetsInFolder(Long userId, Long folderId) {
        return favoriteRepository.findTargetsInFolder(userId, folderId);
    }
    
    @Override
    public int countFavoritedItemsByTarget(Long targetId, String targetType) {
        TargetType targetTypeEnum = TargetType.fromCode(targetType);
        return favoriteRepository.countFavoritedItemsByTarget(targetId, targetTypeEnum.getDbCode());
    }
    
    /**
     * 更新目标对象的收藏数量
     * 根据目标类型枚举进行更新
     * 
     * @param targetId 目标ID
     * @param targetType 目标类型枚举
     * @param isIncrease 是否增加
     */
    private void updateTargetFavoriteCount(Long targetId, TargetType targetType, boolean isIncrease) {
        try {
            log.info("[收藏服务] updateTargetFavoriteCount被调用 - 目标: {}, 类型: {}, isIncrease: {}", targetId, targetType, isIncrease);
            
            // 使用枚举方法判断是否为帖子类型
            if (targetType.isPost()) {
                log.info("[收藏服务] 目标类型为POST，继续处理");
                
                if (postRepositoryHelper == null) {
                    log.error("[收藏服务] postRepositoryHelper未注入，无法更新收藏数，目标: {}, 类型: {}", targetId, targetType);
                    throw new IllegalStateException("postRepositoryHelper未注入，无法更新收藏数");
                }
                
                log.info("[收藏服务] postRepositoryHelper已注入，开始计算增量值");
                // 使用增量更新方式，收藏时+1，取消收藏时-1
                long increment = isIncrease ? 1L : -1L;
                
                log.info("[收藏服务] 调用postRepositoryHelper.updatePostFavoriteCount，目标ID: {}, 增量: {}", targetId, increment);
                // 使用PostRepository辅助类更新帖子收藏数
                postRepositoryHelper.updatePostFavoriteCount(targetId, increment);
                
                log.info("[收藏服务] 更新数据库收藏数成功，目标: {}, 类型: {}, 增量: {}", targetId, targetType, increment);
            } else {
                log.info("[收藏服务] 暂不支持的目标类型，不更新收藏数，类型: {}", targetType);
            }
        } catch (Exception e) {
            log.error("[收藏服务] 更新数据库收藏数失败，目标: {}, 类型: {}, isIncrease: {}", targetId, targetType, isIncrease, e);
            throw e; // 数据库更新失败，抛出异常，事务回滚
        }
    }
}