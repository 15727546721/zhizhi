package cn.xu.repository.impl;

import cn.xu.model.entity.FavoriteFolder;
import cn.xu.repository.IFavoriteFolderRepository;
import cn.xu.repository.mapper.FavoriteFolderMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 收藏夹仓储实现
 *
 * @author xu
 */
@Repository
public class FavoriteFolderRepository implements IFavoriteFolderRepository {

    @Autowired
    private FavoriteFolderMapper favoriteFolderMapper;

    @Override
    public Long save(FavoriteFolder favoriteFolder) {
        favoriteFolderMapper.insert(favoriteFolder);
        return favoriteFolder.getId();
    }

    @Override
    public void update(FavoriteFolder favoriteFolder) {
        favoriteFolderMapper.update(favoriteFolder);
    }

    @Override
    public void deleteById(Long id) {
        favoriteFolderMapper.deleteById(id);
    }

    @Override
    public Optional<FavoriteFolder> findById(Long id) {
        FavoriteFolder favoriteFolder = favoriteFolderMapper.selectById(id);
        return Optional.ofNullable(favoriteFolder);
    }

    @Override
    public List<FavoriteFolder> findByUserId(Long userId) {
        return favoriteFolderMapper.selectByUserId(userId);
    }

    @Override
    public Optional<FavoriteFolder> findByUserIdAndName(Long userId, String name) {
        FavoriteFolder favoriteFolder = favoriteFolderMapper.selectByUserIdAndName(userId, name);
        return Optional.ofNullable(favoriteFolder);
    }

    @Override
    public Optional<FavoriteFolder> findDefaultFolderByUserId(Long userId) {
        FavoriteFolder favoriteFolder = favoriteFolderMapper.selectDefaultByUserId(userId);
        return Optional.ofNullable(favoriteFolder);
    }

    @Override
    public void updateContentCount(Long folderId, Integer count) {
        favoriteFolderMapper.updateContentCount(folderId, count);
    }
}