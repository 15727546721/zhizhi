package cn.xu.domain.post.service;

import cn.xu.domain.collect.model.entity.CollectEntity;
import cn.xu.domain.collect.model.entity.CollectFolderEntity;
import cn.xu.domain.collect.service.ICollectService;

import java.util.List;

/**
 * 帖子收藏服务接口
 * 
 * 已重构为依赖独立的collect领域服务
 * @see ICollectService
 */
public interface IPostCollectService extends ICollectService {
    // 帖子收藏服务现在继承自通用的收藏服务接口
    // 保留此接口是为了向后兼容，实际实现将委托给collect领域服务
    
    /**
     * 收藏帖子
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void collectPost(Long userId, Long postId);

    /**
     * 取消收藏帖子
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     */
    void uncollectPost(Long userId, Long postId);

    /**
     * 检查用户是否已收藏指定帖子
     *
     * @param userId 用户ID
     * @param postId 帖子ID
     * @return 是否已收藏
     */
    boolean isPostCollected(Long userId, Long postId);

    /**
     * 获取用户收藏的帖子ID列表
     *
     * @param userId 用户ID
     * @return 收藏的帖子ID列表
     */
    List<Long> getCollectedPostIds(Long userId);

    /**
     * 统计用户收藏的帖子数量
     *
     * @param userId 用户ID
     * @return 收藏的帖子数量
     */
    int countCollectedPosts(Long userId);

    /**
     * 创建收藏夹
     *
     * @param userId      用户ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @return 收藏夹ID
     */
    Long createFolder(Long userId, String name, String description);

    /**
     * 更新收藏夹
     *
     * @param folderId    收藏夹ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     */
    void updateFolder(Long folderId, String name, String description);

    /**
     * 删除收藏夹
     *
     * @param userId   用户ID
     * @param folderId 收藏夹ID
     */
    void deleteFolder(Long userId, Long folderId);

    /**
     * 获取用户的收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    List<CollectFolderEntity> getFoldersByUserId(Long userId);

    /**
     * 将帖子添加到收藏夹
     *
     * @param userId   用户ID
     * @param postId   帖子ID
     * @param folderId 收藏夹ID
     */
    void addPostToFolder(Long userId, Long postId, Long folderId);

    /**
     * 从收藏夹中移除帖子
     *
     * @param userId   用户ID
     * @param postId   帖子ID
     * @param folderId 收藏夹ID
     */
    void removePostFromFolder(Long userId, Long postId, Long folderId);

    /**
     * 获取收藏夹中的帖子列表
     *
     * @param userId   用户ID
     * @param folderId 收藏夹ID
     * @return 帖子列表
     */
    List<CollectEntity> getPostsInFolder(Long userId, Long folderId);
}