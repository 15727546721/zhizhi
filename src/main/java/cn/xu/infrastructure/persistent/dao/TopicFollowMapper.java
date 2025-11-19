package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.TopicFollow;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * 话题关注Mapper
 */
@Mapper
public interface TopicFollowMapper {

    @Insert("INSERT INTO topic_follow (user_id, topic_id, create_time) VALUES (#{userId}, #{topicId}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(TopicFollow topicFollow);

    @Delete("DELETE FROM topic_follow WHERE user_id = #{userId} AND topic_id = #{topicId}")
    int deleteByUserIdAndTopicId(@Param("userId") Long userId, @Param("topicId") Long topicId);

    @Select("SELECT id, user_id, topic_id, create_time FROM topic_follow WHERE user_id = #{userId} AND topic_id = #{topicId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "topicId", column = "topic_id"),
            @Result(property = "createTime", column = "create_time")
    })
    TopicFollow selectByUserIdAndTopicId(@Param("userId") Long userId, @Param("topicId") Long topicId);

    @Select("SELECT id, user_id, topic_id, create_time FROM topic_follow WHERE user_id = #{userId}")
    @Results({
            @Result(property = "id", column = "id"),
            @Result(property = "userId", column = "user_id"),
            @Result(property = "topicId", column = "topic_id"),
            @Result(property = "createTime", column = "create_time")
    })
    List<TopicFollow> selectByUserId(@Param("userId") Long userId);

    @Select("SELECT COUNT(*) FROM topic_follow WHERE topic_id = #{topicId}")
    Long selectCountByTopicId(@Param("topicId") Long topicId);
}
