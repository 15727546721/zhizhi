package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.CollectFolderPO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 收藏夹DAO接口
 */
@Mapper
public interface ICollectFolderDao {
    /**
     * 插入收藏夹
     */
    void insert(CollectFolderPO folderPO);

    /**
     * 根据ID查询收藏夹
     */
    CollectFolderPO selectById(Long id);

    /**
     * 根据用户ID查询收藏夹列表
     */
    List<CollectFolderPO> selectByUserId(Long userId);

    /**
     * 根据ID删除收藏夹
     */
    void deleteById(Long id);

    /**
     * 更新收藏夹信息
     */
    void update(CollectFolderPO folderPO);
}