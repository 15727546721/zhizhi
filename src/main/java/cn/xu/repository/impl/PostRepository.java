package cn.xu.repository.impl;

import cn.xu.cache.RedisService;
import cn.xu.common.ResponseCode;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.PostTag;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.repository.mapper.PostTagMapper;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 帖子仓储实现
 * <p>负责帖子的持久化操作</p>
 
 */
@Slf4j
@Repository("postRepository")
@RequiredArgsConstructor
public class PostRepository {

    private final PostMapper postMapper;
    private final PostTagMapper postTagMapper;
    private final RedisService redisService;

    private static final String HOT_SCORE_KEY_PREFIX = "post:hot:score:";

    // ==================== 基础CRUD ====================

    /**
     * 保存帖子（包含标签）
     * @param post 帖子对象
     * @param tagIds 标签ID列表
     * @return 帖子ID
     */
    @Transactional(rollbackFor = Exception.class)
    public Long save(Post post, List<Long> tagIds) {
        if (post == null) {
            log.warn("保存帖子参数错误 - post为null");
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子对象不能为空");
        }

        // 验证帖子完整性
        post.validateForCreation();

        // 插入帖子
        postMapper.insert(post);
        Long postId = post.getId();

        // 保存标签关联
        if (tagIds != null && !tagIds.isEmpty()) {
            savePostTags(postId, tagIds);
        }

        log.info("保存帖子成功 - ID: {}, 标签数量: {}", postId, tagIds == null ? 0 : tagIds.size());
        return postId;
    }

    /**
     * 更新帖子（包含标签）
     * @param post 帖子对象
     * @param tagIds 标签ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void update(Post post, List<Long> tagIds) {
        if (post == null || post.getId() == null) {
            log.warn("更新帖子参数错误 - post为null或id为null");
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子对象或ID不能为空");
        }

        Long postId = post.getId();

        // 更新帖子
        postMapper.update(post);

        // 更新标签关联
        if (tagIds != null) {
            // 删除旧的标签关联
            postTagMapper.deleteByPostId(postId);

            // 添加新的标签关联
            if (!tagIds.isEmpty()) {
                savePostTags(postId, tagIds);
            }
        }

        // 更新热度分数
        updateHotScore(postId);

        log.info("更新帖子成功 - ID: {}", postId);
    }

    /**
     * 根据ID查找帖子（包含标签）
     * @param postId 帖子ID
     * @return 帖子对象（Optional）
     */
    public Optional<Post> findById(Long postId) {
        if (postId == null) {
            return Optional.empty();
        }

        Post post = postMapper.findById(postId);
        return Optional.ofNullable(post);
    }

    /**
     * 根据ID查找帖子及其标签ID列表
     * @param postId 帖子ID
     * @return 帖子与标签组合对象
     */
    public PostWithTags findByIdWithTags(Long postId) {
        Optional<Post> postOpt = findById(postId);
        if (!postOpt.isPresent()) {
            return null;
        }

        List<Long> tagIds = postTagMapper.selectTagIdsByPostId(postId);
        return new PostWithTags(postOpt.get(), tagIds);
    }

    /**
     * 批量删除帖子
     * @param postIds 帖子ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }

        // 删除标签关联
        postTagMapper.deleteByPostIds(postIds);

        // 删除帖子
        postMapper.deleteByIds(postIds);

        log.info("批量删除帖子成功 - IDs: {}", postIds);
    }

    // ==================== 查询方法 ====================

    /**
     * 获取所有已发布的帖子
     */
    public List<Post> findAllPublished() {
        return postMapper.findAllPublishedPosts();
    }

    /**
     * 根据用户ID获取帖子列表
     */
    public List<Post> findByUserId(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return postMapper.findByUserId(userId);
    }

    /**
     * 根据用户ID和状态获取帖子列表
     */
    public List<Post> findByUserIdAndStatus(Long userId, Integer status, int offset, int limit) {
        if (userId == null) {
            return Collections.emptyList();
        }

        String statusStr = status != null ? String.valueOf(status) : null;
        return postMapper.findPostsByUserIdAndStatus(userId, statusStr, offset, limit);
    }

    /**
     * 根据标签ID分页获取帖子
     */
    public List<Post> findByTagId(Long tagId, int offset, int limit) {
        if (tagId == null) {
            return Collections.emptyList();
        }
        return postMapper.findPostsByTagId(tagId, offset, limit);
    }

    /**
     * 分页获取所有帖子
     */
    public List<Post> findAll(int offset, int limit) {
        return postMapper.getPostPageList(offset, limit);
    }

    /**
     * 分页获取热门帖子
     */
    public List<Post> findHotPosts(int offset, int limit) {
        return postMapper.findHotPosts(offset, limit);
    }

    /**
     * 根据ID列表批量查询帖子
     */
    public List<Post> findByIds(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return postMapper.findPostsByIds(postIds);
    }

