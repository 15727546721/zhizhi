package cn.xu.domain.post.repository;

import cn.xu.domain.collect.model.entity.CollectFolderEntity;

import java.util.List;
import java.util.Optional;

/**
 * 收藏夹仓储接口（已废弃）
 * 
 * @deprecated 请使用 {@link cn.xu.domain.collect.repository.ICollectFolderRepository} 替代
 */
@Deprecated
public interface ICollectFolderRepository {

    /**
     * 保存收藏夹
     *
     * @param collectFolderEntity 收藏夹实体
     * @return 收藏夹ID
     */
    Long save(CollectFolderEntity collectFolderEntity);

    /**
     * 更新收藏夹
     *
     * @param collectFolderEntity 收藏夹实体
     */
    void update(CollectFolderEntity collectFolderEntity);

    /**
     * 根据ID删除收藏夹
     *
     * @param id 收藏夹ID
     */
    void deleteById(Long id);

    /**
     * 根据ID查询收藏夹
     *
     * @param id 收藏夹ID
     * @return 收藏夹实体
     */
    Optional<CollectFolderEntity> findById(Long id);

    /**
     * 根据用户ID查询收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    List<CollectFolderEntity> findByUserId(Long userId);

    /**
     * 根据用户ID和名称查询收藏夹
     *
     * @param userId 用户ID
     * @param name   收藏夹名称
     * @return 收藏夹实体
     */
    Optional<CollectFolderEntity> findByUserIdAndName(Long userId, String name);

    /**
     * 查询用户的默认收藏夹
     *
     * @param userId 用户ID
     * @return 默认收藏夹实体
     */
    Optional<CollectFolderEntity> findDefaultFolderByUserId(Long userId);

    /**
     * 更新收藏夹帖子数量
     *
     * @param folderId 收藏夹ID
     * @param count    帖子数量
     */
    void updatePostCount(Long folderId, Integer count);
}