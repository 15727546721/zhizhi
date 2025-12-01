package cn.xu.repository;

import cn.xu.model.dto.post.PostAndTagAgg;
import cn.xu.model.entity.Tag;

import java.util.List;

/**
 * 标签仓储接口（简化版）
 * 
 * 设计改进：
 * 1. 直接返回Tag PO，移除TagEntity转换
 * 2. 简化数据访问逻辑
 */
public interface ITagRepository {
    
    /**
     * 添加标签
     *
     * @param name 标签名称
     */
    void addTag(String name);
    
    /**
     * 根据帖子ID获取标签名称列表
     *
     * @param postId 帖子ID
     * @return 标签名称列表
     */
    List<String> getTagNamesByPostId(Long postId);
    
    /**
     * 根据帖子ID列表获取标签聚合信息
     *
     * @param postIds 帖子ID列表
     * @return 标签聚合信息列表
     */
    List<PostAndTagAgg> selectByPostIds(List<Long> postIds);
    
    /**
     * 搜索标签
     *
     * @param keyword 搜索关键词
     * @return 标签列表
     */
    List<Tag> searchTags(String keyword);
    
    /**
     * 获取热门标签
     *
     * @param limit 限制数量
     * @return 标签列表
     */
    List<Tag> getHotTags(int limit);
    
    /**
     * 获取热门标签（支持时间维度）
     *
     * @param timeRange 时间范围：today(今日)、week(本周)、month(本月)、all(全部)
     * @param limit 限制数量
     * @return 标签列表
     */
    List<Tag> getHotTagsByTimeRange(String timeRange, int limit);
    
    /**
     * 获取所有标签
     *
     * @return 标签列表
     */
    List<Tag> getAllTags();
    
    /**
     * 根据帖子ID获取标签ID列表
     *
     * @param postId 帖子ID
     * @return 标签ID列表
     */
    List<Long> findTagIdsByPostId(Long postId);
    
    /**
     * 根据ID获取标签
     *
     * @param id 标签ID
     * @return 标签
     */
    Tag getTagById(Long id);
    
    /**
     * 更新标签名称
     *
     * @param id 标签ID
     * @param name 标签名称
     */
    void updateTag(Long id, String name);
    
    /**
     * 更新标签（完整对象）
     *
     * @param tag 标签对象
     */
    void updateTag(Tag tag);
    
    /**
     * 删除标签
     *
     * @param id 标签ID
     */
    void deleteTag(Long id);
}