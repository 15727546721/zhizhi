package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.article.model.entity.ArticleEntity;
import cn.xu.infrastructure.persistent.po.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IArticleDao {
    void insert(Article article);

    List<ArticleEntity> queryByPage(@Param("page") int page, @Param("size") int size);
}
