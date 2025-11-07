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
     * 获取热门标签（支持时间维度）
     *
     * @param timeRange 时间范围：today(今日)、week(本周)、month(本月)、all(全部)
     * @param limit 限制数量
     * @return 标签列表
     */
    List<Tag> getHotTagsByTimeRange(@Param("timeRange") String timeRange, @Param("limit") int limit);
    
    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    List<Tag> getAllTags();
    
    /**
     * 根据ID获取标签
     *
     * @param id 标签ID
     * @return 标签实体
     */
    Tag getTagById(@Param("id") Long id);
    
    /**
     * 更新标签
     *
     * @param id 标签ID
     * @param name 标签名称
     */
    void updateTag(@Param("id") Long id, @Param("name") String name);
    
    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTag(@Param("id") Long id);
}