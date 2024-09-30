package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.ArticleTag;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface IArticleTagDao {
    void insert(ArticleTag articleTag);

    void insertTags(List<ArticleTag> articleTag);
}
