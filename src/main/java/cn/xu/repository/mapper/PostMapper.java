package cn.xu.repository.mapper;

import cn.xu.controller.admin.model.dto.post.SysPostQueryRequest;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.Tag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 帖子Mapper接口
 * <p>处理帖子相关的数据库操作</p>

 */
@Mapper
public interface PostMapper {
    Long insert(Post post);

    List<Post> queryByPage(SysPostQueryRequest postRequest);

    void deleteByIds(@Param("postIds") List<Long> postIds);

    Post findById(@Param("id") Long id);
    
    /**
     * 批量查询帖子
     *
     * @param ids 帖子ID列表
     * @return 帖子列表
     */
    List<Post> findByIds(@Param("ids") List<Long> ids);

    void update(Post post);

    /**
     * 根据用户ID获取帖子列表
     *
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<Post> findByUserId(@Param("userId") Long userId);

    /**
     * 根据帖子ID更改帖子状态
     *
     * @param status 状态
     * @param id 帖子ID
     */
    void updateStatus(@Param("status") Integer status, @Param("id") Long id);

    /**
     * 更新帖子加精状态
     *
     * @param isFeatured 加精状态
     * @param id 帖子ID
     */
    void updateFeatured(@Param("isFeatured") Integer isFeatured, @Param("id") Long id);

    /**
     * 根据用户ID获取草稿箱帖子列表
     *
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<Post> findDraftPostListByUserId(Long userId);

    /**
     * 删除帖子
     *
     * @param id 帖子ID
     */
    void deleteById(Long id);

    /**
     * 更新帖子点赞数
     *
     * @param postId 帖子ID
     * @param count 点赞数
     */
    void updateLikeCount(@Param("postId") long postId, @Param("count") Long count);

    /**
     * 更新帖子评论数
     *
     * @param postId 帖子ID
     * @param count 评论数
     */
    void updateCommentCount(@Param("postId") Long postId, @Param("count") int count);

    /**
     * 更新帖子收藏数
     *
     * @param postId 帖子ID
     * @param count 收藏数
     */
    void updateFavoriteCount(@Param("postId") long postId, @Param("count") Long count);

    /**
     * 增加帖子收藏数
     */
    void increaseFavoriteCount(@Param("postId") Long postId);

    /**
     * 减少帖子收藏数
     */
    void decreaseFavoriteCount(@Param("postId") Long postId);

    /**
     * 更新帖子浏览量
     *
     * @param postId 帖子ID
     * @param viewCount 浏览量
     */
    void updateViewCount(@Param("postId") Long postId, @Param("viewCount") Long viewCount);

    /**
     * 分页查询帖子列表（带排序）
     */
    List<Post> getPostPageListWithSort(@Param("offset") Integer offset,
                                       @Param("size") Integer size,
                                       @Param("sortBy") String sortBy);

    /**
     * 检查帖子是否存在
     */
    boolean existsById(@Param("id") Long id);

    /**
     * 统计用户已发布帖子数量
     */
    Long countPublishedByUserId(@Param("userId") Long userId);

    /**
     * 统计用户草稿数量
     */
    Long countDraftsByUserId(@Param("userId") Long userId);

    /**
     * 获取已发布帖子列表（分页）
     */
    List<Post> getPublishedPostPageList(@Param("offset") Integer offset, @Param("size") Integer size);

    /**
     * 根据标题搜索帖子（分页）
     */
    List<Post> searchPosts(@Param("keyword") String keyword,
                           @Param("offset") int offset,
                           @Param("limit") int limit);

    /**
     * 统计根据标题搜索的帖子数量
     */
    Long countSearchResults(@Param("keyword") String keyword);

    /**
     * 支持筛选的搜索帖子（分页）
     */
    List<Post> searchPostsWithFilters(@Param("keyword") String keyword,
                                     @Param("startTime") java.time.LocalDateTime startTime,
                                     @Param("endTime") java.time.LocalDateTime endTime,
                                     @Param("sortBy") String sortBy,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);

