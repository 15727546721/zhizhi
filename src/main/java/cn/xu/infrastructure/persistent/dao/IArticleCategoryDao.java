package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.ArticleCategory;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IArticleCategoryDao {
    void insert(ArticleCategory articleCategory);
}
