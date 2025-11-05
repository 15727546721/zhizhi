package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.favorite.model.entity.FavoriteFolderEntity;
import cn.xu.domain.favorite.repository.IFavoriteFolderRepository;
import cn.xu.infrastructure.persistent.mapper.FavoriteFolderMapper;
import cn.xu.infrastructure.persistent.po.FavoriteFolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 收藏夹仓储实现类
 */
@Repository
public class FavoriteFolderRepository implements IFavoriteFolderRepository {

    @Autowired
    private FavoriteFolderMapper favoriteFolderMapper;

    @Override
    public Long save(FavoriteFolderEntity favoriteFolderEntity) {
        FavoriteFolder favoriteFolder = convertToPO(favoriteFolderEntity);
        favoriteFolderMapper.insert(favoriteFolder);
        return favoriteFolder.getId();
    }

    @Override
    public void update(FavoriteFolderEntity favoriteFolderEntity) {
        FavoriteFolder favoriteFolder = convertToPO(favoriteFolderEntity);
        favoriteFolderMapper.update(favoriteFolder);
    }

    @Override
    public void deleteById(Long id) {
        favoriteFolderMapper.deleteById(id);
    }

    @Override
    public Optional<FavoriteFolderEntity> findById(Long id) {
        FavoriteFolder favoriteFolder = favoriteFolderMapper.selectById(id);
        if (favoriteFolder == null) {
            return Optional.empty();
        }
        return Optional.of(convertToEntity(favoriteFolder));
    }

    @Override
    public List<FavoriteFolderEntity> findByUserId(Long userId) {
        List<FavoriteFolder> favoriteFolders = favoriteFolderMapper.selectByUserId(userId);
        return favoriteFolders.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<FavoriteFolderEntity> findByUserIdAndName(Long userId, String name) {
        FavoriteFolder favoriteFolder = favoriteFolderMapper.selectByUserIdAndName(userId, name);
        if (favoriteFolder == null) {
            return Optional.empty();
        }
        return Optional.of(convertToEntity(favoriteFolder));
    }

    @Override
    public Optional<FavoriteFolderEntity> findDefaultFolderByUserId(Long userId) {
        FavoriteFolder favoriteFolder = favoriteFolderMapper.selectDefaultByUserId(userId);
        if (favoriteFolder == null) {
            return Optional.empty();
        }
        return Optional.of(convertToEntity(favoriteFolder));
    }

    @Override
    public void updateContentCount(Long folderId, Integer count) {
        favoriteFolderMapper.updateContentCount(folderId, count);
    }

    /**
     * 将实体转换为PO
     */
    private FavoriteFolder convertToPO(FavoriteFolderEntity entity) {
        FavoriteFolder favoriteFolder = new FavoriteFolder();
        favoriteFolder.setId(entity.getId());
        favoriteFolder.setUserId(entity.getUserId());
        favoriteFolder.setName(entity.getName());
        favoriteFolder.setDescription(entity.getDescription());
        favoriteFolder.setIsPublic(entity.getIsPublic());
        favoriteFolder.setContentCount(entity.getContentCount());
        favoriteFolder.setCreateTime(entity.getCreateTime());
        favoriteFolder.setUpdateTime(entity.getUpdateTime());
        return favoriteFolder;
    }

    /**
     * 将PO转换为实体
     */
    private FavoriteFolderEntity convertToEntity(FavoriteFolder favoriteFolder) {
        return FavoriteFolderEntity.builder()
                .id(favoriteFolder.getId())
                .userId(favoriteFolder.getUserId())
                .name(favoriteFolder.getName())
                .description(favoriteFolder.getDescription())
                .isPublic(favoriteFolder.getIsPublic())
                .contentCount(favoriteFolder.getContentCount())
                .createTime(favoriteFolder.getCreateTime())
                .updateTime(favoriteFolder.getUpdateTime())
                .build();
    }
}