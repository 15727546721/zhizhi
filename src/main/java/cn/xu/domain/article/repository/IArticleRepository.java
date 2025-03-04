package cn.xu.domain.article.repository;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.domain.article.model.entity.ArticleRecommendOrNew;

import java.util.List;
import java.util.Map;

public interface IArticleRepository {
    Long save(ArticleEntity articleAggregate);

    List<ArticlePageVO> queryArticle(ArticleRequest articleRequest);

    void deleteByIds(List<Long> articleIds);

    ArticleEntity findById(Long id);

    void update(ArticleEntity articleEntity);

    List<ArticleRecommendOrNew> queryArticleByPage();

    List<ArticleListVO> queryArticleByCategoryId(Long categoryId);

    /**
     * 更新文章点赞数
     *
     * @param articleId 文章ID
     * @param likeCount 点赞数
     */
    void updateArticleLikeCount(Long articleId, Long likeCount);

    /**
     * 批量更新文章点赞数
     *
     * @param likeCounts key为文章ID，value为点赞数的Map
     */
    void batchUpdateArticleLikeCount(Map<Long, Long> likeCounts);

    /**
     * 获取所有已发布文章
     */
    List<ArticleEntity> findAllPublishedArticles();

    /**
     * 获取所有文章
     *
     * @return 所有文章列表
     */
    List<ArticleEntity> findAll();

    List<ArticleListVO> queryArticleByUserId(Long userId);

    /**
     * 更新文章状态
     *
     * @param status 状态值
     * @param id 文章ID
     */
    void updateArticleStatus(Integer status, Long id);

    /**
     * 查询草稿箱文章列表
     *
     * @param userId
     * @return
     */
    List<ArticleListVO> queryDraftArticleListByUserId(Long userId);

    /**
     * 删除文章
     *
     * @param id
     */
    void deleteById(Long id);
}
