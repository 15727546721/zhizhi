package cn.xu.repository.mapper;

import cn.xu.model.entity.Share;
import org.apache.ibatis.annotations.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * 分享记录Mapper
 */
@Mapper
public interface ShareMapper {

    /**
     * 插入分享记录
     */
    @Insert("INSERT INTO share (post_id, user_id, platform, ip, user_agent, create_time) " +
            "VALUES (#{postId}, #{userId}, #{platform}, #{ip}, #{userAgent}, #{createTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    int insert(Share share);

    /**
     * 根据帖子ID查询分享记录
     */
    @Select("SELECT * FROM share WHERE post_id = #{postId} ORDER BY create_time DESC LIMIT #{limit} OFFSET #{offset}")
    List<Share> selectByPostId(@Param("postId") Long postId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计帖子分享数
     */
    @Select("SELECT COUNT(*) FROM share WHERE post_id = #{postId}")
    long countByPostId(@Param("postId") Long postId);

    /**
     * 统计用户分享数
     */
    @Select("SELECT COUNT(*) FROM share WHERE user_id = #{userId}")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 按平台统计帖子分享数
     */
    @Select("SELECT platform, COUNT(*) as count FROM share WHERE post_id = #{postId} GROUP BY platform")
    List<Map<String, Object>> countByPostIdGroupByPlatform(@Param("postId") Long postId);

    /**
     * 查询用户是否分享过某帖子
     */
    @Select("SELECT COUNT(*) FROM share WHERE post_id = #{postId} AND user_id = #{userId}")
    long countByPostIdAndUserId(@Param("postId") Long postId, @Param("userId") Long userId);

    /**
     * 查询时间范围内的分享记录
     */
    @Select("SELECT * FROM share WHERE create_time BETWEEN #{startTime} AND #{endTime} ORDER BY create_time DESC")
    List<Share> selectByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 统计时间范围内的分享数
     */
    @Select("SELECT COUNT(*) FROM share WHERE create_time BETWEEN #{startTime} AND #{endTime}")
    long countByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime);

    /**
     * 获取分享排行榜（按帖子分享数排序）
     */
    @Select("SELECT post_id, COUNT(*) as share_count FROM share " +
            "WHERE create_time >= #{startTime} " +
            "GROUP BY post_id ORDER BY share_count DESC LIMIT #{limit}")
    List<Map<String, Object>> getShareRanking(@Param("startTime") LocalDateTime startTime, @Param("limit") int limit);

    /**
     * 删除帖子的所有分享记录
     */
    @Delete("DELETE FROM share WHERE post_id = #{postId}")
    int deleteByPostId(@Param("postId") Long postId);
}
