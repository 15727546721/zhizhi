package cn.xu.repository.impl;

import cn.xu.model.entity.Favorite;
import cn.xu.repository.IFavoriteRepository;
import cn.xu.repository.mapper.FavoriteMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 收藏仓储实现
 * <p>负责收藏数据的持久化操作</p>

 */
@Repository
@RequiredArgsConstructor
public class FavoriteRepository implements IFavoriteRepository {

    private final FavoriteMapper favoriteMapper;

    @Override
    public void save(Favorite favorite) {
        favoriteMapper.insertOrUpdate(favorite);
    }

    @Override
    public Favorite findByUserIdAndTargetId(Long userId, Long targetId, String targetType) {
        return favoriteMapper.selectByUserIdAndTargetId(userId, targetId, targetType);
    }

    @Override
    public void deleteByUserIdAndTargetId(Long userId, Long targetId, String targetType) {
        favoriteMapper.deleteByUserIdAndTargetId(userId, targetId, targetType);
    }

    @Override
    public List<Long> findFavoritedTargetIdsByUserId(Long userId, String targetType) {
        return favoriteMapper.selectFavoritedTargetIdsByUserId(userId, targetType);
    }

    @Override
    public List<Long> findFavoritedTargetIdsByUserIdWithPage(Long userId, String targetType, Long folderId, int offset, int limit) {
        return favoriteMapper.selectFavoritedTargetIdsByUserIdWithPage(userId, targetType, folderId, offset, limit);
    }

    @Override
    public int countFavoritedItemsByUserId(Long userId, String targetType) {
        return favoriteMapper.countFavoritedItemsByUserId(userId, targetType);
    }
    
    @Override
    public int countFavoritedItemsByTarget(Long targetId, String targetType) {
        return favoriteMapper.countFavoritedItemsByTarget(targetId, targetType);
    }

    @Override
    public void addTargetToFolder(Long userId, Long targetId, String targetType, Long folderId) {
        Favorite favorite = favoriteMapper.selectByUserIdAndTargetId(userId, targetId, targetType);
        if (favorite != null) {
            favoriteMapper.updateFolderId(favorite.getId(), folderId);
        }
    }

    @Override
    public void removeTargetFromFolder(Long userId, Long targetId, String targetType, Long folderId) {
        Favorite favorite = favoriteMapper.selectByUserIdAndTargetId(userId, targetId, targetType);
        if (favorite != null && favorite.getFolderId() != null && favorite.getFolderId().equals(folderId)) {
            favoriteMapper.updateFolderId(favorite.getId(), null);
        }
    }

    @Override
    public List<Favorite> findTargetsInFolder(Long userId, Long folderId) {
        return favoriteMapper.selectByFolderId(userId, folderId);
    }
    
    @Override
    public void batchUpdateFolderId(Long userId, Long oldFolderId, Long newFolderId) {
        favoriteMapper.batchUpdateFolderId(userId, oldFolderId, newFolderId);
    }
    
    @Override
    public List<Long> findUserIdsByTarget(Long targetId, String targetType) {
        if (targetId == null || targetType == null) {
            return new java.util.ArrayList<>();
        }
        List<Long> userIds = favoriteMapper.selectUserIdsByTarget(targetId, targetType);
        return userIds != null ? userIds : new java.util.ArrayList<>();
    }
    
    @Override
    public long countByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long count = favoriteMapper.countByUserId(userId);
        return count != null ? count : 0L;
    }
}