    /**
     * 根据用户ID列表查询帖子（用于关注动态Feed）
     */
    public List<Post> findByUserIds(List<Long> userIds, int offset, int limit) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        return postMapper.findPostsByUserIds(userIds, offset, limit);
    }

    /**
     * 统计指定用户ID列表的帖子数量
     */
    public long countByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0L;
        }
        Long count = postMapper.countPostsByUserIds(userIds);
        return count != null ? count : 0L;
    }

    // ==================== 统计方法 ====================

    /**
     * 统计所有帖子数量
     */
    public long countAll() {
        Long count = postMapper.countAll();
        return count != null ? count : 0L;
    }

    /**
     * 统计指定标签ID的帖子数量
     */
    public long countByTagId(Long tagId) {
        if (tagId == null) {
            return 0L;
        }
        Long count = postMapper.countPostsByTagId(tagId);
        return count != null ? count : 0L;
    }

    /**
     * 统计指定用户ID的已发布帖子数量
     */
    public long countPublishedByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long count = postMapper.countPublishedByUserId(userId);
        return count != null ? count : 0L;
    }

    /**
     * 统计指定用户ID的草稿数量
     */
    public long countDraftsByUserId(Long userId) {
        if (userId == null) {
            return 0L;
        }
        Long count = postMapper.countDraftsByUserId(userId);
        return count != null ? count : 0L;
    }

    // ==================== 计数更新 ====================

    /**
     * 增加浏览量
     */
    public void incrementViewCount(Long postId) {
        if (postId != null) {
            // 获取当前浏览量并+1
            Post post = postMapper.findById(postId);
            if (post != null) {
                Long newViewCount = (post.getViewCount() == null ? 0 : post.getViewCount()) + 1;
                postMapper.updateViewCount(postId, newViewCount);
            }
        }
    }

    /**
     * 增加收藏量
     */
    public void increaseFavoriteCount(Long postId) {
        if (postId != null) {
            Post post = postMapper.findById(postId);
            if (post != null) {
                post.increaseFavoriteCount();
                postMapper.update(post);
            }
        }
    }

    /**
     * 减少收藏量
     */
    public void decreaseFavoriteCount(Long postId) {
        if (postId != null) {
            Post post = postMapper.findById(postId);
            if (post != null) {
                post.decreaseFavoriteCount();
                postMapper.update(post);
            }
        }
    }

    /**
     * 更新热度分数
     */
    public void updateHotScore(Long postId) {
        if (postId == null) {
            return;
        }

        // 从数据库获取帖子数据
        Post post = postMapper.findById(postId);
        if (post == null) {
            return;
        }

        // 计算热度分数（Reddit算法）
        double hotScore = calculateHotScore(post);

        // 更新到Redis（用于热门排序）
        try {
            redisService.zSetAdd("post:hot:ranking", postId.toString(), hotScore);
        } catch (Exception e) {
            log.warn("更新Redis热度排名失败 - postId: {}", postId, e);
        }

        log.debug("更新帖子热度分数 - postId: {}, score: {}", postId, hotScore);
    }

    /**
     * 计算热度分数（Reddit Hot算法）
     */
    private double calculateHotScore(Post post) {
        if (post == null) {
            return 0.0;
        }

        // 点赞数量
        long upvotes = post.getLikeCount() != null ? post.getLikeCount() : 0;
        long score = upvotes;

        // 评论加权（每个评论相当于0.5个赞）
        long comments = post.getCommentCount() != null ? post.getCommentCount() : 0;
        score += comments / 2;

        // 收藏加权（每个收藏相当于2个赞）
        long favorites = post.getFavoriteCount() != null ? post.getFavoriteCount() : 0;
        score += favorites * 2;

        // 计算时间衰减（小时为单位）
        long hoursSincePublish = 0;
        if (post.getCreateTime() != null) {
            hoursSincePublish = java.time.Duration.between(
                    post.getCreateTime(),
                    java.time.LocalDateTime.now()
            ).toHours();
        }

        // Reddit Hot算法：log10(score) - (hours / 12.5)
        double hotScore = Math.log10(Math.max(1, score)) - (hoursSincePublish / 12.5);

        return hotScore;
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 保存帖子标签关联
     */
    private void savePostTags(Long postId, List<Long> tagIds) {
        if (tagIds == null || tagIds.isEmpty()) {
            return;
        }

        // 验证标签数量
        if (tagIds.size() > 10) {
            log.warn("帖子标签数量超限 - 当前数量: {}", tagIds.size());
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "帖子标签数量不能超过10个");
        }

        List<PostTag> postTags = tagIds.stream()
                .map(tagId -> PostTag.builder()
                        .postId(postId)
                        .tagId(tagId)
                        .build())
                .collect(Collectors.toList());

        postTagMapper.insertBatchByList(postTags);
    }

    /**
     * 帖子与标签的组合对象
     */
    public static class PostWithTags {
        private final Post post;
        private final List<Long> tagIds;

        public PostWithTags(Post post, List<Long> tagIds) {
            this.post = post;
            this.tagIds = tagIds;
        }

        public Post getPost() {
            return post;
        }

        public List<Long> getTagIds() {
            return tagIds;
        }
    }
}
