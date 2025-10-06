package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.PostCategory;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CategoryMapper {
    void insert(PostCategory postCategory);

    List<PostCategory> selectListByPage(@Param("page") int page, @Param("size") int size);

    void update(PostCategory postCategory);

    void delete(@Param("list") List<Long> idList);

    List<PostCategory> selectList();

    PostCategory selectByPostId(@Param("postId") Long id);
    
    /**
     * 搜索分类
     *
     * @param keyword 搜索关键词
     * @return 分类列表
     */
    List<PostCategory> searchCategories(@Param("keyword") String keyword);
}