package cn.xu.infrastructure.persistent.dao;

import cn.xu.api.dto.article.ArticlePageResponse;
import cn.xu.api.dto.article.ArticleRequest;
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
}
