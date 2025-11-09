package cn.xu.domain.post.repository;

import cn.xu.api.system.model.dto.post.SysPostQueryRequest;
import cn.xu.api.web.model.vo.post.PostPageResponse;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;

import java.util.List;
import java.util.Optional;

/**
 * 帖子仓储接口
 * 定义帖子数据访问层的操作规范
 */
public interface IPostRepository {
    
    /**
     * 保存帖子聚合根
     *
     * @param aggregate 帖子聚合根
     * @return 帖子ID
     */
    Long save(PostAggregate aggregate);
    
    /**
     * 更新帖子聚合根
     *
     * @param aggregate 帖子聚合根
     */
    void update(PostAggregate aggregate);
    
    /**
     * 根据ID查找帖子聚合根
     *
     * @param id 帖子ID
     * @return 帖子聚合根
     */
    Optional<PostAggregate> findById(Long id);
    
    /**
     * 批量删除帖子
     *
     * @param postIds 帖子ID列表
     */
    void deleteByIds(List<Long> postIds);
    
    /**
     * 获取所有已发布的帖子
     *
     * @return 帖子列表
     */
    List<PostEntity> findAllPublished();
    
    /**
     * 获取所有帖子
     *
     * @return 帖子列表
     */
    List<PostEntity> findAll();
    
    /**
     * 根据用户ID获取帖子列表
     *
     * @param userId 用户ID
     * @return 帖子列表
     */
    List<PostEntity> findByUserId(Long userId);
    
    /**
     * 根据用户ID列表获取帖子列表
     *
     * @param userIds 用户ID列表
     * @param offset  偏移量
     * @param limit   数量
     * @return 帖子列表
     */
    List<PostEntity> findByUserIds(List<Long> userIds, int offset, int limit);
    
    /**
     * 根据用户ID获取草稿列表
     *
     * @param userId 用户ID
     * @return 草稿列表
     */
    List<PostEntity> findDraftsByUserId(Long userId);
    
    /**
     * 增加帖子浏览量
     *
     * @param postId 帖子ID
     */
    void incrementViewCount(Long postId);
    
    /**
     * 更新帖子热度分数
     *
     * @param postId 帖子ID
     */
    void updateHotScore(Long postId);
    
    /**
     * 根据分类ID分页获取帖子列表
     *
     * @param categoryId 分类ID
     * @param offset     偏移量
     * @param limit      数量
     * @return 帖子列表
     */
    List<PostEntity> findByCategoryId(Long categoryId, int offset, int limit);
    
    /**
     * 分页获取所有帖子列表
     *
     * @param offset 偏移量
     * @param limit  数量
     * @return 帖子列表
     */
    List<PostEntity> findAll(int offset, int limit);
    
    /**
     * 分页搜索帖子列表
     *
     * @param title  标题关键词
     * @param offset 偏移量
     * @param limit  数量
     * @return 帖子列表
     */
    List<PostEntity> searchByTitle(String title, int offset, int limit);
    
    /**
     * 统计搜索结果数量
     *
     * @param title 标题关键词
     * @return 结果数量
     */
    long countSearchByTitle(String title);
    
    /**
     * 支持筛选的搜索帖子列表（数据库层面筛选，性能优化）
     *
     * @param keyword   搜索关键词
     * @param types     帖子类型列表（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @param sortBy    排序方式（time/hot/comment/like）
     * @param offset    偏移量
     * @param limit     数量
     * @return 帖子列表
     */
    List<PostEntity> searchByTitleWithFilters(String keyword, java.util.List<String> types,
                                             java.time.LocalDateTime startTime, java.time.LocalDateTime endTime,
                                             String sortBy, int offset, int limit);
    
    /**
     * 统计支持筛选的搜索结果数量
     *
     * @param keyword   搜索关键词
     * @param types     帖子类型列表（可选）
     * @param startTime 开始时间（可选）
     * @param endTime   结束时间（可选）
     * @return 结果数量
     */
    long countSearchByTitleWithFilters(String keyword, java.util.List<String> types,
                                      java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
    
    /**
     * 根据类型分页获取帖子列表
     *
     * @param type   帖子类型
     * @param offset 偏移量
     * @param limit  数量
     * @return 帖子列表
     */
    List<PostEntity> findByType(PostType type, int offset, int limit);
    
    /**
     * 获取热门帖子列表
     *
     * @param offset 偏移量
     * @param limit  数量
     * @return 帖子列表
     */
    List<PostEntity> findHotPosts(int offset, int limit);
    
    /**
     * 根据标签ID分页获取帖子列表
     *
     * @param tagId  标签ID
     * @param offset 偏移量
     * @param limit  数量
     * @return 帖子列表
     */
    List<PostEntity> findByTagId(Long tagId, int offset, int limit);
    
    /**
     * 根据用户ID和状态分页获取帖子列表
     *
     * @param userId     用户ID
     * @param postStatus 帖子状态
     * @param offset     偏移量
     * @param limit      数量
     * @return 帖子列表
     */
    List<PostEntity> findByUserIdAndStatus(Long userId, String postStatus, int offset, int limit);
    
    /**
     * 系统管理分页查询
     *
     * @param postRequest 查询参数
     * @return 查询结果
     */
    List<PostPageResponse> queryByPage(SysPostQueryRequest postRequest);
    
    /**
     * 查询精选帖子列表
     *
     * @param offset 偏移量
     * @param limit  数量
     * @return 帖子列表
     */
    List<PostEntity> findFeaturedPosts(int offset, int limit);
    
    /**
     * 根据ID列表查询帖子列表
     *
     * @param postIds ID列表
     * @return 帖子列表
     */
    List<PostEntity> findPostsByIds(List<Long> postIds);
    
    /**
     * 根据问题ID查询回答列表
     *
     * @param questionId 问题ID
     * @param offset     偏移量
     * @param limit      数量
     * @return 回答列表
     */
    List<PostEntity> findAnswersByQuestionId(Long questionId, int offset, int limit);
    
    /**
     * 统计指定类型的帖子数量
     *
     * @param type 帖子类型
     * @return 帖子数量
     */
    long countByType(PostType type);
    
    /**
     * 统计热门帖子数量
     *
     * @return 帖子数量
     */
    long countHotPosts();
    
    /**
     * 统计指定标签的帖子数量
     *
     * @param tagId 标签ID
     * @return 帖子数量
     */
    long countByTagId(Long tagId);
    
    /**
     * 统计指定用户的帖子数量
     *
     * @param userIds 用户ID列表
     * @return 帖子数量
     */
    long countByUserIds(List<Long> userIds);
    
    /**
     * 统计精选帖子数量
     *
     * @return 帖子数量
     */
    long countFeaturedPosts();
    
    /**
     * 统计所有帖子数量
     *
     * @return 帖子数量
     */
    long countAll();
    
    /**
     * 统计用户已发布帖子数量
     *
     * @param userId 用户ID
     * @return 已发布帖子数量
     */
    long countPublishedByUserId(Long userId);
    
    /**
     * 根据帖子类型查找相关帖子
     *
     * @param postType 帖子类型
     * @param excludePostId 要排除的帖子ID
     * @param limit 返回的帖子数量限制
     * @return 相关帖子列表
     */
    List<PostEntity> findRelatedPostsByType(PostType postType, Long excludePostId, int limit);

}