package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.CollectFolderPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CollectFolderMapper {
    /**
     * 插入收藏夹
     *
     * @param collectFolderPO 收藏夹对象
     * @return 插入记录数
     */
    int insert(CollectFolderPO collectFolderPO);

    /**
     * 根据ID更新收藏夹
     *
     * @param collectFolderPO 收藏夹对象
     * @return 更新记录数
     */
    int update(CollectFolderPO collectFolderPO);

    /**
     * 根据ID删除收藏夹
     *
     * @param id 收藏夹ID
     * @return 删除记录数
     */
    int deleteById(@Param("id") Long id);

    /**
     * 根据ID查询收藏夹
     *
     * @param id 收藏夹ID
     * @return 收藏夹对象
     */
    CollectFolderPO selectById(@Param("id") Long id);

    /**
     * 根据用户ID查询收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    List<CollectFolderPO> selectByUserId(@Param("userId") Long userId);

    /**
     * 根据用户ID和名称查询收藏夹
     *
     * @param userId 用户ID
     * @param name   收藏夹名称
     * @return 收藏夹对象
     */
    CollectFolderPO selectByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

    /**
     * 查询用户的默认收藏夹
     *
     * @param userId 用户ID
     * @return 默认收藏夹对象
     */
    CollectFolderPO selectDefaultFolderByUserId(@Param("userId") Long userId);

    /**
     * 更新收藏夹帖子数量
     *
     * @param folderId 收藏夹ID
     * @param count    帖子数量
     * @return 更新记录数
     */
    int updatePostCount(@Param("folderId") Long folderId, @Param("count") Integer count);
}