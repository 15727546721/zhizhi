package cn.xu.domain.post.repository;

import cn.xu.domain.collect.model.entity.CollectEntity;

import java.util.List;
import java.util.Optional;

/**
 * 收藏夹帖子关联仓储接口（已废弃）
 * 
 * @deprecated 请使用 {@link cn.xu.domain.collect.repository.ICollectRepository} 替代
 */
@Deprecated
public interface ICollectFolderPostRepository {

    /**
     * 保存收藏夹帖子关联记录
     *
     * @param collectEntity 收藏夹帖子关联实体
     * @return 记录ID
     */
    Long save(CollectEntity collectEntity);

    /**
     * 根据收藏夹ID和帖子ID删除关联记录
     *
     * @param folderId 收藏夹ID
     * @param postId   帖子ID
     */
    void deleteByFolderIdAndPostId(Long folderId, Long postId);

    /**
     * 根据收藏夹ID删除关联记录
     *
     * @param folderId 收藏夹ID
     */
    void deleteByFolderId(Long folderId);

    /**
     * 根据帖子ID删除关联记录
     *
     * @param postId 帖子ID
     */
    void deleteByPostId(Long postId);

    /**
     * 根据收藏夹ID查询关联记录列表
     *
     * @param folderId 收藏夹ID
     * @return 关联记录列表
     */
    List<CollectEntity> findByFolderId(Long folderId);

    /**
     * 根据用户ID和帖子ID查询关联记录
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 关联记录列表
     */
    List<CollectEntity> findByUserIdAndPostId(Long userId, Long postId);

    /**
     * 根据收藏夹ID和帖子ID查询关联记录
     *
     * @param folderId 收藏夹ID
     * @param postId   帖子ID
     * @return 关联记录对象
     */
    Optional<CollectEntity> findByFolderIdAndPostId(Long folderId, Long postId);

    /**
     * 检查帖子是否已被收藏到指定收藏夹
     *
     * @param folderId 收藏夹ID
     * @param postId   帖子ID
     * @return 是否已收藏
     */
    boolean existsByFolderIdAndPostId(Long folderId, Long postId);

    /**
     * 统计收藏夹中的帖子数量
     *
     * @param folderId 收藏夹ID
     * @return 帖子数量
     */
    int countByFolderId(Long folderId);
}