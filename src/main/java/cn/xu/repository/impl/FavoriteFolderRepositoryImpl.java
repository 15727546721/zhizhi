package cn.xu.repository.impl;

import cn.xu.model.entity.FavoriteFolder;
import cn.xu.repository.FavoriteFolderRepository;
import cn.xu.repository.mapper.FavoriteFolderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 收藏夹仓储实现
 */
@Repository
@RequiredArgsConstructor
public class FavoriteFolderRepositoryImpl implements FavoriteFolderRepository {

    private final FavoriteFolderMapper favoriteFolderMapper;

    @Override
    public void save(FavoriteFolder folder) {
        favoriteFolderMapper.insert(folder);
    }

    @Override
    public void update(FavoriteFolder folder) {
        favoriteFolderMapper.update(folder);
    }

    @Override
    public FavoriteFolder findById(Long id) {
        return favoriteFolderMapper.selectById(id);
    }

    @Override
    public List<FavoriteFolder> findByUserId(Long userId) {
        return favoriteFolderMapper.selectByUserId(userId);
    }

    @Override
    public FavoriteFolder findDefaultByUserId(Long userId) {
        return favoriteFolderMapper.selectDefaultByUserId(userId);
    }

    @Override
    public void deleteById(Long id) {
        favoriteFolderMapper.deleteById(id);
    }

    @Override
    public void incrementItemCount(Long id) {
        favoriteFolderMapper.incrementItemCount(id);
    }

    @Override
    public void decrementItemCount(Long id) {
        favoriteFolderMapper.decrementItemCount(id);
    }

    @Override
    public void updateItemCount(Long id, int count) {
        favoriteFolderMapper.updateItemCount(id, count);
    }

    @Override
    public int countByUserId(Long userId) {
        return favoriteFolderMapper.countByUserId(userId);
    }

    @Override
    public List<FavoriteFolder> findPublicByUserId(Long userId) {
        return favoriteFolderMapper.selectPublicByUserId(userId);
    }
}
