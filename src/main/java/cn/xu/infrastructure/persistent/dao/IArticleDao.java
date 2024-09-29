package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.article.model.entity.ArticleEntity;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IArticleDao {
    void insert(ArticleEntity articleEntity);

    List<ArticleEntity> queryByPage(@Param("page") int page, @Param("size") int size);
}
