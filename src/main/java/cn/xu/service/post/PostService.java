package cn.xu.service.post;

import cn.xu.model.entity.Post;
import cn.xu.repository.impl.PostRepositoryImpl.PostWithTags;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

/**
 * 帖子服务（门面）
 * 
 * 委托给具体的服务类：
 * - PostQueryService: 查询操作
 * - PostCommandService: 增删改操作
 * - PostStatisticsService: 统计操作
 * 
 * 保留此类是为了向后兼容，新代码建议直接注入具体的服务类
 */
@Slf4j
@Service("postService")
@RequiredArgsConstructor
public class PostService {

    private final PostQueryService queryService;
    private final PostCommandService commandService;
    private final PostStatisticsService statisticsService;

    // ==================== 创建与更新（委托给 CommandService）====================

    @Transactional(rollbackFor = Exception.class)
    public Long createDraft(Long userId, String title, String content, String description,
                            String coverUrl, List<Long> tagIds) {
        return commandService.createDraft(userId, title, content, description, coverUrl, tagIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateDraft(Long postId, Long userId, String title, String content,
                            String description, String coverUrl, List<Long> tagIds) {
        commandService.updateDraft(postId, userId, title, content, description, coverUrl, tagIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public Long publishPost(Long postId, Long userId, String title, String content,
                            String description, String coverUrl, List<Long> tagIds) {
        return commandService.publishPost(postId, userId, title, content, description, coverUrl, tagIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void withdrawPost(Long postId, Long userId) {
        commandService.withdrawPost(postId, userId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        commandService.deletePost(postId, userId, isAdmin);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeletePosts(List<Long> postIds) {
        commandService.batchDelete(postIds);
    }

    @Transactional(rollbackFor = Exception.class)
    public void batchDeletePostsByUser(List<Long> postIds, Long userId) {
        commandService.batchDeleteByUser(postIds, userId);
    }

    // ==================== 查询方法（委托给 QueryService）====================

    public Optional<Post> getPostById(Long postId) {
        return queryService.getById(postId);
    }

    public PostWithTags getPostWithTags(Long postId) {
        return queryService.getWithTags(postId);
    }

    public List<Post> getUserPosts(Long userId) {
        return queryService.getByUserId(userId);
    }

    public List<Post> getUserPosts(Long userId, Integer status, int pageNo, int pageSize) {
        return queryService.getByUserIdAndStatus(userId, status, pageNo, pageSize);
    }

    public List<Post> getUserPostsByStatus(Long userId, Integer status, int pageNo, int pageSize) {
        return queryService.getByUserIdAndStatus(userId, status, pageNo, pageSize);
    }

    public List<Post> getUserPostsWithKeyword(Long userId, Integer status, String keyword, int pageNo, int pageSize) {
        return queryService.getByUserIdWithKeyword(userId, status, keyword, pageNo, pageSize);
    }

    public List<Post> getLatestPosts(int pageNo, int pageSize) {
        return queryService.getAll(pageNo, pageSize);
    }

    public List<Post> getPostsByTag(Long tagId, int pageNo, int pageSize) {
        return queryService.getByTagId(tagId, pageNo, pageSize);
    }

    public List<Post> getHotPosts(int pageNo, int pageSize) {
        return queryService.getHotPosts(pageNo, pageSize);
    }

    public List<Post> getAllPosts(int pageNo, int pageSize) {
        return queryService.getAll(pageNo, pageSize);
    }

    public List<Post> getPostsByIds(List<Long> postIds) {
        return queryService.getByIds(postIds);
    }

    public List<Post> getAllPublishedPosts() {
        return queryService.getAllPublished();
    }

    public List<Post> getPostsByUserIds(List<Long> userIds, int pageNo, int pageSize) {
        return queryService.getByUserIds(userIds, pageNo, pageSize);
    }

    public List<Post> getHotPostsByTag(Long tagId, int pageNo, int pageSize) {
        return queryService.getHotPostsByTag(tagId, pageNo, pageSize);
    }

    public List<Post> getFeaturedPosts(int pageNo, int pageSize) {
        return queryService.getFeaturedPosts(pageNo, pageSize);
    }

    public List<Post> getFeaturedPostsByTag(Long tagId, int pageNo, int pageSize) {
        return queryService.getFeaturedPostsByTag(tagId, pageNo, pageSize);
    }

    public List<Post> getPostsByFavoriteCount(int limit) {
        return queryService.getByFavoriteCount(limit);
    }

    public List<Post> getUserPosts(Long userId, String status, int page, int size) {
        Integer statusCode = null;
        if ("PUBLISHED".equals(status)) {
            statusCode = Post.STATUS_PUBLISHED;
        } else if ("DRAFT".equals(status)) {
            statusCode = Post.STATUS_DRAFT;
        }
        return queryService.getByUserIdAndStatus(userId, statusCode, page, size);
    }

    public Optional<Post> findPostById(Long id) {
        return getPostById(id);
    }

    // ==================== 统计方法（委托给 StatisticsService）====================

    public long countAllPosts() {
        return statisticsService.countAll();
    }

    public long countPostsByTag(Long tagId) {
        return statisticsService.countByTagId(tagId);
    }

    public long countUserPublishedPosts(Long userId) {
        return statisticsService.countPublishedByUserId(userId);
    }

    public long countUserDrafts(Long userId) {
        return statisticsService.countDraftsByUserId(userId);
    }

    public long countUserPostsWithKeyword(Long userId, Integer status, String keyword) {
        return statisticsService.countByUserIdWithKeyword(userId, status, keyword);
    }

    public long countPostsByUserIds(List<Long> userIds) {
        return statisticsService.countByUserIds(userIds);
    }

    public long countHotPosts() {
        return statisticsService.countHot();
    }

    public long countHotPostsByTag(Long tagId) {
        return statisticsService.countHotByTagId(tagId);
    }

    public long countFeaturedPosts() {
        return statisticsService.countFeatured();
    }

    public long countFeaturedPostsByTag(Long tagId) {
        return statisticsService.countFeaturedByTagId(tagId);
    }

    public long countPublishedByUserId(Long userId) {
        return statisticsService.countPublishedByUserId(userId);
    }

    public long countPublishedPosts() {
        return statisticsService.countPublished();
    }

    // ==================== 搜索方法 ====================

    public List<Post> searchPosts(String keyword, int offset, int limit) {
        return queryService.search(keyword, offset, limit);
    }

    public long countSearchPosts(String keyword) {
        return statisticsService.countSearch(keyword);
    }

    // ==================== 互动操作（委托给 CommandService）====================

    public void viewPost(Long postId, Long userId, String clientIp) {
        commandService.viewPost(postId, userId, clientIp);
    }

    public void updateHotScore(Long postId) {
        commandService.updateHotScore(postId);
    }

    public void increaseShareCount(Long postId) {
        commandService.increaseShareCount(postId);
    }

    public void increaseCommentCount(Long postId) {
        commandService.increaseCommentCount(postId);
    }

    public void decreaseCommentCount(Long postId) {
        commandService.decreaseCommentCount(postId);
    }

    public void increaseLikeCount(Long postId) {
        commandService.increaseLikeCount(postId);
    }

    public void decreaseLikeCount(Long postId) {
        commandService.decreaseLikeCount(postId);
    }

    public void increaseFavoriteCount(Long postId) {
        commandService.increaseFavoriteCount(postId);
    }

    public void decreaseFavoriteCount(Long postId) {
        commandService.decreaseFavoriteCount(postId);
    }

    // ==================== 管理操作（委托给 CommandService）====================

    @Transactional(rollbackFor = Exception.class)
    public void toggleFeatured(Long postId) {
        commandService.toggleFeatured(postId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void togglePublish(Long postId) {
        commandService.togglePublish(postId);
    }

    // ==================== 文件上传 ====================

    public String uploadCover(MultipartFile imageFile) {
        return commandService.uploadCover(imageFile);
    }

    // ==================== 兼容方法 ====================

    @Transactional(rollbackFor = Exception.class)
    public Long createPost(Post post) {
        if (post.getStatus() == null || Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
            return publishPost(null, post.getUserId(), post.getTitle(),
                    post.getContent(), post.getDescription(), post.getCoverUrl(), null);
        } else {
            return createDraft(post.getUserId(), post.getTitle(),
                    post.getContent(), post.getDescription(), post.getCoverUrl(), null);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void updatePost(Post post) {
        if (Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus())) {
            publishPost(post.getId(), post.getUserId(), post.getTitle(),
                    post.getContent(), post.getDescription(), post.getCoverUrl(), null);
        } else {
            updateDraft(post.getId(), post.getUserId(), post.getTitle(),
                    post.getContent(), post.getDescription(), post.getCoverUrl(), null);
        }
    }
}
