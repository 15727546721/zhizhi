package cn.xu.repository;

import cn.xu.model.entity.FavoriteFolder;

import java.util.List;

/**
 * 收藏夹仓储接口
 */
public interface FavoriteFolderRepository {
    
    /**
     * 保存收藏夹
     */
    void save(FavoriteFolder folder);
    
    /**
     * 更新收藏夹
     */
    void update(FavoriteFolder folder);
    
    /**
     * 根据ID查询
     */
    FavoriteFolder findById(Long id);
    
    /**
     * 根据用户ID查询所有收藏夹
     */
    List<FavoriteFolder> findByUserId(Long userId);
    
    /**
     * 查询用户的默认收藏夹
     */
    FavoriteFolder findDefaultByUserId(Long userId);
    
    /**
     * 删除收藏夹
     */
    void deleteById(Long id);
    
    /**
     * 增加收藏数量
     */
    void incrementItemCount(Long id);
    
    /**
     * 减少收藏数量
     */
    void decrementItemCount(Long id);
    
    /**
     * 更新收藏数量(直接设置)
     */
    void updateItemCount(Long id, int count);
    
    /**
     * 统计用户收藏夹数量
     */
    int countByUserId(Long userId);
    
    /**
     * 查询用户公开的收藏夹
     */
    List<FavoriteFolder> findPublicByUserId(Long userId);
}
