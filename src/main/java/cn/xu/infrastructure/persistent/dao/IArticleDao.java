package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Article;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IArticleDao {
    void insert(Article article);

    List<Article> queryByPage(@Param("page") int page, @Param("size") int size);

    void deleteByIds(@Param("articleIds") List<Long> articleIds);

    Article findById(@Param("id") Long id);
}
