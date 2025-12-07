package cn.xu.repository.mapper;

import cn.xu.model.dto.post.PostAndTagAgg;
import cn.xu.model.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 标签Mapper接口
 * <p>处理标签相关的数据库操作</p>
 
 */
@Mapper
public interface TagMapper {
    Long addTag(@Param("name") String name);

    List<String> getTagNamesByPostId(Long postId);

    /**
     * 根据帖子ID列表查询帖子标签聚合
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
     * 更新标签名称
     *
     * @param id 标签ID
     * @param name 标签名称
     */
    void updateTag(@Param("id") Long id, @Param("name") String name);
    
    /**
     * 更新标签（完整对象）
     *
     * @param tag 标签对象
     */
    void updateTagFull(@Param("tag") Tag tag);
    
    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTag(@Param("id") Long id);
    
    /**
     * 批量增加标签使用次数
     *
     * @param tagIds 标签ID列表
     */
    void incrementUsageCountBatch(@Param("tagIds") List<Long> tagIds);
    
    /**
     * 批量减少标签使用次数
     *
     * @param tagIds 标签ID列表
     */
    void decrementUsageCountBatch(@Param("tagIds") List<Long> tagIds);
    
    /**
     * 统计所有标签数
     */
    Long countAll();
    
    /**
     * 批量根据ID获取标签
     *
     * @param tagIds 标签ID集合
     * @return 标签列表
     */
    List<Tag> findByIds(@Param("tagIds") List<Long> tagIds);
}
