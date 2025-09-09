package cn.xu.application.service;

import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import cn.xu.domain.article.model.entity.CollectFolderEntity;
import cn.xu.domain.article.service.IArticleCollectionOrchestrationService;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文章收藏应用服务
 * 对外提供统一的文章收藏功能接口，协调领域服务处理应用层逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCollectionApplicationService {

    @Resource
    private IArticleCollectionOrchestrationService articleCollectionOrchestrationService;

    /**
     * 收藏文章到默认收藏夹
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 文章收藏实体
     */
    public ArticleCollectEntity collectArticleToDefaultFolder(Long userId, Long articleId) {
        log.info("开始收藏文章到默认收藏夹 - userId: {}, articleId: {}", userId, articleId);

        try {
            ArticleCollectEntity result = articleCollectionOrchestrationService.collectArticleToDefaultFolder(userId, articleId);
            log.info("收藏文章到默认收藏夹成功 - userId: {}, articleId: {}", userId, articleId);
            return result;
        } catch (Exception e) {
            log.error("收藏文章到默认收藏夹失败 - userId: {}, articleId: {}", userId, articleId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("收藏文章到默认收藏夹失败: " + e.getMessage());
            }
        }
    }

    /**
     * 收藏文章到指定收藏夹
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @param folderId  收藏夹ID
     * @return 文章收藏实体
     */
    public ArticleCollectEntity collectArticleToFolder(Long userId, Long articleId, Long folderId) {
        log.info("开始收藏文章到指定收藏夹 - userId: {}, articleId: {}, folderId: {}", userId, articleId, folderId);

        try {
            ArticleCollectEntity result = articleCollectionOrchestrationService.collectArticleToFolder(userId, articleId, folderId);
            log.info("收藏文章到指定收藏夹成功 - userId: {}, articleId: {}, folderId: {}", userId, articleId, folderId);
            return result;
        } catch (Exception e) {
            log.error("收藏文章到指定收藏夹失败 - userId: {}, articleId: {}, folderId: {}", userId, articleId, folderId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("收藏文章到指定收藏夹失败: " + e.getMessage());
            }
        }
    }

    /**
     * 取消收藏文章
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     */
    public void uncollectArticle(Long userId, Long articleId) {
        log.info("开始取消收藏文章 - userId: {}, articleId: {}", userId, articleId);

        try {
            articleCollectionOrchestrationService.uncollectArticle(userId, articleId);
            log.info("取消收藏文章成功 - userId: {}, articleId: {}", userId, articleId);
        } catch (Exception e) {
            log.error("取消收藏文章失败 - userId: {}, articleId: {}", userId, articleId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("取消收藏文章失败: " + e.getMessage());
            }
        }
    }

    /**
     * 批量收藏文章到默认收藏夹
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 成功收藏的文章数量
     */
    public int collectArticlesToDefaultFolder(Long userId, List<Long> articleIds) {
        log.info("开始批量收藏文章到默认收藏夹 - userId: {}, articleCount: {}", userId, articleIds.size());

        try {
            int result = articleCollectionOrchestrationService.collectArticlesToDefaultFolder(userId, articleIds);
            log.info("批量收藏文章到默认收藏夹完成 - userId: {}, successCount: {}", userId, result);
            return result;
        } catch (Exception e) {
            log.error("批量收藏文章到默认收藏夹失败 - userId: {}, articleCount: {}", userId, articleIds.size(), e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("批量收藏文章到默认收藏夹失败: " + e.getMessage());
            }
        }
    }

    /**
     * 批量收藏文章到指定收藏夹
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @param folderId   收藏夹ID
     * @return 成功收藏的文章数量
     */
    public int collectArticlesToFolder(Long userId, List<Long> articleIds, Long folderId) {
        log.info("开始批量收藏文章到指定收藏夹 - userId: {}, articleCount: {}, folderId: {}", userId, articleIds.size(), folderId);

        try {
            int result = articleCollectionOrchestrationService.collectArticlesToFolder(userId, articleIds, folderId);
            log.info("批量收藏文章到指定收藏夹完成 - userId: {}, folderId: {}, successCount: {}", userId, folderId, result);
            return result;
        } catch (Exception e) {
            log.error("批量收藏文章到指定收藏夹失败 - userId: {}, articleCount: {}, folderId: {}", userId, articleIds.size(), folderId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("批量收藏文章到指定收藏夹失败: " + e.getMessage());
            }
        }
    }

    /**
     * 批量取消收藏文章
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 成功取消收藏的文章数量
     */
    public int uncollectArticles(Long userId, List<Long> articleIds) {
        log.info("开始批量取消收藏文章 - userId: {}, articleCount: {}", userId, articleIds.size());

        try {
            int result = articleCollectionOrchestrationService.uncollectArticles(userId, articleIds);
            log.info("批量取消收藏文章完成 - userId: {}, successCount: {}", userId, result);
            return result;
        } catch (Exception e) {
            log.error("批量取消收藏文章失败 - userId: {}, articleCount: {}", userId, articleIds.size(), e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("批量取消收藏文章失败: " + e.getMessage());
            }
        }
    }

    /**
     * 获取用户默认收藏夹
     *
     * @param userId 用户ID
     * @return 默认收藏夹
     */
    public CollectFolderEntity getUserDefaultFolder(Long userId) {
        log.info("开始获取用户默认收藏夹 - userId: {}", userId);

        try {
            CollectFolderEntity result = articleCollectionOrchestrationService.getUserDefaultFolder(userId);
            log.info("获取用户默认收藏夹成功 - userId: {}, folderId: {}", userId, result.getId());
            return result;
        } catch (Exception e) {
            log.error("获取用户默认收藏夹失败 - userId: {}", userId, e);
            throw new BusinessException("获取默认收藏夹失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户的收藏夹列表
     *
     * @param userId 用户ID
     * @return 收藏夹列表
     */
    public List<CollectFolderEntity> getUserFolders(Long userId) {
        log.info("开始获取用户收藏夹列表 - userId: {}", userId);

        try {
            List<CollectFolderEntity> result = articleCollectionOrchestrationService.getUserFolders(userId);
            log.info("获取用户收藏夹列表成功 - userId: {}, folderCount: {}", userId, result.size());
            return result;
        } catch (Exception e) {
            log.error("获取用户收藏夹列表失败 - userId: {}", userId, e);
            throw new BusinessException("获取收藏夹列表失败: " + e.getMessage());
        }
    }

    /**
     * 检查文章是否已收藏
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 是否已收藏
     */
    public boolean isArticleCollected(Long userId, Long articleId) {
        log.info("开始检查文章收藏状态 - userId: {}, articleId: {}", userId, articleId);

        try {
            boolean result = articleCollectionOrchestrationService.isArticleCollected(userId, articleId);
            log.info("检查文章收藏状态成功 - userId: {}, articleId: {}, isCollected: {}", userId, articleId, result);
            return result;
        } catch (Exception e) {
            log.error("检查文章收藏状态失败 - userId: {}, articleId: {}", userId, articleId, e);
            throw new BusinessException("检查收藏状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏的文章ID列表
     *
     * @param userId 用户ID
     * @return 收藏的文章ID列表
     */
    public List<Long> getCollectArticleIds(Long userId) {
        log.info("开始获取用户收藏文章ID列表 - userId: {}", userId);

        try {
            List<Long> result = articleCollectionOrchestrationService.getCollectArticleIds(userId);
            log.info("获取用户收藏文章ID列表成功 - userId: {}, articleCount: {}", userId, result.size());
            return result;
        } catch (Exception e) {
            log.error("获取用户收藏文章ID列表失败 - userId: {}", userId, e);
            throw new BusinessException("获取收藏文章ID列表失败: " + e.getMessage());
        }
    }
}