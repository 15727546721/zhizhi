package cn.xu.domain.post.repository;

import cn.xu.domain.collect.model.entity.CollectEntity;

import java.util.List;

/**
 * 用户帖子收藏仓储接口（已废弃）
 * 
 * @deprecated 请使用 {@link cn.xu.domain.collect.repository.ICollectRepository} 替代
 */
@Deprecated
public interface ICollectRepository {
    /**
     * 根据用户ID和帖子ID查询收藏记录
     */
    CollectEntity findByUserIdAndPostId(Long userId, Long postId);

    /**
     * 保存收藏记录
     */
    Long save(CollectEntity collectEntity);

    /**
     * 更新收藏记录
     */
    void update(CollectEntity collectEntity);

    /**
     * 根据用户ID和帖子ID删除收藏记录
     */
    void deleteByUserIdAndPostId(Long userId, Long postId);

    /**
     * 根据用户ID查询收藏的帖子ID列表
     */
    List<Long> findPostIdsByUserId(Long userId);

    /**
     * 统计用户收藏的帖子数量
     */
    int countByUserId(Long userId);

    /**
     * 批量保存收藏记录
     */
    int batchSave(List<CollectEntity> collectEntities);

    /**
     * 批量删除收藏记录
     */
    int batchDeleteByUserIdAndPostIds(Long userId, List<Long> postIds);

    /**
     * 根据用户ID和收藏夹ID查询收藏记录列表
     */
    List<CollectEntity> findByUserIdAndFolderId(Long userId, Long folderId);
}