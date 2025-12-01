package cn.xu.repository.mapper;

import cn.xu.model.entity.UserStatistics;
import org.apache.ibatis.annotations.*;

/**
 * 用户统计Mapper
 */
@Mapper
public interface UserStatisticsMapper {
    
    /**
     * 插入统计记录
     */
    @Insert("INSERT INTO user_statistics (user_id, post_count, comment_count, essay_count, follow_count, fans_count, like_count, favorite_count, view_count) " +
            "VALUES (#{userId}, #{postCount}, #{commentCount}, #{essayCount}, #{followCount}, #{fansCount}, #{likeCount}, #{favoriteCount}, #{viewCount})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(UserStatistics statistics);
    
    /**
     * 根据用户ID查询
     */
    @Select("SELECT * FROM user_statistics WHERE user_id = #{userId}")
    UserStatistics selectByUserId(Long userId);
    
    /**
     * 更新统计
     */
    @Update("UPDATE user_statistics SET post_count = #{postCount}, comment_count = #{commentCount}, " +
            "essay_count = #{essayCount}, follow_count = #{followCount}, fans_count = #{fansCount}, " +
            "like_count = #{likeCount}, favorite_count = #{favoriteCount}, view_count = #{viewCount}, " +
            "update_time = NOW() WHERE user_id = #{userId}")
    int update(UserStatistics statistics);
    
    /**
     * 增加发帖数
     */
    @Update("UPDATE user_statistics SET post_count = post_count + 1, update_time = NOW() WHERE user_id = #{userId}")
    int incrementPostCount(Long userId);
    
    /**
     * 减少发帖数
     */
    @Update("UPDATE user_statistics SET post_count = GREATEST(post_count - 1, 0), update_time = NOW() WHERE user_id = #{userId}")
    int decrementPostCount(Long userId);
    
    /**
     * 增加评论数
     */
    @Update("UPDATE user_statistics SET comment_count = comment_count + 1, update_time = NOW() WHERE user_id = #{userId}")
    int incrementCommentCount(Long userId);
    
    /**
     * 减少评论数
     */
    @Update("UPDATE user_statistics SET comment_count = GREATEST(comment_count - 1, 0), update_time = NOW() WHERE user_id = #{userId}")
    int decrementCommentCount(Long userId);
    
    /**
     * 增加关注数
     */
    @Update("UPDATE user_statistics SET follow_count = follow_count + 1, update_time = NOW() WHERE user_id = #{userId}")
    int incrementFollowCount(Long userId);
    
    /**
     * 减少关注数
     */
    @Update("UPDATE user_statistics SET follow_count = GREATEST(follow_count - 1, 0), update_time = NOW() WHERE user_id = #{userId}")
    int decrementFollowCount(Long userId);
    
    /**
     * 增加粉丝数
     */
    @Update("UPDATE user_statistics SET fans_count = fans_count + 1, update_time = NOW() WHERE user_id = #{userId}")
    int incrementFansCount(Long userId);
    
    /**
     * 减少粉丝数
     */
    @Update("UPDATE user_statistics SET fans_count = GREATEST(fans_count - 1, 0), update_time = NOW() WHERE user_id = #{userId}")
    int decrementFansCount(Long userId);
    
    /**
     * 删除统计
     */
    @Delete("DELETE FROM user_statistics WHERE user_id = #{userId}")
    int deleteByUserId(Long userId);
}
