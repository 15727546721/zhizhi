package cn.xu.infrastructure.persistent.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface ILikeDao {
    /**
     * 插入文章点赞记录
     *
     * @param articleId 文章id
     * @param userId    用户id
     * @param value     点赞值(1点赞，0取消点赞)
     * @param type      点赞类型-1文章
     */
    void insertArticleLikeRecord(@Param("articleId") Long articleId,
                                 @Param("userId") Long userId,
                                 @Param("value") int value,
                                 @Param("type") int type);

    /**
     * 批量更新文章点赞数
     *
     * @param likeCounts 点赞数Map
     */
    void batchUpdateArticleLikeCount(@Param("likeCounts") Map<Long, Long> likeCounts);

    /**
     * 获取用户点赞的文章列表
     *
     * @param userId 用户ID
     * @return 文章ID列表
     */
    List<Long> getUserLikedArticles(@Param("userId") Long userId);

    /**
     * 获取文章的点赞用户列表
     *
     * @param articleId 文章ID
     * @return 用户ID列表
     */
    List<Long> getArticleLikedUsers(@Param("articleId") Long articleId);

    /**
     * 获取热门文章ID列表（按点赞数排序）
     *
     * @param limit 限制数量
     * @return 文章ID列表
     */
    List<Long> getHotArticlesByLikes(@Param("limit") int limit);
}
