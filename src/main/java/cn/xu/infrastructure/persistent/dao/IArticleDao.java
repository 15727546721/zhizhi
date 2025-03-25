package cn.xu.infrastructure.persistent.dao;

import cn.xu.api.system.model.dto.article.ArticleRequest;
import cn.xu.api.web.model.dto.article.ArticlePageRequest;
import cn.xu.api.web.model.vo.article.ArticleListPageVO;
import cn.xu.api.web.model.vo.article.ArticleListVO;
import cn.xu.api.web.model.vo.article.ArticlePageVO;
import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.infrastructure.persistent.po.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IArticleDao {
    Long insert(Article article);

    List<ArticlePageVO> queryByPage(ArticleRequest articleRequest);

    void deleteByIds(@Param("articleIds") List<Long> articleIds);

    Article findById(@Param("id") Long id);

    void update(Article article);

    List<ArticleEntity> queryArticleByPage(@Param("page") int page, @Param("size") int size);

    List<ArticleListVO> queryByCategoryId(Long categoryId);

    /**
     * 获取所有已发布的文章
     */
    List<Article> findAllPublishedArticles();

    /**
     * 获取所有文章
     *
     * @return 所有文章列表
     */
    List<Article> findAll();

    /**
     * 根据用户id获取文章列表
     *
     * @param userId
     * @return
     */
    List<ArticleListVO> queryByUserId(@Param("userId") Long userId);

    /**
     * 根据文章id更改文章状态
     *
     * @param status
     * @param id
     */
    void updateStatus(@Param("status") Integer status, @Param("id") Long id);

    /**
     * 根据用户id获取草稿箱文章列表
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


    /**
     * 更新文章点赞数
     *
     * @param targetId
     * @param count
     */
    void updateLikeCount(@Param("targetId") long targetId, @Param("count") Integer count);

    /**
     * 更新文章评论数
     *
     * @param articleId
     * @param count
     */
    void updateCommentCount(@Param("articleId") Long articleId, @Param("count") int count);

    /**
     * 分页查询文章列表
     * @param request
     * @return
     */
    List<ArticleEntity> getArticlePageByCategory(Long categoryId, int offset, int size);
}
