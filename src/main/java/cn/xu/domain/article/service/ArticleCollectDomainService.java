package cn.xu.domain.article.service;

import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import cn.xu.domain.article.repository.IArticleCollectRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.List;

/**
 * 文章收藏领域服务
 * 处理文章收藏相关的核心业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCollectDomainService implements IArticleCollectDomainService {

    @Resource
    private IArticleCollectRepository articleCollectRepository;

    /**
     * 收藏文章
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 文章收藏实体
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleCollectEntity collectArticle(Long userId, Long articleId) {
        log.info("开始收藏文章 - userId: {}, articleId: {}", userId, articleId);

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);
            ArticleCollectEntity.validateArticleId(articleId);

            // 查询是否已存在收藏记录
            ArticleCollectEntity existingEntity = articleCollectRepository.findByUserIdAndArticleId(userId, articleId);

            if (existingEntity != null) {
                // 如果已存在且已收藏，直接返回
                if (existingEntity.isCollected()) {
                    log.info("文章已收藏 - userId: {}, articleId: {}", userId, articleId);
                    return existingEntity;
                }
                // 如果已存在但未收藏，更新状态
                existingEntity.collect();
                articleCollectRepository.update(existingEntity);
                log.info("更新文章收藏状态成功 - userId: {}, articleId: {}", userId, articleId);
                return existingEntity;
            } else {
                // 检查用户收藏数量是否达到上限
                int collectCount = articleCollectRepository.countByUserId(userId);
                if (collectCount >= 10000) { // 假设最大收藏数量为10000
                    throw new BusinessException("已达到最大收藏数量限制");
                }
                
                // 如果不存在，创建新的收藏记录
                ArticleCollectEntity newEntity = ArticleCollectEntity.create(userId, articleId);
                articleCollectRepository.save(newEntity);
                log.info("创建文章收藏记录成功 - userId: {}, articleId: {}, collectId: {}", userId, articleId, newEntity.getId());
                return newEntity;
            }
        } catch (Exception e) {
            log.error("收藏文章失败 - userId: {}, articleId: {}", userId, articleId, e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("收藏文章失败: " + e.getMessage());
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

            // 查询是否已存在收藏记录
            ArticleCollectEntity existingEntity = articleCollectRepository.findByUserIdAndArticleId(userId, articleId);

            if (existingEntity != null && existingEntity.isCollected()) {
                // 如果已收藏，取消收藏
                existingEntity.uncollect();
                articleCollectRepository.update(existingEntity);
                log.info("取消文章收藏成功 - userId: {}, articleId: {}", userId, articleId);
            } else {
                log.info("文章未收藏或不存在 - userId: {}, articleId: {}", userId, articleId);
            }
            // 如果未收藏或不存在记录，则无需操作
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
     * 检查文章是否已收藏
     *
     * @param userId    用户ID
     * @param articleId 文章ID
     * @return 是否已收藏
     */
    @Override
    public boolean isArticleCollected(Long userId, Long articleId) {
        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);
            ArticleCollectEntity.validateArticleId(articleId);

            // 查询用户是否收藏了该文章
            ArticleCollectEntity articleCollectEntity = articleCollectRepository.findByUserIdAndArticleId(userId, articleId);
            return articleCollectEntity != null && articleCollectEntity.isCollected();
        } catch (Exception e) {
            log.error("检查文章收藏状态失败 - userId: {}, articleId: {}", userId, articleId, e);
            throw new BusinessException("检查收藏状态失败: " + e.getMessage());
        }
    }

    /**
     * 获取用户收藏的文章数量
     *
     * @param userId 用户ID
     * @return 收藏的文章数量
     */
    @Override
    public int getCollectCount(Long userId) {
        log.info("开始获取用户收藏文章数量 - userId: {}", userId);

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);

            int count = articleCollectRepository.countByUserId(userId);
            log.info("获取用户收藏文章数量成功 - userId: {}, count: {}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("获取用户收藏文章数量失败 - userId: {}", userId, e);
            throw new BusinessException("获取收藏文章数量失败: " + e.getMessage());
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

            List<Long> articleIds = articleCollectRepository.findArticleIdsByUserId(userId);
            log.info("获取用户收藏文章ID列表成功 - userId: {}, count: {}", userId, articleIds.size());
            return articleIds;
        } catch (Exception e) {
            log.error("获取用户收藏文章ID列表失败 - userId: {}", userId, e);
            throw new BusinessException("获取收藏文章ID列表失败: " + e.getMessage());
        }
    }

    /**
     * 批量收藏文章
     *
     * @param userId     用户ID
     * @param articleIds 文章ID列表
     * @return 成功收藏的文章数量
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public int collectArticles(Long userId, List<Long> articleIds) {
        log.info("开始批量收藏文章 - userId: {}, articleCount: {}", userId, articleIds.size());

        try {
            // 参数校验
            ArticleCollectEntity.validateUserId(userId);
            if (articleIds == null || articleIds.isEmpty()) {
                return 0;
            }

            // 检查用户收藏数量是否达到上限
            int currentCount = articleCollectRepository.countByUserId(userId);
            if (currentCount >= 10000) { // 假设最大收藏数量为10000
                throw new BusinessException("已达到最大收藏数量限制");
            }

            int successCount = 0;
            for (Long articleId : articleIds) {
                try {
                    ArticleCollectEntity.validateArticleId(articleId);
                    // 尝试收藏每篇文章
                    collectArticle(userId, articleId);
                    successCount++;
                } catch (BusinessException e) {
                    log.warn("收藏文章失败 - userId: {}, articleId: {}, reason: {}", userId, articleId, e.getMessage());
                    // 继续处理其他文章
                }
            }

            log.info("批量收藏文章完成 - userId: {}, successCount: {}", userId, successCount);
            return successCount;
        } catch (Exception e) {
            log.error("批量收藏文章失败 - userId: {}, articleCount: {}", userId, articleIds.size(), e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("批量收藏文章失败: " + e.getMessage());
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

            int successCount = 0;
            for (Long articleId : articleIds) {
                try {
                    ArticleCollectEntity.validateArticleId(articleId);
                    // 尝试取消收藏每篇文章
                    uncollectArticle(userId, articleId);
                    successCount++;
                } catch (BusinessException e) {
                    log.warn("取消收藏文章失败 - userId: {}, articleId: {}, reason: {}", userId, articleId, e.getMessage());
                    // 继续处理其他文章
                }
            }

            log.info("批量取消收藏文章完成 - userId: {}, successCount: {}", userId, successCount);
            return successCount;
        } catch (Exception e) {
            log.error("批量取消收藏文章失败 - userId: {}, articleCount: {}", userId, articleIds.size(), e);
            if (e instanceof BusinessException) {
                throw e;
            } else {
                throw new BusinessException("批量取消收藏文章失败: " + e.getMessage());
            }
        }
    }
}