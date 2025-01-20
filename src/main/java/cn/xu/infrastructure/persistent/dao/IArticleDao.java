package cn.xu.infrastructure.persistent.dao;

import cn.xu.api.web.controller.article.ArticleListDTO;
import cn.xu.api.web.model.dto.article.ArticlePageResponse;
import cn.xu.api.web.model.dto.article.ArticleRequest;
import cn.xu.domain.article.model.entity.ArticleRecommendOrNew;
import cn.xu.infrastructure.persistent.po.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IArticleDao {
    Long insert(Article article);

    List<ArticlePageResponse> queryByPage(ArticleRequest articleRequest);

    void deleteByIds(@Param("articleIds") List<Long> articleIds);

    Article findById(@Param("id") Long id);

    void update(Article article);

    List<ArticleRecommendOrNew> queryArticleByPage(@Param("page") int page, @Param("size") int size);

    List<ArticleListDTO> queryByCategory(Long categoryId);

    /**
     * 获取所有文章
     *
     * @return 所有文章列表
     */
    List<Article> findAll();
}
