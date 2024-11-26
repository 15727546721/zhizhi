package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.ArticleCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface IArticleCategoryDao {
    void insert(ArticleCategory articleCategory);

    void deleteByArticleId(@Param("articleId") Long articleId);
}
