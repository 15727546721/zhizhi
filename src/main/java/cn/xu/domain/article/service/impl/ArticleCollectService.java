package cn.xu.domain.article.service.impl;

import cn.xu.domain.article.model.entity.ArticleCollectEntity;
import cn.xu.domain.article.repository.IArticleCollectRepository;
import cn.xu.domain.article.service.IArticleCollectService;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章收藏领域服务实现
 * 处理文章收藏相关的业务逻辑
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleCollectService implements IArticleCollectService {

    @Resource
    private IArticleCollectRepository articleCollectRepository;

    @Override
    public boolean checkStatus(Long currentUserId, Long articleId) {
        try {
            // 查询用户是否收藏了该文章
            ArticleCollectEntity articleCollectEntity = articleCollectRepository.findByUserIdAndArticleId(currentUserId, articleId);

            // 如果查询结果不为空且 status == 1 (已收藏)，则返回 true
            return articleCollectEntity != null && articleCollectEntity.getStatus() == 1;
        } catch (Exception e) {
            log.error("检查文章收藏状态失败 - userId: {}, articleId: {}", currentUserId, articleId, e);
            throw new BusinessException("检查收藏状态失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ArticleCollectEntity collectArticle(Long userId, Long articleId) {
        log.info("开始收藏文章 - userId: {}, articleId: {}", userId, articleId);

        try {
            // 查询是否已存在收藏记录
            ArticleCollectEntity existingEntity = articleCollectRepository.findByUserIdAndArticleId(userId, articleId);

            if (existingEntity != null) {
                // 如果已存在且已收藏，直接返回
                if (existingEntity.isCollected()) {
                    return existingEntity;
                }
                // 如果已存在但未收藏，更新状态
                existingEntity.collect();
                articleCollectRepository.update(existingEntity);
                return existingEntity;
            } else {
                // 如果不存在，创建新的收藏记录
                ArticleCollectEntity newEntity = ArticleCollectEntity.create(userId, articleId);
                articleCollectRepository.save(newEntity);
                return newEntity;
            }
        } catch (Exception e) {
            log.error("收藏文章失败 - userId: {}, articleId: {}", userId, articleId, e);
            throw new BusinessException("收藏文章失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void uncollectArticle(Long userId, Long articleId) {
        log.info("开始取消收藏文章 - userId: {}, articleId: {}", userId, articleId);

        try {
            // 查询是否已存在收藏记录
            ArticleCollectEntity existingEntity = articleCollectRepository.findByUserIdAndArticleId(userId, articleId);

            if (existingEntity != null && existingEntity.isCollected()) {
                // 如果已收藏，取消收藏
                existingEntity.uncollect();
                articleCollectRepository.update(existingEntity);
            }
            // 如果未收藏或不存在记录，则无需操作
        } catch (Exception e) {
            log.error("取消收藏文章失败 - userId: {}, articleId: {}", userId, articleId, e);
            throw new BusinessException("取消收藏文章失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int collectArticles(Long userId, List<Long> articleIds) {
        log.info("开始批量收藏文章 - userId: {}, articleCount: {}", userId, articleIds.size());

        if (articleIds == null || articleIds.isEmpty()) {
            return 0;
        }

        try {
            int successCount = 0;
            
            // 过滤掉已收藏的文章
            List<Long> notCollectedArticleIds = new ArrayList<>();
            for (Long articleId : articleIds) {
                if (!checkStatus(userId, articleId)) {
                    notCollectedArticleIds.add(articleId);
                }
            }

            if (notCollectedArticleIds.isEmpty()) {
                return 0;
            }

            // 创建收藏实体列表
            List<ArticleCollectEntity> collectEntities = notCollectedArticleIds.stream()
                    .map(articleId -> ArticleCollectEntity.create(userId, articleId))
                    .collect(Collectors.toList());

            // 批量保存
            successCount = articleCollectRepository.batchSave(collectEntities);

            log.info("批量收藏文章成功 - userId: {}, successCount: {}", userId, successCount);
            return successCount;
        } catch (Exception e) {
            log.error("批量收藏文章失败 - userId: {}, articleCount: {}", userId, articleIds.size(), e);
            throw new BusinessException("批量收藏文章失败: " + e.getMessage());
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int uncollectArticles(Long userId, List<Long> articleIds) {
        log.info("开始批量取消收藏文章 - userId: {}, articleCount: {}", userId, articleIds.size());

        if (articleIds == null || articleIds.isEmpty()) {
            return 0;
        }

        try {
            // 批量删除
            int successCount = articleCollectRepository.batchDeleteByUserIdAndArticleIds(userId, articleIds);

            log.info("批量取消收藏文章成功 - userId: {}, successCount: {}", userId, successCount);
            return successCount;
        } catch (Exception e) {
            log.error("批量取消收藏文章失败 - userId: {}, articleCount: {}", userId, articleIds.size(), e);
            throw new BusinessException("批量取消收藏文章失败: " + e.getMessage());
        }
    }

    @Override
    public int getCollectCount(Long userId) {
        log.info("开始获取用户收藏文章数量 - userId: {}", userId);

        try {
            int count = articleCollectRepository.countByUserId(userId);
            log.info("获取用户收藏文章数量成功 - userId: {}, count: {}", userId, count);
            return count;
        } catch (Exception e) {
            log.error("获取用户收藏文章数量失败 - userId: {}", userId, e);
            throw new BusinessException("获取收藏文章数量失败: " + e.getMessage());
        }
    }

    @Override
    public List<Long> getCollectArticleIds(Long userId) {
        log.info("开始获取用户收藏文章ID列表 - userId: {}", userId);

        try {
            List<Long> articleIds = articleCollectRepository.findArticleIdsByUserId(userId);
            log.info("获取用户收藏文章ID列表成功 - userId: {}, count: {}", userId, articleIds.size());
            return articleIds;
        } catch (Exception e) {
            log.error("获取用户收藏文章ID列表失败 - userId: {}", userId, e);
            throw new BusinessException("获取收藏文章ID列表失败: " + e.getMessage());
        }
    }
}