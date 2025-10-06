package cn.xu.infrastructure.persistent.dao;

import cn.xu.domain.post.model.aggregate.PostAndTagAgg;
import cn.xu.infrastructure.persistent.po.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface TagMapper {
    void addTag(@Param("name") String name);

    List<String> getTagNamesByPostId(Long postId);

    /**
     *
     * @param postIds
     * @return
     */
    List<PostAndTagAgg> selectByPostIds(@Param("postIds") List<Long> postIds);
    
    /**
     * 搜索标签
     *
     * @param keyword 搜索关键词
     * @return 标签列表
     */
    List<Tag> searchTags(@Param("keyword") String keyword);
    
    /**
     * 获取热门标签
     *
     * @param limit 限制数量
     * @return 标签列表
     */
    List<Tag> getHotTags(@Param("limit") int limit);
    
    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    List<Tag> getAllTags();
}