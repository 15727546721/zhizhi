package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.ArticleTag;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IArticleTagDao {
    void insert(ArticleTag articleTag);
}
