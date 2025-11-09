package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.favorite.model.entity.FavoriteEntity;
import cn.xu.domain.favorite.repository.IFavoriteRepository;
import cn.xu.infrastructure.persistent.dao.IFavoriteDao;
import cn.xu.infrastructure.persistent.po.FavoritePO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏仓储实现类
 */
@Repository
public class FavoriteRepository implements IFavoriteRepository {

    @Autowired
    private IFavoriteDao favoriteDao;

    @Override
    public void save(FavoriteEntity favoriteEntity) {
        FavoritePO favoritePO = new FavoritePO();
        favoritePO.setId(favoriteEntity.getId());
        favoritePO.setUserId(favoriteEntity.getUserId());
        favoritePO.setTargetId(favoriteEntity.getTargetId());
        favoritePO.setTargetType(favoriteEntity.getTargetType());
        favoritePO.setFolderId(favoriteEntity.getFolderId());
        favoritePO.setStatus(favoriteEntity.getStatus());
        favoritePO.setCreateTime(favoriteEntity.getCreateTime());
        favoritePO.setUpdateTime(favoriteEntity.getUpdateTime());
        favoriteDao.insertOrUpdate(favoritePO);
    }

    @Override
    public FavoriteEntity findByUserIdAndTargetId(Long userId, Long targetId, String targetType) {
        FavoritePO favoritePO = favoriteDao.selectByUserIdAndTargetId(userId, targetId, targetType);
        if (favoritePO == null) {
            return null;
        }
        return convertToEntity(favoritePO);
    }

    @Override
    public void deleteByUserIdAndTargetId(Long userId, Long targetId, String targetType) {
        favoriteDao.deleteByUserIdAndTargetId(userId, targetId, targetType);
    }

    @Override
    public List<Long> findFavoritedTargetIdsByUserId(Long userId, String targetType) {
        return favoriteDao.selectFavoritedTargetIdsByUserId(userId, targetType);
    }

    @Override
    public List<Long> findFavoritedTargetIdsByUserIdWithPage(Long userId, String targetType, Long folderId, int offset, int limit) {
        return favoriteDao.selectFavoritedTargetIdsByUserIdWithPage(userId, targetType, folderId, offset, limit);
    }

    @Override
    public int countFavoritedItemsByUserId(Long userId, String targetType) {
        return favoriteDao.countFavoritedItemsByUserId(userId, targetType);
    }
    
    @Override
    public int countFavoritedItemsByTarget(Long targetId, String targetType) {
        return favoriteDao.countFavoritedItemsByTarget(targetId, targetType);
    }

    @Override
    public void addTargetToFolder(Long userId, Long targetId, String targetType, Long folderId) {
        FavoritePO favoritePO = favoriteDao.selectByUserIdAndTargetId(userId, targetId, targetType);
        if (favoritePO != null) {
            favoriteDao.updateFolderId(favoritePO.getId(), folderId);
        }
    }

    @Override
    public void removeTargetFromFolder(Long userId, Long targetId, String targetType, Long folderId) {
        FavoritePO favoritePO = favoriteDao.selectByUserIdAndTargetId(userId, targetId, targetType);
        if (favoritePO != null && favoritePO.getFolderId() != null && favoritePO.getFolderId().equals(folderId)) {
            favoriteDao.updateFolderId(favoritePO.getId(), null);
        }
    }

    @Override
    public List<FavoriteEntity> findTargetsInFolder(Long userId, Long folderId) {
        List<FavoritePO> favoritePOs = favoriteDao.selectByFolderId(userId, folderId);
        return favoritePOs.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }
    
    @Override
    public List<Long> findUserIdsByTarget(Long targetId, String targetType) {
        if (targetId == null || targetType == null) {
            return new java.util.ArrayList<>();
        }
        List<Long> userIds = favoriteDao.selectUserIdsByTarget(targetId, targetType);
        return userIds != null ? userIds : new java.util.ArrayList<>();
    }

    private FavoriteEntity convertToEntity(FavoritePO favoritePO) {
        return FavoriteEntity.builder()
                .id(favoritePO.getId())
                .userId(favoritePO.getUserId())
                .targetId(favoritePO.getTargetId())
                .targetType(favoritePO.getTargetType())
                .folderId(favoritePO.getFolderId())
                .status(favoritePO.getStatus())
                .createTime(favoritePO.getCreateTime())
                .updateTime(favoritePO.getUpdateTime())
                .build();
    }
}