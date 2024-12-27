package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.ArticleCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ICategoryDao {
    void insert(ArticleCategory articleCategory);

    List<ArticleCategory> selectListByPage(@Param("page") int page, @Param("size") int size);

    void update(ArticleCategory articleCategory);

    void delete(@Param("list") List<Long> idList);

    List<ArticleCategory> selectList();

    ArticleCategory selectByArticleId(@Param("articleId") Long id);
}
