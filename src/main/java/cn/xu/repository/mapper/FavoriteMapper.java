package cn.xu.repository.mapper;

import cn.xu.model.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏Mapper接口
 * <p>处理收藏相关的数据库操作</p>
 
 */
@Mapper
public interface FavoriteMapper {
    
    /**
     * 插入或更新收藏记录
     */
    void insertOrUpdate(Favorite favorite);

    /**
     * 根据用户ID和内容ID查找收藏记录
     */
    Favorite selectByUserIdAndTargetId(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);

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
            @Param("offset") int offset,
            @Param("limit") int limit);

    /**
     * 统计用户收藏的内容数量
     */
    int countFavoritedItemsByUserId(@Param("userId") Long userId, @Param("targetType") String targetType);
    
    /**
     * 统计特定目标的收藏数量
     */
    int countFavoritedItemsByTarget(@Param("targetId") Long targetId, @Param("targetType") String targetType);
    
    /**
     * 获取收藏某个目标的用户ID列表
     */
    List<Long> selectUserIdsByTarget(@Param("targetId") Long targetId, @Param("targetType") String targetType);
    
    /**
     * 统计所有收藏数
     */
    Long countAll();
    
    /**
     * 统计用户收藏总数
     */
    Long countByUserId(@Param("userId") Long userId);
}
