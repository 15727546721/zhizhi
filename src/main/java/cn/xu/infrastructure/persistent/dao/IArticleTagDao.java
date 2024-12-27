package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.ArticleTagRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IArticleTagDao {
    void insert(ArticleTagRelation articleTagRelation);

    void insertTags(List<ArticleTagRelation> articleTagRelation);

    void deleteByArticleIds(@Param("articleIds") List<Long> articleIds);

    void insertBatchByList(List<ArticleTagRelation> articleTagRelations);

    void deleteByArticleId(@Param("articleId") Long articleId);
}