    /**
     * 统计支持筛选的搜索结果数量
     */
    Long countSearchResultsWithFilters(@Param("keyword") String keyword,
                                      @Param("startTime") java.time.LocalDateTime startTime,
                                      @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 查询热门帖子列表
     */
    List<Post> findHotPosts(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 按时间范围查询热门帖子（用于周榜/月榜）
     */
    List<Post> findHotPostsByTimeRange(@Param("startTime") java.time.LocalDateTime startTime,
                                       @Param("offset") int offset,
                                       @Param("limit") int limit);

    /**
     * 按指定排序方式查询帖子
     */
    List<Post> findPostsBySort(@Param("sort") String sort,
                               @Param("offset") int offset,
                               @Param("limit") int limit);

    /**
     * 按时间范围和排序方式查询帖子
     */
    List<Post> findPostsByTimeRangeAndSort(@Param("startTime") java.time.LocalDateTime startTime,
                                           @Param("sort") String sort,
                                           @Param("offset") int offset,
                                           @Param("limit") int limit);

    /**
     * 统计热门帖子数量
     */
    Long countHotPosts();

    /**
     * 根据标签ID查询帖子列表
     */
    List<Post> findPostsByTagId(@Param("tagId") Long tagId,
                                @Param("offset") int offset,
                                @Param("limit") int limit);

    /**
     * 统计指定标签的帖子数量
     */
    Long countPostsByTagId(@Param("tagId") Long tagId);

    /**
     * 根据用户ID列表查询帖子列表
     */
    List<Post> findPostsByUserIds(@Param("userIds") List<Long> userIds,
                                  @Param("offset") int offset,
                                  @Param("limit") int limit);

    /**
     * 统计指定用户的帖子数量
     */
    Long countPostsByUserIds(@Param("userIds") List<Long> userIds);

    /**
     * 根据ID列表查询帖子列表
     */
    List<Post> findPostsByIds(@Param("postIds") List<Long> postIds);

    /**
     * 根据用户ID和帖子状态分页查询帖子列表
     */
    List<Post> findPostsByUserIdAndStatus(@Param("userId") Long userId,
                                         @Param("status") String status,
                                         @Param("offset") int offset,
                                         @Param("limit") int limit);

    /**
     * 查询精选帖子列表
     */
    List<Post> findFeaturedPosts(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计精选帖子数量
     */
    Long countFeaturedPosts();

    /**
     * 按标签查询热门帖子
     */
    List<Post> findHotPostsByTagId(@Param("tagId") Long tagId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计按标签筛选的热门帖子数量
     */
    Long countHotPostsByTagId(@Param("tagId") Long tagId);

    /**
     * 按标签查询精选帖子
     */
    List<Post> findFeaturedPostsByTagId(@Param("tagId") Long tagId, @Param("offset") int offset, @Param("limit") int limit);

    /**
     * 统计按标签筛选的精选帖子数量
     */
    Long countFeaturedPostsByTagId(@Param("tagId") Long tagId);

    /**
     * 按收藏数获取帖子列表
     */
    List<Post> findPostsByFavoriteCount(@Param("limit") int limit);

    /**
     * 获取所有已发布的帖子
     */
    List<Post> findAllPublishedPosts();

    /**
     * 获取所有帖子
     */
    List<Post> findAll();

    /**
     * 分页查询帖子列表
     */
    List<Post> getPostPageList(@Param("offset") Integer offset, @Param("size") Integer size);

    /**
     * 查询热门标签
     */
    List<Tag> findHotTags(@Param("limit") int limit);

    /**
     * 统计所有帖子数量
     */
    Long countAll();

    /**
     * 按状态统计帖子数
     */
    Long countByStatus(@Param("status") Integer status);

    /**
     * 统计加精帖子数
     */
    Long countFeatured();

    /**
     * 批量更新帖子计数（用于Redis同步）
     */
    void updateCounts(@Param("postId") Long postId,
                      @Param("viewCount") long viewCount,
                      @Param("likeCount") long likeCount,
                      @Param("commentCount") long commentCount,
                      @Param("favoriteCount") long favoriteCount);

    /**
     * 分页查询所有帖子（用于批量索引）
     */
    List<Post> findAllWithPagination(@Param("offset") int offset, @Param("limit") int limit);

    /**
     * 后台帖子列表查询（不包含content字段，优化性能）
     */
    List<cn.xu.controller.admin.model.vo.SysPostListVO> findPostListForAdmin(
            @Param("title") String title,
            @Param("status") Integer status,
            @Param("userId") Long userId,
            @Param("tagId") Long tagId,
            @Param("offset") int offset,
            @Param("pageSize") int pageSize
    );

    /**
     * 后台帖子总数统计（支持筛选条件）
     */
    Long countPostsForAdmin(
            @Param("title") String title,
            @Param("status") Integer status,
            @Param("userId") Long userId,
            @Param("tagId") Long tagId
    );
    
    /**
     * 获取帖子点赞数
     *
     * @param postId 帖子ID
     * @return 点赞数
     */
    Long getLikeCount(@Param("postId") Long postId);
    
    /**
     * 获取帖子收藏数
     *
     * @param postId 帖子ID
     * @return 收藏数
     */
    Long getFavoriteCount(@Param("postId") Long postId);
    
    /**
     * 获取帖子作者ID
     *
     * @param postId 帖子ID
     * @return 作者ID
     */
    Long getAuthorId(@Param("postId") Long postId);
}
