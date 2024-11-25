package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.ArticleTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IArticleTagDao {
    void insert(ArticleTag articleTag);

    void insertTags(List<ArticleTag> articleTag);

    void deleteByArticleIds(@Param("articleIds") List<Long> articleIds);

    void insertBatchByList(List<ArticleTag> articleTags);
}
