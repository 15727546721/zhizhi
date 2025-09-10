package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import cn.xu.domain.article.model.entity.CollectFolderEntity;
import cn.xu.infrastructure.common.exception.BusinessException;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文章收藏编排领域服务
 * 统一协调文章收藏和收藏夹功能，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCollectionOrchestrationDomainService implements IArticleCollectionOrchestrationService {

    @Resource
    private IArticleCollectDomainService articleCollectDomainService;

    @Resource
    private ICollectFolderDomainService collectFolderDomainService;

    /**
     * 收藏文章到默认收藏夹
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 文章收藏实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleCollectEntity collectArticleToDefaultFolder(Long userId, Long articleId) {
        log.info("开始收藏文章到默认收藏夹 - userId: {}, articleId: {}", userId, articleId);

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);
            ArticleCollectEntity.validateArticleId(articleId);

            // 收藏文章（全局收藏）
            ArticleCollectEntity articleCollectEntity = articleCollectDomainService.collectArticle(userId, articleId);

            // 获取用户默认收藏夹
            CollectFolderEntity defaultFolder = collectFolderDomainService.getUserDefaultFolder(userId);

            // 收藏文章到默认收藏夹
            try {
                collectFolderDomainService.collectArticleToFolder(defaultFolder.getId(), articleId, userId);
            } catch (BusinessException e) {
                // 如果已收藏到该收藏夹，忽略异常
                if (!"文章已收藏到该收藏夹".equals(e.getMessage())) {
                    throw e;
                }
                log.info("文章已收藏到默认收藏夹 - userId: {}, articleId: {}, folderId: {}", userId, articleId, defaultFolder.getId());
            }

            log.info("收藏文章到默认收藏夹成功 - userId: {}, articleId: {}", userId, articleId);
            return articleCollectEntity;
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleCollectEntity collectArticleToFolder(Long userId, Long articleId, Long folderId) {
        log.info("开始收藏文章到指定收藏夹 - userId: {}, articleId: {}, folderId: {}", userId, articleId, folderId);

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);
            ArticleCollectEntity.validateArticleId(articleId);
            ArticleCollectEntity.validateFolderId(folderId);

            // 收藏文章（全局收藏）
            ArticleCollectEntity articleCollectEntity = articleCollectDomainService.collectArticle(userId, articleId);

            // 收藏文章到指定收藏夹
            try {
                collectFolderDomainService.collectArticleToFolder(folderId, articleId, userId);
            } catch (BusinessException e) {
                // 如果已收藏到该收藏夹，忽略异常
                if (!"文章已收藏到该收藏夹".equals(e.getMessage())) {
                    throw e;
                }
                log.info("文章已收藏到指定收藏夹 - userId: {}, articleId: {}, folderId: {}", userId, articleId, folderId);
            }

            log.info("收藏文章到指定收藏夹成功 - userId: {}, articleId: {}, folderId: {}", userId, articleId, folderId);
            return articleCollectEntity;
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long userId, Long articleId) {
        log.info("开始取消收藏文章 - userId: {}, articleId: {}", userId, articleId);

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);
            ArticleCollectEntity.validateArticleId(articleId);

            // 取消全局收藏
            articleCollectDomainService.uncollectArticle(userId, articleId);

            // 从所有收藏夹中取消收藏该文章
            collectFolderDomainService.uncollectArticles(userId, Arrays.asList(articleId));

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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int collectArticlesToDefaultFolder(Long userId, List<Long> articleIds) {
        log.info("开始批量收藏文章到默认收藏夹 - userId: {}, articleCount: {}", userId, articleIds.size());

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);
            if (articleIds == null || articleIds.isEmpty()) {
                return 0;
            }

            // 获取用户默认收藏夹
            CollectFolderEntity defaultFolder = collectFolderDomainService.getUserDefaultFolder(userId);

            // 批量收藏文章（全局收藏）
            int globalSuccessCount = articleCollectDomainService.collectArticles(userId, articleIds);

            // 批量收藏文章到默认收藏夹
            int folderSuccessCount = collectFolderDomainService.collectArticlesToFolder(
                    defaultFolder.getId(), articleIds, userId);

            log.info("批量收藏文章到默认收藏夹完成 - userId: {}, globalSuccessCount: {}, folderSuccessCount: {}",
                    userId, globalSuccessCount, folderSuccessCount);
            return Math.max(globalSuccessCount, folderSuccessCount);
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int collectArticlesToFolder(Long userId, List<Long> articleIds, Long folderId) {
        log.info("开始批量收藏文章到指定收藏夹 - userId: {}, articleCount: {}, folderId: {}", userId, articleIds.size(), folderId);

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);
            ArticleCollectEntity.validateFolderId(folderId);
            if (articleIds == null || articleIds.isEmpty()) {
                return 0;
            }

            // 批量收藏文章（全局收藏）
            int globalSuccessCount = articleCollectDomainService.collectArticles(userId, articleIds);

            // 批量收藏文章到指定收藏夹
            int folderSuccessCount = collectFolderDomainService.collectArticlesToFolder(
                    folderId, articleIds, userId);

            log.info("批量收藏文章到指定收藏夹完成 - userId: {}, folderId: {}, globalSuccessCount: {}, folderSuccessCount: {}",
                    userId, folderId, globalSuccessCount, folderSuccessCount);
            return Math.max(globalSuccessCount, folderSuccessCount);
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
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int uncollectArticles(Long userId, List<Long> articleIds) {
        log.info("开始批量取消收藏文章 - userId: {}, articleCount: {}", userId, articleIds.size());

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);
            if (articleIds == null || articleIds.isEmpty()) {
                return 0;
            }

            // 批量取消全局收藏
            int globalSuccessCount = articleCollectDomainService.uncollectArticles(userId, articleIds);

            // 从所有收藏夹中批量取消收藏文章
            int folderSuccessCount = collectFolderDomainService.uncollectArticles(userId, articleIds);

            log.info("批量取消收藏文章完成 - userId: {}, globalSuccessCount: {}, folderSuccessCount: {}",
                    userId, globalSuccessCount, folderSuccessCount);
            return Math.max(globalSuccessCount, folderSuccessCount);
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
    @Override
    public CollectFolderEntity getUserDefaultFolder(Long userId) {
        log.info("开始获取用户默认收藏夹 - userId: {}", userId);

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);

            CollectFolderEntity defaultFolder = collectFolderDomainService.getUserDefaultFolder(userId);

            log.info("获取用户默认收藏夹成功 - userId: {}, folderId: {}", userId, defaultFolder.getId());
            return defaultFolder;
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
    @Override
    public List<CollectFolderEntity> getUserFolders(Long userId) {
        log.info("开始获取用户收藏夹列表 - userId: {}", userId);

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);

            List<CollectFolderEntity> folders = collectFolderDomainService.getUserFolders(userId);

            log.info("获取用户收藏夹列表成功 - userId: {}, folderCount: {}", userId, folders.size());
            return folders;
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
    @Override
    public boolean isArticleCollected(Long userId, Long articleId) {
        log.info("开始检查文章收藏状态 - userId: {}, articleId: {}", userId, articleId);

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);
            ArticleCollectEntity.validateArticleId(articleId);

            // 检查文章是否已收藏
            boolean isCollected = articleCollectDomainService.isArticleCollected(userId, articleId);

            log.info("检查文章收藏状态成功 - userId: {}, articleId: {}, isCollected: {}", userId, articleId, isCollected);
            return isCollected;
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
    @Override
    public List<Long> getCollectArticleIds(Long userId) {
        log.info("开始获取用户收藏文章ID列表 - userId: {}", userId);

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);

            // 获取用户收藏的文章ID列表
            List<Long> articleIds = articleCollectDomainService.getCollectArticleIds(userId);

            log.info("获取用户收藏文章ID列表成功 - userId: {}, articleCount: {}", userId, articleIds.size());
            return articleIds;
        } catch (Exception e) {
            log.error("获取用户收藏文章ID列表失败 - userId: {}", userId, e);
            throw new BusinessException("获取收藏文章ID列表失败: " + e.getMessage());
        }
    }
}