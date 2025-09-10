package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.article.model.aggregate.ArticleAndTagAgg;
import cn.xu.infrastructure.persistent.po.ArticleTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {
    void insert(ArticleTag articleTag);

    List<ArticleTag> selectListByPage(@Param("page") int page, @Param("size") int size);

    void update(ArticleTag articleTag);

    void delete(List<Long> idList);

    ArticleTag selectById(@Param("id") Long tagId);

    List<ArticleTag> selectList();

    List<ArticleTag> selectByArticleId(@Param("articleId") Long articleId);

    /**
     *
     * @param articleIds
     * @return
     */
    List<ArticleAndTagAgg> selectByArticleIds(@Param("articleIds") List<Long> articleIds);
}
