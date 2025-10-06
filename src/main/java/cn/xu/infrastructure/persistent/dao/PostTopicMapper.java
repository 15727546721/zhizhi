package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.PostTopic;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostTopicMapper {
    void insert(PostTopic postTopic);

    void insertBatchByList(List<PostTopic> postTopics);

    void deleteByPostId(@Param("postId") Long postId);
    
    // 根据帖子ID和话题ID列表删除话题关联
    void deleteByPostIdAndTopicIds(@Param("postId") Long postId, @Param("topicIds") List<Long> topicIds);
    
    // 添加根据帖子ID查询话题ID的方法
    List<Long> selectTopicIdsByPostId(@Param("postId") Long postId);
    
    // 根据话题ID查询帖子ID列表
    List<Long> selectPostIdsByTopicId(@Param("topicId") Long topicId, @Param("offset") int offset, @Param("limit") int limit);
    
    // 根据帖子ID列表查询话题关联信息
    List<PostTopic> selectByPostIds(@Param("postIds") List<Long> postIds);
}