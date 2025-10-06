package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Collect;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CollectMapper {
    /**
     * 插入收藏记录
     *
     * @param collect 收藏对象
     * @return 插入记录数
     */
    int insert(Collect collect);

    /**
     * 根据用户ID和帖子ID查询收藏记录
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 收藏记录
     */
    Collect findByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 根据用户ID、帖子ID和收藏夹ID查询收藏记录
     *
     * @param userId    用户ID
     * @param postId    帖子ID
     * @param folderId  收藏夹ID
     * @return 收藏记录
     */
    Collect findByUserIdAndPostIdAndFolderId(@Param("userId") Long userId, 
                                           @Param("postId") Long postId, 
                                           @Param("folderId") Long folderId);

    /**
     * 更新收藏记录
     *
     * @param collect 收藏对象
     * @return 更新记录数
     */
    int updateById(Collect collect);

    /**
     * 根据用户ID和帖子ID删除收藏记录
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 删除记录数
     */
    int deleteByUserIdAndPostId(@Param("userId") Long userId, @Param("postId") Long postId);

    /**
     * 根据用户ID查询收藏的帖子ID列表
     *
     * @param userId 用户ID
     * @return 收藏的帖子ID列表
     */
    List<Long> findPostIdsByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID统计收藏的帖子数量
     *
     * @param userId 用户ID
     * @return 收藏的帖子数量
     */
    int countByUserId(@Param("userId") Long userId);

    /**
     * 批量插入收藏记录
     *
     * @param collects 收藏对象列表
     * @return 插入记录数
     */
    int batchInsert(@Param("collects") List<Collect> collects);

    /**
     * 批量删除收藏记录
     *
     * @param userId  用户ID
     * @param postIds 帖子ID列表
     * @return 删除记录数
     */
    int batchDeleteByUserIdAndPostIds(@Param("userId") Long userId, @Param("postIds") List<Long> postIds);

    /**
     * 根据收藏夹ID删除收藏记录
     *
     * @param folderId 收藏夹ID
     * @return 删除记录数
     */
    int deleteByFolderId(@Param("folderId") Long folderId);

    /**
     * 根据用户ID和收藏夹ID查询收藏记录列表
     *
     * @param userId   用户ID
     * @param folderId 收藏夹ID
     * @return 收藏记录列表
     */
    List<Collect> findByUserIdAndFolderId(@Param("userId") Long userId, @Param("folderId") Long folderId);
}