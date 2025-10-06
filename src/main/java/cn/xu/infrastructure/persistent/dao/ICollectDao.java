package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.CollectPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 收藏DAO接口
 */
@Mapper
public interface ICollectDao {
    /**
     * 插入或更新收藏记录
     */
    void insertOrUpdate(CollectPO collectPO);

    /**
     * 根据用户ID和内容ID查找收藏记录
     */
    CollectPO selectByUserIdAndTargetId(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);

    /**
     * 删除收藏记录
     */
    void deleteByUserIdAndTargetId(@Param("userId") Long userId, @Param("targetId") Long targetId, @Param("targetType") String targetType);

    /**
     * 获取用户收藏的内容ID列表
     */
    List<Long> selectCollectedTargetIdsByUserId(@Param("userId") Long userId, @Param("targetType") String targetType);

    /**
     * 统计用户收藏的内容数量
     */
    int countCollectedItemsByUserId(@Param("userId") Long userId, @Param("targetType") String targetType);

    /**
     * 更新收藏夹ID
     */
    void updateFolderId(@Param("id") Long id, @Param("folderId") Long folderId);

    /**
     * 根据收藏夹ID查询收藏记录
     */
    List<CollectPO> selectByFolderId(@Param("userId") Long userId, @Param("folderId") Long folderId);
}