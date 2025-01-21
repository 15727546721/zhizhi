package cn.xu.domain.article.service;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.infrastructure.common.response.PageResponse;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface IArticleService {

    Long createArticle(ArticleEntity articleEntity);

    String uploadCover(MultipartFile imageFile);

    PageResponse<List<ArticlePageVO>> listArticle(ArticleRequest articleRequest);

    void deleteArticles(List<Long> articleIds);

    void updateArticle(ArticleEntity articleEntity);

    ArticleEntity getArticleById(Long id);

    List<ArticleListVO> getArticleByCategory(Long categoryId);

    /**
     * 获取热门文章列表
     *
     * @param limit 限制数量
     * @return 热门文章列表
     */
    List<ArticleListVO> getHotArticles(int limit);

    /**
     * 获取用户点赞的文章列表
     *
     * @param userId 用户ID
     * @return 文章列表
     */
    List<ArticleListVO> getUserLikedArticles(Long userId);

    /**
     * 获取文章的点赞用户列表
     *
     * @param articleId 文章ID
     * @return 用户ID列表
     */
    List<Long> getArticleLikedUsers(Long articleId);

    /**
     * 获取文章的点赞状态
     *
     * @param articleId 文章ID
     * @param userId    用户ID
     * @return 是否已点赞
     */
    boolean isArticleLiked(Long articleId, Long userId);

    /**
     * 获取所有文章，用于重建索引
     *
     * @return 所有文章列表
     */
    List<ArticleEntity> getAllArticles();
}
