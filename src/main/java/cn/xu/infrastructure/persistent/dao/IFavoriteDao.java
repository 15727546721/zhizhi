package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.FavoritePO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏DAO接口
 * 注意：为了兼容现有数据库结构，此接口仍然映射到collect表
 * 后续数据库迁移时可以更新表名
 */
@Mapper
public interface IFavoriteDao {
    /**
     * 插入或更新收藏记录
     */
    void insertOrUpdate(FavoritePO favoritePO);

    /**
     * 根据用户ID和内容ID查找收藏记录
     */
    FavoritePO selectByUserIdAndTargetId(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);

    /**
     * 删除收藏记录
     */
    void deleteByUserIdAndTargetId(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);

    /**
     * 获取用户收藏的内容ID列表
     */
    List<Long> selectFavoritedTargetIdsByUserId(@Param("userId") Long userId, @Param("targetType") String targetType);

    /**
     * 分页获取用户收藏的内容ID列表
     */
    List<Long> selectFavoritedTargetIdsByUserIdWithPage(
            @Param("userId") Long userId, 
            @Param("targetType") String targetType,
            @Param("folderId") Long folderId,
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 统计用户收藏的内容数量
     */
    int countFavoritedItemsByUserId(@Param("userId") Long userId, @Param("targetType") String targetType);
    
    /**
     * 统计特定目标的收藏数量（所有用户）
     */
    int countFavoritedItemsByTarget(@Param("targetId") Long targetId, @Param("targetType") String targetType);

    /**
     * 更新收藏夹ID
     */
    void updateFolderId(@Param("id") Long id, @Param("folderId") Long folderId);

    /**
     * 根据收藏夹ID查询收藏记录
     */
    List<FavoritePO> selectByFolderId(@Param("userId") Long userId, @Param("folderId") Long folderId);
    
    /**
     * 获取收藏某个目标的用户ID列表
     * @param targetId 目标ID
     * @param targetType 目标类型
     * @return 用户ID列表
     */
    List<Long> selectUserIdsByTarget(@Param("targetId") Long targetId, @Param("targetType") String targetType);
}