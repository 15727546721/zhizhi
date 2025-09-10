package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.CollectFolderArticleEntity;
import cn.xu.domain.article.model.entity.CollectFolderEntity;

import java.util.List;

/**
 * 收藏夹领域服务接口
 * 处理收藏夹相关的业务逻辑
 */
public interface ICollectFolderDomainService {
    /**
     * 创建收藏夹
     *
     * @param userId      用户ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @param isPublic    是否公开
     * @return 收藏夹ID
     */
    Long createFolder(Long userId, String name, String description, Boolean isPublic);

    /**
     * 更新收藏夹信息
     *
     * @param folderId    收藏夹ID
     * @param userId      用户ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @param isPublic    是否公开
     */
    void updateFolder(Long folderId, Long userId, String name, String description, Boolean isPublic);

    /**
     * 删除收藏夹
     *
     * @param folderId 收藏夹ID
     * @param userId   用户ID
     */
    void deleteFolder(Long folderId, Long userId);

    /**
     * 获取用户的收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    List<CollectFolderEntity> getUserFolders(Long userId);

    /**
     * 获取用户的默认收藏夹
     *
     * @param userId 用户ID
     * @return 默认收藏夹
     */
    CollectFolderEntity getUserDefaultFolder(Long userId);

    /**
     * 收藏文章到收藏夹
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @param userId    用户ID
     */
    void collectArticleToFolder(Long folderId, Long articleId, Long userId);

    /**
     * 从收藏夹取消收藏文章
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @param userId    用户ID
     */
    void uncollectArticleFromFolder(Long folderId, Long articleId, Long userId);

    /**
     * 检查文章是否已收藏到指定收藏夹
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 是否已收藏
     */
    boolean isArticleCollectedToFolder(Long folderId, Long articleId, Long userId);

    /**
     * 获取收藏夹中的文章列表
     *
     * @param folderId 收藏夹ID
     * @param userId   用户ID
     * @return 文章关联记录列表
     */
    List<CollectFolderArticleEntity> getFolderArticles(Long folderId, Long userId);

    /**
     * 批量收藏文章到收藏夹
     *
     * @param folderId   收藏夹ID
     * @param articleIds 文章ID列表
     * @param userId     用户ID
     * @return 成功收藏的文章数量
     */
    int collectArticlesToFolder(Long folderId, List<Long> articleIds, Long userId);

    /**
     * 批量取消收藏文章
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 成功取消收藏的文章数量
     */
    int uncollectArticles(Long userId, List<Long> articleIds);

    /**
     * 获取用户收藏的文章数量统计
     *
     * @param userId 用户ID
     * @return 各收藏夹的文章数量统计
     */
    List<CollectFolderEntity> getUserFolderStats(Long userId);
}