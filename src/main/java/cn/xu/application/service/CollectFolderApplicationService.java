package cn.xu.application.service;

import cn.xu.domain.article.model.entity.CollectFolderArticleEntity;
import cn.xu.domain.article.model.entity.CollectFolderEntity;
import cn.xu.domain.article.service.ICollectFolderDomainService;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 收藏夹应用服务
 * 协调领域服务处理应用层逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CollectFolderApplicationService {

    @Resource
    private ICollectFolderDomainService collectFolderDomainService;

    /**
     * 创建收藏夹
     *
     * @param userId      用户ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @param isPublic    是否公开
     * @return 收藏夹ID
     */
    public Long createFolder(Long userId, String name, String description, Boolean isPublic) {
        return collectFolderDomainService.createFolder(userId, name, description, isPublic);
    }

    /**
     * 更新收藏夹信息
     *
     * @param folderId    收藏夹ID
     * @param userId      用户ID
     * @param name        收藏夹名称
     * @param description 收藏夹描述
     * @param isPublic    是否公开
     */
    public void updateFolder(Long folderId, Long userId, String name, String description, Boolean isPublic) {
        collectFolderDomainService.updateFolder(folderId, userId, name, description, isPublic);
    }

    /**
     * 删除收藏夹
     *
     * @param folderId 收藏夹ID
     * @param userId   用户ID
     */
    public void deleteFolder(Long folderId, Long userId) {
        collectFolderDomainService.deleteFolder(folderId, userId);
    }

    /**
     * 获取用户的收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    public List<CollectFolderEntity> getUserFolders(Long userId) {
        return collectFolderDomainService.getUserFolders(userId);
    }

    /**
     * 获取用户的默认收藏夹
     *
     * @param userId 用户ID
     * @return 默认收藏夹
     */
    public CollectFolderEntity getUserDefaultFolder(Long userId) {
        return collectFolderDomainService.getUserDefaultFolder(userId);
    }

    /**
     * 收藏文章到收藏夹
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @param userId    用户ID
     */
    public void collectArticleToFolder(Long folderId, Long articleId, Long userId) {
        collectFolderDomainService.collectArticleToFolder(folderId, articleId, userId);
    }

    /**
     * 从收藏夹取消收藏文章
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @param userId    用户ID
     */
    public void uncollectArticleFromFolder(Long folderId, Long articleId, Long userId) {
        collectFolderDomainService.uncollectArticleFromFolder(folderId, articleId, userId);
    }

    /**
     * 检查文章是否已收藏到指定收藏夹
     *
     * @param folderId  收藏夹ID
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 是否已收藏
     */
    public boolean isArticleCollectedToFolder(Long folderId, Long articleId, Long userId) {
        return collectFolderDomainService.isArticleCollectedToFolder(folderId, articleId, userId);
    }

    /**
     * 获取收藏夹中的文章列表
     *
     * @param folderId 收藏夹ID
     * @param userId   用户ID
     * @return 文章关联记录列表
     */
    public List<CollectFolderArticleEntity> getFolderArticles(Long folderId, Long userId) {
        return collectFolderDomainService.getFolderArticles(folderId, userId);
    }
}