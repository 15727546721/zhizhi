package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.article.model.entity.ArticleEntity;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IArticleDao {
    void insert(ArticleEntity articleEntity);
}
