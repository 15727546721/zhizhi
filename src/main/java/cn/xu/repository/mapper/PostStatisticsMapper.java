package cn.xu.repository.mapper;

import cn.xu.model.entity.PostStatistics;
import org.apache.ibatis.annotations.*;

/**
 * 帖子统计Mapper
 */
@Mapper
public interface PostStatisticsMapper {
    
    /**
     * 插入统计记录
     */
    @Insert("INSERT INTO post_statistics (post_id, view_count, comment_count, like_count, favorite_count, share_count, hot_score) " +
            "VALUES (#{postId}, #{viewCount}, #{commentCount}, #{likeCount}, #{favoriteCount}, #{shareCount}, #{hotScore})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(PostStatistics statistics);
    
    /**
     * 根据帖子ID查询
     */
    @Select("SELECT * FROM post_statistics WHERE post_id = #{postId}")
    PostStatistics selectByPostId(Long postId);
    
    /**
     * 更新统计
     */
    @Update("UPDATE post_statistics SET view_count = #{viewCount}, comment_count = #{commentCount}, " +
            "like_count = #{likeCount}, favorite_count = #{favoriteCount}, share_count = #{shareCount}, " +
            "hot_score = #{hotScore}, update_time = NOW() WHERE post_id = #{postId}")
    int update(PostStatistics statistics);
    
    /**
     * 增加浏览量
     */
    @Update("UPDATE post_statistics SET view_count = view_count + 1, update_time = NOW() WHERE post_id = #{postId}")
    int incrementViewCount(Long postId);
    
    /**
     * 增加评论数
     */
    @Update("UPDATE post_statistics SET comment_count = comment_count + 1, update_time = NOW() WHERE post_id = #{postId}")
    int incrementCommentCount(Long postId);
    
    /**
     * 减少评论数
     */
    @Update("UPDATE post_statistics SET comment_count = GREATEST(comment_count - 1, 0), update_time = NOW() WHERE post_id = #{postId}")
    int decrementCommentCount(Long postId);
    
    /**
     * 增加点赞数
     */
    @Update("UPDATE post_statistics SET like_count = like_count + 1, update_time = NOW() WHERE post_id = #{postId}")
    int incrementLikeCount(Long postId);
    
    /**
     * 减少点赞数
     */
    @Update("UPDATE post_statistics SET like_count = GREATEST(like_count - 1, 0), update_time = NOW() WHERE post_id = #{postId}")
    int decrementLikeCount(Long postId);
    
    /**
     * 增加收藏数
     */
    @Update("UPDATE post_statistics SET favorite_count = favorite_count + 1, update_time = NOW() WHERE post_id = #{postId}")
    int incrementFavoriteCount(Long postId);
    
    /**
     * 减少收藏数
     */
    @Update("UPDATE post_statistics SET favorite_count = GREATEST(favorite_count - 1, 0), update_time = NOW() WHERE post_id = #{postId}")
    int decrementFavoriteCount(Long postId);
    
    /**
     * 删除统计
     */
    @Delete("DELETE FROM post_statistics WHERE post_id = #{postId}")
    int deleteByPostId(Long postId);
}
