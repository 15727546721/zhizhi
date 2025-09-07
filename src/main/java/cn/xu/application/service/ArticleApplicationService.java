package cn.xu.application.service;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleDetailVO;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.domain.article.event.ArticleEventPublisher;
import cn.xu.domain.article.model.aggregate.ArticleAggregate;
import cn.xu.domain.article.model.aggregate.ArticleAndAuthorAggregate;
import cn.xu.domain.article.repository.IArticleAggregateRepository;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.service.*;
import cn.xu.infrastructure.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文章应用服务
 * 负责业务流程编排、事务管理和跨领域协调，遵循DDD应用服务规范
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleApplicationService {

    private final IArticleAggregateRepository articleAggregateRepository;
    private final ArticleQueryDomainService articleQueryDomainService;
    private final ArticleManagementDomainService articleManagementDomainService;
    private final ArticleAggregateDomainService articleAggregateDomainService;
    private final ArticleCacheDomainService articleCacheDomainService;
    private final ArticleEventPublisher articleEventPublisher;
    private final IArticleTagService articleTagService;
    private final ArticleCreationDomainService articleCreationDomainService;

    /**
     * 创建文章
     * @param articleEntity 文章实体
     * @param tagIds 标签ID列表
     * @return 文章ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createArticle(ArticleEntity articleEntity, List<Long> tagIds) {
        log.info("[文章应用服务] 开始创建文章 - title: {}, userId: {}", 
                articleEntity.getTitle(), articleEntity.getUserId());

        // 创建文章聚合根
        ArticleAggregate articleAggregate = ArticleAggregate.builder()
                .articleEntity(articleEntity)
                .tagIds(tagIds)
                .build();
        
        // 执行业务规则验证
        articleAggregate.validateForCreation();
        
        // 发布文章（如果状态是已发布）
        if (articleEntity.isPublished()) {
            articleAggregate.publish();
        }

        // 保存聚合根
        Long articleId = articleAggregateRepository.save(articleAggregate);

        // 发布文章创建事件
        articleEventPublisher.publishCreated(articleEntity);

        // 更新热度缓存
        articleCacheDomainService.updateHotRank(articleId, 0.0);

        log.info("[文章应用服务] 文章创建成功 - articleId: {}", articleId);
        return articleId;
    }

    /**
     * 创建或更新文章草稿
     * @param articleEntity 文章实体
     * @param tagIds 标签ID列表
     * @return 文章ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createOrUpdateArticleDraft(ArticleEntity articleEntity, List<Long> tagIds) {
        log.info("[文章应用服务] 开始创建/更新文章草稿 - title: {}, userId: {}", 
                articleEntity.getTitle(), articleEntity.getUserId());

        // 创建或更新草稿
        Long articleId = articleCreationDomainService.createOrUpdateArticleDraft(articleEntity);

        // 更新文章标签关系
        if (tagIds != null && !tagIds.isEmpty()) {
            if (articleEntity.getId() != null) {
                // 更新标签
                articleTagService.updateArticleTag(articleId, tagIds);
            } else {
                // 新建标签
                articleTagService.saveArticleTag(articleId, tagIds);
            }
        }

        log.info("[文章应用服务] 文章草稿操作成功 - articleId: {}", articleId);
        return articleId;
    }

    /**
     * 上传文章封面
     * @param imageFile 图片文件
     * @return 图片URL
     */
    public String uploadCover(MultipartFile imageFile) {
        log.info("[文章应用服务] 开始上传文章封面");
        return articleCreationDomainService.uploadCover(imageFile);
    }

    /**
     * 分页查询文章列表
     * @param articleRequest 查询请求
     * @return 分页响应
     */
    public PageResponse<List<ArticlePageVO>> listArticle(ArticleRequest articleRequest) {
        log.info("[文章应用服务] 开始分页查询文章列表 - pageNo: {}, pageSize: {}", 
                articleRequest.getPageNo(), articleRequest.getPageSize());

        List<ArticlePageVO> articles = articleQueryDomainService.queryArticleByPage(articleRequest);
        
        return PageResponse.of(articleRequest.getPageNo(), articleRequest.getPageSize(), 
                (long) articles.size(), articles);
    }

    /**
     * 发布文章
     * @param articleEntity 文章实体
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void publishArticle(ArticleEntity articleEntity, Long userId) {
        log.info("[文章应用服务] 开始发布文章 - articleId: {}, userId: {}", 
                articleEntity.getId(), userId);

        // 发布文章
        articleManagementDomainService.publishArticle(articleEntity, userId);

        // 发布文章更新事件
        articleEventPublisher.publishUpdated(articleEntity);

        // 更新热度缓存
        articleCacheDomainService.updateHotRank(articleEntity.getId(), 0.0);

        log.info("[文章应用服务] 文章发布成功 - articleId: {}", articleEntity.getId());
    }

    /**
     * 删除文章
     * @param articleId 文章ID
     * @param userId 用户ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticle(Long articleId, Long userId) {
        log.info("[文章应用服务] 开始删除文章 - articleId: {}, userId: {}", articleId, userId);

        // 删除文章
        articleManagementDomainService.deleteUserArticle(articleId, userId);

        // 发布文章删除事件
        articleEventPublisher.publishDeleted(articleId);

        // 从热度缓存中移除
        articleCacheDomainService.removeFromHotRank(articleId);

        log.info("[文章应用服务] 文章删除成功 - articleId: {}", articleId);
    }

    /**
     * 批量删除文章（管理员）
     * @param articleIds 文章ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteArticlesByAdmin(List<Long> articleIds) {
        log.info("[文章应用服务] 管理员批量删除文章 - count: {}", 
                articleIds != null ? articleIds.size() : 0);

        // 批量删除文章
        articleManagementDomainService.deleteArticlesByAdmin(articleIds);

        // 批量发布删除事件
        if (articleIds != null) {
            for (Long articleId : articleIds) {
                articleEventPublisher.publishDeleted(articleId);
                articleCacheDomainService.removeFromHotRank(articleId);
            }
        }

        log.info("[文章应用服务] 管理员批量删除文章成功");
    }

    /**
     * 更新文章
     * @param articleEntity 文章实体
     * @param tagIds 标签ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateArticle(ArticleEntity articleEntity, List<Long> tagIds) {
        log.info("[文章应用服务] 开始更新文章 - articleId: {}", articleEntity.getId());

        // 更新文章
        articleManagementDomainService.updateArticle(articleEntity);

        // 更新文章标签关系
        if (tagIds != null) {
            articleTagService.updateArticleTag(articleEntity.getId(), tagIds);
        }

        // 发布文章更新事件
        articleEventPublisher.publishUpdated(articleEntity);

        log.info("[文章应用服务] 文章更新成功 - articleId: {}", articleEntity.getId());
    }

    /**
     * 浏览文章（增加浏览数）
     * @param articleId 文章ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void viewArticle(Long articleId) {
        log.info("[文章应用服务] 文章浏览 - articleId: {}", articleId);

        // 增加浏览数
        articleManagementDomainService.incrementViewCount(articleId);

        log.info("[文章应用服务] 文章浏览成功 - articleId: {}", articleId);
    }

    /**
     * 获取文章详情
     * @param articleId 文章ID
     * @param currentUserId 当前用户ID
     * @return 文章详情VO
     */
    public ArticleDetailVO getArticleDetail(Long articleId, Long currentUserId) {
        log.info("[文章应用服务] 获取文章详情 - articleId: {}, userId: {}", articleId, currentUserId);

        // 查询文章
        ArticleEntity article = articleQueryDomainService.findArticleById(articleId);

        // 构建文章详情
        return articleAggregateDomainService.buildArticleDetailVO(article, currentUserId);
    }

    /**
     * 获取文章和作者聚合
     * @param articleId 文章ID
     * @return 文章和作者聚合
     */
    public ArticleAndAuthorAggregate getArticleAndAuthorAggregate(Long articleId) {
        log.info("[文章应用服务] 获取文章聚合 - articleId: {}", articleId);

        // 查询文章
        ArticleEntity article = articleQueryDomainService.findArticleById(articleId);

        // 构建聚合
        return articleAggregateDomainService.buildArticleAndAuthorAggregate(article);
    }

    /**
     * 根据用户ID查询文章列表
     * @param userId 用户ID
     * @return 文章列表
     */
    public List<ArticleListVO> getArticlesByUserId(Long userId) {
        log.info("[文章应用服务] 获取用户文章列表 - userId: {}", userId);
        return articleQueryDomainService.queryArticlesByUserId(userId);
    }

    /**
     * 获取草稿文章列表
     * @param userId 用户ID
     * @return 草稿文章列表
     */
    public List<ArticleListVO> getDraftArticleList(Long userId) {
        log.info("[文章应用服务] 获取草稿文章列表 - userId: {}", userId);
        return articleQueryDomainService.queryDraftArticlesByUserId(userId);
    }

    /**
     * 分页查询文章
     * @param categoryId 分类ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 文章实体列表
     */
    public List<ArticleEntity> getArticlePageList(Long categoryId, Integer pageNo, Integer pageSize) {
        log.info("[文章应用服务] 分页查询文章 - categoryId: {}, pageNo: {}, pageSize: {}", 
                categoryId, pageNo, pageSize);
        return articleQueryDomainService.queryArticleEntitiesByCategoryId(categoryId, pageNo, pageSize);
    }

    /**
     * 获取所有已发布文章
     * @return 已发布文章列表
     */
    public List<ArticleEntity> getAllPublishedArticles() {
        log.info("[文章应用服务] 获取所有已发布文章");
        return articleQueryDomainService.findAllPublishedArticles();
    }

    /**
     * 获取所有文章
     * @return 所有文章列表
     */
    public List<ArticleEntity> getAllArticles() {
        log.info("[文章应用服务] 获取所有文章");
        return articleQueryDomainService.findAllArticles();
    }
}