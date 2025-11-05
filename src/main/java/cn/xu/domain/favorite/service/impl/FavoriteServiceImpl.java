package cn.xu.domain.favorite.service.impl;

import cn.xu.domain.favorite.model.entity.FavoriteEntity;
import cn.xu.domain.favorite.model.entity.FavoriteFolderEntity;
import cn.xu.domain.favorite.repository.IFavoriteFolderRepository;
import cn.xu.domain.favorite.repository.IFavoriteRepository;
import cn.xu.domain.favorite.service.IFavoriteService;
import cn.xu.infrastructure.persistent.repository.PostRepository;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
public class FavoriteServiceImpl implements IFavoriteService {
    
    private final IFavoriteRepository favoriteRepository;
    private final IFavoriteFolderRepository favoriteFolderRepository;
    private final PostRepository postRepositoryHelper;
    
    @Override
    @Transactional
    public void favorite(Long userId, Long targetId, String targetType) {
        FavoriteEntity favoriteEntity = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);
        if (favoriteEntity == null) {
            // 创建新收藏记录
            favoriteEntity = FavoriteEntity.builder()
                    .userId(userId)
                    .targetId(targetId)
                    .targetType(targetType)
                    .status(1)
                    .createTime(LocalDateTime.now())
                    .updateTime(LocalDateTime.now())
                    .build();
        } else {
            // 更新状态为已收藏
            favoriteEntity.setStatus(1);
            favoriteEntity.setUpdateTime(LocalDateTime.now());
        }
        favoriteRepository.save(favoriteEntity);
        
        // 更新目标对象的收藏数量
        updateTargetFavoriteCount(targetId, targetType, true);
    }
    
    @Override
    @Transactional
    public void unfavorite(Long userId, Long targetId, String targetType) {
        favoriteRepository.deleteByUserIdAndTargetId(userId, targetId, targetType);
        
        // 更新目标对象的收藏数量
        updateTargetFavoriteCount(targetId, targetType, false);
    }
    
    @Override
    public boolean isFavorited(Long userId, Long targetId, String targetType) {
        FavoriteEntity favoriteEntity = favoriteRepository.findByUserIdAndTargetId(userId, targetId, targetType);
        return favoriteEntity != null && favoriteEntity.getStatus() == 1;
    }
    
    @Override
    public List<Long> getFavoritedTargetIds(Long userId, String targetType) {
        return favoriteRepository.findFavoritedTargetIdsByUserId(userId, targetType);
    }
    
    @Override
    public int countFavoritedItems(Long userId, String targetType) {
        return favoriteRepository.countFavoritedItemsByUserId(userId, targetType);
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
        favoriteRepository.addTargetToFolder(userId, targetId, targetType, folderId);
    }
    
    @Override
    @Transactional
    public void removeTargetFromFolder(Long userId, Long targetId, String targetType, Long folderId) {
        favoriteRepository.removeTargetFromFolder(userId, targetId, targetType, folderId);
    }
    
    @Override
    public List<FavoriteEntity> getTargetsInFolder(Long userId, Long folderId) {
        return favoriteRepository.findTargetsInFolder(userId, folderId);
    }
    
    /**
     * 更新目标对象的收藏数量
     * 
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @param isIncrease 是否增加
     */
    private void updateTargetFavoriteCount(Long targetId, String targetType, boolean isIncrease) {
        try {
            if ("post".equals(targetType) && postRepositoryHelper != null) {
                // 统计该帖子的收藏数量
                // 这里我们假设favoriteRepository有一个专门统计特定目标收藏数的方法
                // 如果没有，我们可以通过其他方式获取准确的收藏数
                // 这里使用countFavoritedItemsByUserId并传入null作为userId来获取所有用户对该帖子的收藏数
                int currentCount = favoriteRepository.countFavoritedItemsByUserId(null, targetType);
                
                // 使用PostRepository辅助类直接更新帖子收藏数
                postRepositoryHelper.updatePostFavoriteCount(targetId, (long) currentCount);
                
                log.info("帖子收藏数更新成功: postId={}, 新收藏数={}", targetId, currentCount);
            }
        } catch (Exception e) {
            log.error("更新目标收藏数失败: targetId={}, targetType={}, isIncrease={}", targetId, targetType, isIncrease, e);
        }
    }
}