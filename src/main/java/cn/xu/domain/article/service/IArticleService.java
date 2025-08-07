package cn.xu.domain.article.service;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.domain.article.model.aggregate.ArticleAndAuthorAggregate;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.infrastructure.common.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IArticleService {

    /**
     * 创建文章
     *
     * @param articleEntity
     * @return
     */
    Long createArticle(ArticleEntity articleEntity);

    /**
     * 保存文章草稿
     *
     * @param articleEntity
     * @return
     */
    Long createOrUpdateArticleDraft(ArticleEntity articleEntity);

    /**
     * 上传文章封面
     *
     * @param imageFile
     * @return
     */
    String uploadCover(MultipartFile imageFile);

    /**
     * 获取文章列表
     *
     * @param articleRequest
     * @return
     */
    PageResponse<List<ArticlePageVO>> listArticle(ArticleRequest articleRequest);

    /**
     * 删除文章
     *
     * @param articleIds
     */
    void deleteArticles(List<Long> articleIds);

    /**
     * 更新文章
     */
    void updateArticle(ArticleEntity articleEntity);

    /**
     * 获取全部文章（已发布）
     */
    List<ArticleEntity> getAllPublishedArticles();

    /**
     * 获取所有文章，用于重建索引
     *
     * @return 所有文章列表
     */
    List<ArticleEntity> getAllArticles();

    /**
     * 获取文章详细信息
     *
     * @param articleId 文章ID
     * @return 文章实体，不存在则返回null
     */
    ArticleAndAuthorAggregate getArticleDetailById(Long articleId);

    List<ArticleListVO> getArticlesByUserId(Long userId);

    /**
     * 发布文章（把草稿发布为正式文章）
     *
     * @param articleEntity
     * @param userId
     */
    void publishArticle(ArticleEntity articleEntity, Long userId);

    /**
     * 获取文章草稿列表
     *
     * @param userId
     * @return
     */
    List<ArticleListVO> getDraftArticleList(Long userId);

    /**
     * 删除文章
     *
     * @param id     文章ID
     * @param userId 用户ID
     */
    void deleteArticle(Long id, Long userId);

    /**
     * 文章阅读数+1
     *
     * @param articleId
     */
    void viewArticle(Long articleId);

    /**
     * 更新文章热度
     *
     * @param articleId
     */
    void updateArticleHotScore(Long articleId);

    /**
     * 根据分类ID获取文章列表
     *
     * @param categoryId
     * @return
     */
    List<ArticleEntity> getArticlePageListByCategoryId(Long categoryId, Integer pageNo, Integer pageSize);

    /**
     * 获取文章分页列表
     *
     * @param pageNo
     * @param pageSize
     * @return
     */
    List<ArticleEntity> getArticlePageList(Integer pageNo, Integer pageSize);
}
