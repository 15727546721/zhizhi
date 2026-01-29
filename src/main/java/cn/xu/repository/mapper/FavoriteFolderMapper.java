package cn.xu.repository.mapper;

import cn.xu.model.entity.FavoriteFolder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏夹Mapper接口
 */
@Mapper
public interface FavoriteFolderMapper {
    
    /**
     * 插入收藏夹
     */
    void insert(FavoriteFolder folder);
    
    /**
     * 更新收藏夹
     */
    void update(FavoriteFolder folder);
    
    /**
     * 根据ID查询
     */
    FavoriteFolder selectById(@Param("id") Long id);
    
    /**
     * 根据用户ID查询所有收藏夹
     */
    List<FavoriteFolder> selectByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户的默认收藏夹
     */
    FavoriteFolder selectDefaultByUserId(@Param("userId") Long userId);
    
    /**
     * 删除收藏夹
     */
    void deleteById(@Param("id") Long id);
    
    /**
     * 增加收藏数量
     */
    void incrementItemCount(@Param("id") Long id);
    
    /**
     * 减少收藏数量
     */
    void decrementItemCount(@Param("id") Long id);
    
    /**
     * 更新收藏数量(直接设置)
     */
    void updateItemCount(@Param("id") Long id, @Param("count") int count);
    
    /**
     * 统计用户收藏夹数量
     */
    int countByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户公开的收藏夹
     */
    List<FavoriteFolder> selectPublicByUserId(@Param("userId") Long userId);
}
