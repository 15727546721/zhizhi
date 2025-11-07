package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.PostTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostTagMapper {
    void insert(PostTag postTag);

    void insertTags(List<PostTag> postTags);

    void deleteByPostIds(@Param("postIds") List<Long> postIds);

    void insertBatchByList(List<PostTag> postTags);

    void deleteByPostId(@Param("postId") Long postId);
    
    // 根据帖子ID和标签ID列表删除标签关联
    void deleteByPostIdAndTagIds(@Param("postId") Long postId, @Param("tagIds") List<Long> tagIds);
    
    // 根据标签ID删除所有关联关系
    void deleteByTagId(@Param("tagId") Long tagId);
    
    // 添加根据帖子ID查询标签ID的方法
    List<Long> selectTagIdsByPostId(@Param("postId") Long postId);
    
    // 根据标签ID查询帖子ID列表
    List<Long> selectPostIdsByTagId(@Param("tagId") Long tagId, @Param("offset") int offset, @Param("limit") int limit);
    
    // 根据帖子ID列表查询标签关联信息
    List<PostTag> selectByPostIds(@Param("postIds") List<Long> postIds);
    
    // 统计标签使用次数
    int countByTagId(@Param("tagId") Long tagId);
    
    // 获取热门标签统计
    List<TagStatistics> selectHotTags(@Param("limit") int limit);
    
    // 根据帖子类型获取标签统计
    List<TagStatistics> selectTagStatisticsByPostType(@Param("postType") String postType, @Param("limit") int limit);
    
    class TagStatistics {
        private Long tagId;
        private String tagName;
        private Integer usageCount;
        
        // Getters and Setters
        public Long getTagId() {
            return tagId;
        }
        
        public void setTagId(Long tagId) {
            this.tagId = tagId;
        }
        
        public String getTagName() {
            return tagName;
        }
        
        public void setTagName(String tagName) {
            this.tagName = tagName;
        }
        
        public Integer getUsageCount() {
            return usageCount;
        }
        
        public void setUsageCount(Integer usageCount) {
            this.usageCount = usageCount;
        }
    }
}