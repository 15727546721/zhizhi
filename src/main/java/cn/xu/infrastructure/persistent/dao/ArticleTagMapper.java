package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.ArticleTagRel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ArticleTagMapper {
    void insert(ArticleTagRel articleTagRel);

    void insertTags(List<ArticleTagRel> articleTagRel);

    void deleteByArticleIds(@Param("articleIds") List<Long> articleIds);

    void insertBatchByList(List<ArticleTagRel> articleTagRels);

    void deleteByArticleId(@Param("articleId") Long articleId);
    
    // 添加根据文章ID查询标签ID的方法
    List<Long> selectTagIdsByArticleId(@Param("articleId") Long articleId);
}