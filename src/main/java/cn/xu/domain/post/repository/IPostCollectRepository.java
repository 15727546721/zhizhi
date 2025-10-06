package cn.xu.domain.post.repository;

import cn.xu.domain.collect.model.entity.CollectEntity;

import java.util.List;

/**
 * 帖子收藏仓储接口（已废弃）
 * 
 * @deprecated 请使用 {@link cn.xu.domain.collect.repository.ICollectRepository} 替代
 */
@Deprecated
public interface IPostCollectRepository {

    /**
     * 根据用户ID和帖子ID查询收藏记录
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 帖子收藏实体
     */
    CollectEntity findByUserIdAndPostId(Long userId, Long postId);

    /**
     * 保存帖子收藏记录
     *
     * @param collectEntity 帖子收藏实体
     * @return 收藏记录ID
     */
    Long save(CollectEntity collectEntity);

    /**
     * 更新帖子收藏记录
     *
     * @param collectEntity 帖子收藏实体
     */
    void update(CollectEntity collectEntity);

    /**
     * 根据用户ID和帖子ID删除收藏记录
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void deleteByUserIdAndPostId(Long userId, Long postId);

    /**
     * 根据用户ID查询收藏的帖子ID列表
     *
     * @param userId 用户ID
     * @return 收藏的帖子ID列表
     */
    List<Long> findPostIdsByUserId(Long userId);

    /**
     * 根据用户ID统计收藏的帖子数量
     *
     * @param userId 用户ID
     * @return 收藏的帖子数量
     */
    int countByUserId(Long userId);

    /**
     * 批量保存帖子收藏记录
     *
     * @param collectEntities 帖子收藏实体列表
     * @return 影响行数
     */
    int batchSave(List<CollectEntity> collectEntities);

    /**
     * 批量删除帖子收藏记录
     *
     * @param userId   用户ID
     * @param postIds 帖子ID列表
     * @return 影响行数
     */
    int batchDeleteByUserIdAndPostIds(Long userId, List<Long> postIds);
}