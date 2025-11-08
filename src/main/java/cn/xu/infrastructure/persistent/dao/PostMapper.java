package cn.xu.infrastructure.persistent.dao;

import cn.xu.api.system.model.dto.post.SysPostQueryRequest;
import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.api.web.model.vo.post.PostPageResponse;
import cn.xu.infrastructure.persistent.po.Post;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface PostMapper {
    Long insert(Post post);

    List<PostPageResponse> queryByPage(SysPostQueryRequest postRequest);

    void deleteByIds(@Param("postIds") List<Long> postIds);

    Post findById(@Param("id") Long id);

    void update(Post post);

    List<Post> findPostByPage(@Param("page") int page, @Param("size") int size);

    List<PostListResponse> findByCategoryId(Long categoryId);

    /**
     * 获取所有已发布的帖子
     */
    List<Post> findAllPublishedPosts();

    /**
     * 获取所有帖子
     *
     * @return 所有帖子列表
     */
    List<Post> findAll();

    /**
     * 根据用户id获取帖子列表
     *
     * @param userId
     * @return
     */
    List<Post> findByUserId(@Param("userId") Long userId);

    /**
     * 根据帖子id更改帖子状态
     *
     * @param status
     * @param id
     */
    void updateStatus(@Param("status") Integer status, @Param("id") Long id);

    /**
     * 根据用户id获取草稿箱帖子列表
     *
     * @param userId
     * @return
     */
    List<Post> findDraftPostListByUserId(Long userId);

    /**
     * 删除帖子
     *
     * @param id
     */
    void deleteById(Long id);


    /**
     * 更新帖子点赞数
     *
     * @param postId
     * @param count
     */
    void updateLikeCount(@Param("postId") long postId, @Param("count") Long count);

    /**
     * 更新帖子评论数
     *
     * @param postId
     * @param count
     */
    void updateCommentCount(@Param("postId") Long postId, @Param("count") int count);

    /**
     * 更新帖子收藏数
     *
     * @param postId
     * @param count
     */
    void updateFavoriteCount(@Param("postId") long postId, @Param("count") Long count);

    /**
     * 更新帖子浏览量
     *
     * @param postId 帖子ID
     * @param viewCount 浏览量
     */
    void updateViewCount(@Param("postId") Long postId, @Param("viewCount") Long viewCount);

    /**
     * 分页查询帖子列表
     */
    List<Post> getPostPageList(@Param("offset") Integer offset,
                               @Param("size") Integer size);

    /**
     * 分页查询分类下的帖子列表
     */
    List<Post> getPostPageByCategory(@Param("categoryId") Long categoryId,
                                     @Param("offset") int offset,
                                     @Param("size") int size);

    /**
     * 分页查询帖子列表（支持排序）
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
     * 获取已发布帖子列表（分页）
     */
    List<Post> getPublishedPostPageList(@Param("offset") Integer offset, @Param("size") Integer size);

    // 删除重复的findByUserId方法，保留一个实现

    /**
     * 根据用户ID列表查询帖子列表
     */
    List<Post> getPostPageListByUserIds(@Param("userIds") List<Long> userIds,
                                        @Param("offset") int offset,
                                        @Param("limit") int limit);

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
                                     @Param("types") List<String> types,
                                     @Param("startTime") java.time.LocalDateTime startTime,
                                     @Param("endTime") java.time.LocalDateTime endTime,
                                     @Param("sortBy") String sortBy,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);
    
    /**
     * 统计支持筛选的搜索结果数量
     */
    Long countSearchResultsWithFilters(@Param("keyword") String keyword,
                                      @Param("types") List<String> types,
                                      @Param("startTime") java.time.LocalDateTime startTime,
                                      @Param("endTime") java.time.LocalDateTime endTime);

    /**
     * 根据帖子类型查询帖子列表
     */
    List<Post> findPostsByType(@Param("type") String type,
                               @Param("offset") int offset,
                               @Param("limit") int limit);
                               
    /**
     * 统计指定类型的帖子数量
     */
    Long countPostsByType(@Param("type") String type);

    /**
     * 查询热门帖子列表
     */
    List<Post> findHotPosts(@Param("offset") int offset,
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
     * 根据问题ID查询回答列表
     */
    List<Post> findAnswersByQuestionId(@Param("questionId") Long questionId,
                                     @Param("offset") int offset,
                                     @Param("limit") int limit);
                                     
    /**
     * 统计所有帖子数量
     */
    Long countAll();
    
    /**
     * 根据帖子类型查找相关帖子（排除指定帖子）
     *
     * @param type 帖子类型
     * @param excludePostId 要排除的帖子ID
     * @param limit 返回的帖子数量限制
     * @return 相关帖子列表
     */
    List<Post> findRelatedPostsByType(@Param("type") String type,
                                     @Param("excludePostId") Long excludePostId,
                                     @Param("limit") int limit);
}