package cn.xu.domain.article.service.impl;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleDetailVO;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.application.service.ArticleApplicationService;
import cn.xu.domain.article.model.aggregate.ArticleAndAuthorAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.service.ArticleHotScoreDomainService;
import cn.xu.domain.article.service.ArticleQueryDomainService;
import cn.xu.domain.article.service.IArticleService;
import cn.xu.infrastructure.common.response.PageResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 文章服务实现类
 * 作为应用服务的门面，委托给应用服务层处理具体业务逻辑
 * 遵循DDD架构规范，保持与原有接口的兼容性
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleService implements IArticleService {

    private final ArticleApplicationService articleApplicationService;
    private final ArticleQueryDomainService articleQueryDomainService;
    private final ArticleHotScoreDomainService articleHotScoreDomainService;

    @Override
    public Long createArticle(ArticleEntity articleEntity) {
        return articleApplicationService.createArticle(articleEntity, null);
    }

    @Override
    public Long createOrUpdateArticleDraft(ArticleEntity articleEntity) {
        return articleApplicationService.createOrUpdateArticleDraft(articleEntity, null);
    }

    @Override
    public String uploadCover(MultipartFile imageFile) {
        return articleApplicationService.uploadCover(imageFile);
    }

    @Override
    public PageResponse<List<ArticlePageVO>> listArticle(ArticleRequest articleRequest) {
        return articleApplicationService.listArticle(articleRequest);
    }

    @Override
    public void deleteArticles(List<Long> articleIds) {
        articleApplicationService.deleteArticlesByAdmin(articleIds);
    }

    @Override
    public void updateArticle(ArticleEntity articleEntity) {
        articleApplicationService.updateArticle(articleEntity, null);
    }

    @Override
    public ArticleAndAuthorAggregate getArticleDetailById(Long articleId) {
        return articleApplicationService.getArticleAndAuthorAggregate(articleId);
    }

    @Override
    public List<ArticleListVO> getArticlesByUserId(Long userId) {
        return articleApplicationService.getArticlesByUserId(userId);
    }

    @Override
    public void publishArticle(ArticleEntity articleEntity, Long userId) {
        articleApplicationService.publishArticle(articleEntity, userId);
    }

    @Override
    public List<ArticleListVO> getDraftArticleList(Long userId) {
        return articleApplicationService.getDraftArticleList(userId);
    }

    @Override
    public void deleteArticle(Long id, Long userId) {
        articleApplicationService.deleteArticle(id, userId);
    }

    @Override
    public void viewArticle(Long articleId) {
        articleApplicationService.viewArticle(articleId);
    }

    /**
     * 更新文章热度到 Redis
     * @param articleId 文章ID
     */
    public void updateArticleHotScore(Long articleId) {
        ArticleEntity article = articleQueryDomainService.findArticleById(articleId);
        articleHotScoreDomainService.updateHotScore(articleId, article);
    }

    /**
     * 获取前N篇热度最高的文章
     * @param topN 获取的文章数量
     * @return 热度前N篇文章的ID
     */
    public List<Long> getTopNHotArticles(int topN) {
        List<String> hotArticleIds = articleHotScoreDomainService.getTopNHotArticles(topN);
        return hotArticleIds.stream()
                .map(Long::parseLong)
                .collect(Collectors.toList());
    }

    @Override
    public List<ArticleEntity> getArticlePageListByCategoryId(Long categoryId, Integer pageNo, Integer pageSize) {
        return articleApplicationService.getArticlePageList(categoryId, pageNo, pageSize);
    }

    @Override
    public List<ArticleEntity> getArticlePageList(Integer pageNo, Integer pageSize) {
        return articleApplicationService.getArticlePageList(null, pageNo, pageSize);
    }

    public ArticleDetailVO getArticleDetail(Long articleId, Long currentUserId) {
        return articleApplicationService.getArticleDetail(articleId, currentUserId);
    }

    @Override
    public List<ArticleEntity> getAllPublishedArticles() {
        return articleApplicationService.getAllPublishedArticles();
    }

    @Override
    public List<ArticleEntity> getAllArticles() {
        return articleApplicationService.getAllArticles();
    }

}

