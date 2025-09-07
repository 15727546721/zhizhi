package cn.xu.domain.article.service;

import cn.xu.domain.article.model.aggregate.ArticleAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.valobj.ArticleStatus;
import cn.xu.domain.article.repository.IArticleAggregateRepository;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * 文章管理领域服务
 * 负责文章删除、发布、状态管理等管理相关的业务逻辑，遵循DDD原则
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleManagementDomainService {

    private final IArticleAggregateRepository articleAggregateRepository;
    private final ArticleAggregateDomainService articleAggregateDomainService;

    /**
     * 发布文章（将草稿发布为正式文章）
     * @param articleEntity 文章实体
     * @param userId 用户ID
     */
    public void publishArticle(ArticleEntity articleEntity, Long userId) {
        if (articleEntity == null || articleEntity.getId() == null) {
            throw new IllegalArgumentException("文章信息不能为空");
        }
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 通过聚合根仓库查找文章
        ArticleAggregate aggregate = articleAggregateRepository.findById(articleEntity.getId())
                .orElseThrow(() -> new BusinessException("文章不存在"));

        // 验证发布权限
        if (!articleAggregateDomainService.validatePublishPermission(aggregate.getArticleEntity(), userId)) {
            throw new BusinessException("文章作者ID不匹配");
        }

        // 检查文章是否已发布
        if (aggregate.isPublished()) {
            log.warn("文章已发布 - articleId: {}", articleEntity.getId());
            return;
        }

        // 使用聚合根的方法发布文章
        aggregate.publish();
        articleAggregateRepository.update(aggregate);
        
        log.info("文章发布成功 - articleId: {}", articleEntity.getId());
    }

    /**
     * 删除文章（用户删除自己的文章）
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    public void deleteUserArticle(Long articleId, Long userId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        if (userId == null) {
            throw new IllegalArgumentException("用户ID不能为空");
        }

        // 通过聚合根仓库查找文章
        ArticleAggregate aggregate = articleAggregateRepository.findById(articleId)
                .orElseGet(() -> {
                    log.warn("文章不存在 - articleId: {}", articleId);
                    return null;
                });
        
        if (aggregate == null) {
            return;
        }

        // 验证删除权限
        if (!articleAggregateDomainService.validateDeletePermission(aggregate.getArticleEntity(), userId)) {
            throw new BusinessException("文章作者ID不匹配");
        }

        // 执行删除
        articleAggregateRepository.deleteById(articleId);
        log.info("删除文章成功 - articleId: {}", articleId);
    }

    /**
     * 批量删除文章（管理员操作）
     * @param articleIds 文章ID列表
     */
    public void deleteArticlesByAdmin(List<Long> articleIds) {
        if (articleIds == null || articleIds.isEmpty()) {
            log.warn("删除文章：文章ID列表为空");
            return;
        }

        try {
            articleAggregateRepository.deleteByIds(articleIds);
            log.info("批量删除文章成功 - 删除数量: {}", articleIds.size());
        } catch (Exception e) {
            log.error("批量删除文章失败 - articleIds: {}", articleIds, e);
            throw new BusinessException("删除文章失败：" + e.getMessage());
        }
    }

    /**
     * 更新文章状态
     * @param articleId 文章ID
     * @param status 新状态
     */
    public void updateArticleStatus(Long articleId, ArticleStatus status) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        if (status == null) {
            throw new IllegalArgumentException("文章状态不能为空");
        }

        // 通过聚合根仓库查找文章
        ArticleAggregate aggregate = articleAggregateRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException("文章不存在"));

        // 使用聚合根的业务方法更新状态
        if (ArticleStatus.PUBLISHED.equals(status)) {
            aggregate.publish();
        } else if (ArticleStatus.DRAFT.equals(status)) {
            aggregate.withdraw();
        } else if (ArticleStatus.DELETED.equals(status)) {
            aggregate.delete();
        }
        
        articleAggregateRepository.update(aggregate);
        log.info("文章状态更新成功 - articleId: {}, status: {}", articleId, status);
    }

    /**
     * 验证文章是否存在
     * @param articleId 文章ID
     * @return 文章实体
     */
    public ArticleEntity validateArticleExists(Long articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }
        
        ArticleAggregate aggregate = articleAggregateRepository.findById(articleId)
                .orElseThrow(() -> new BusinessException("文章不存在"));
        
        return aggregate.getArticleEntity();
    }

    /**
     * 更新文章浏览数
     * @param articleId 文章ID
     */
    public void incrementViewCount(Long articleId) {
        if (articleId == null) {
            throw new IllegalArgumentException("文章ID不能为空");
        }

        try {
            // 通过聚合根仓库查找文章
            ArticleAggregate aggregate = articleAggregateRepository.findById(articleId)
                    .orElseGet(() -> {
                        log.warn("文章不存在 - articleId: {}", articleId);
                        return null;
                    });
            
            if (aggregate == null) {
                return;
            }

            // 使用聚合服务更新浏览数
            aggregate.incrementViewCount();
            articleAggregateRepository.update(aggregate);
            
            log.info("文章浏览数更新成功 - articleId: {}, viewCount: {}", 
                    articleId, aggregate.getArticleEntity().getViewCount());
        } catch (Exception e) {
            log.error("文章浏览数更新失败 - articleId: {}", articleId, e);
            throw new BusinessException("文章浏览失败：" + e.getMessage());
        }
    }

    /**
     * 更新文章
     * @param articleEntity 文章实体
     */
    public void updateArticle(ArticleEntity articleEntity) {
        if (articleEntity == null || articleEntity.getId() == null) {
            throw new IllegalArgumentException("文章信息不能为空");
        }

        try {
            // 通过聚合根仓库查找文章
            ArticleAggregate aggregate = articleAggregateRepository.findById(articleEntity.getId())
                    .orElseThrow(() -> new BusinessException("文章不存在"));

            // 验证作者身份
            if (!Objects.equals(aggregate.getArticleEntity().getUserId(), articleEntity.getUserId())) {
                throw new BusinessException("无权修改其他用户的文章");
            }

            // 更新文章内容
            aggregate.updateArticle(articleEntity);
            articleAggregateRepository.update(aggregate);
            log.info("更新文章成功 - articleId: {}", articleEntity.getId());
        } catch (Exception e) {
            log.error("更新文章失败 - articleId: {}", articleEntity.getId(), e);
            throw new BusinessException("更新文章失败：" + e.getMessage());
        }
    }
}