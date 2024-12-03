package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ITagDao {
    void insert(Tag tag);

    List<Tag> selectListByPage(@Param("page") int page, @Param("size") int size);

    void update(Tag tag);

    void delete(List<Long> idList);

    Tag selectById(@Param("id") Long tagId);

    List<Tag> selectList();

    List<Tag> selectByArticleId(@Param("articleId") Long id);
}
