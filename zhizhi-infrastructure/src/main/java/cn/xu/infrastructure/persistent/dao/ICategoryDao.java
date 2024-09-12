package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Category;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ICategoryDao {
    void insert(Category category);

    List<Category> selectListByPage(@Param("page") int page, @Param("size") int size);

    void update(Category category);

    void delete(@Param("list") List<Long> idList);
}
