package cn.xu.service.post;

import cn.xu.cache.RedisService;
import cn.xu.common.ResponseCode;
import cn.xu.integration.file.service.FileStorageService;
import cn.xu.model.entity.Post;
import cn.xu.repository.impl.PostRepository;
import cn.xu.repository.impl.PostRepository.PostWithTags;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * 帖子服务
 * 
 * @author xu
 */
@Slf4j
@Service("postService")
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final PostTagService postTagService;
    private final FileStorageService fileStorageService;
    private final RedisService redisService;
    private final PostMapper postMapper;

    // ==================== 创建与更新 ====================

    /**
     * 创建帖子草稿
     */
    @Transactional(rollbackFor = Exception.class)
    public Long createDraft(Long userId, String title, String content, String description,
                            String coverUrl, List<Long> tagIds) {
        Post post = Post.createDraft(userId, title, content, description);
        post.setCoverUrl(coverUrl);
        Long postId = postRepository.save(post, tagIds);
        log.info("创建帖子草稿成功, userId: {}, postId: {}", userId, postId);
        return postId;
    }

    /**
     * 更新帖子草稿
     */
    @Transactional(rollbackFor = Exception.class)
    public void updateDraft(Long postId, Long userId, String title, String content,
                            String description, String coverUrl, List<Long> tagIds) {
        Post post = getPostOrThrow(postId);
        post.validateOwnership(userId, false);
        post.updateContent(title, content, description);
        if (coverUrl != null) {
            post.setCoverUrl(coverUrl);
        }
        postRepository.update(post, tagIds);
        log.info("更新帖子草稿成功, postId: {}, userId: {}", postId, userId);
    }

    /**
     * 发布帖子
     */
    @Transactional(rollbackFor = Exception.class)
    public Long publishPost(Long postId, Long userId, String title, String content,
                            String description, String coverUrl, List<Long> tagIds) {
        Post post;
        if (postId == null) {
            post = Post.createDraft(userId, title, content, description);
            post.setCoverUrl(coverUrl);
            post.publish();
            postId = postRepository.save(post, tagIds);
        } else {
            post = getPostOrThrow(postId);
            post.validateOwnership(userId, false);
            post.updateContent(title, content, description);
            if (coverUrl != null) {
                post.setCoverUrl(coverUrl);
            }
            post.publish();
            postRepository.update(post, tagIds);
        }
        postRepository.updateHotScore(postId);
        log.info("发布帖子成功, postId: {}, userId: {}", postId, userId);
        return postId;
    }

    /**
     * 撤回帖子
     */
    @Transactional(rollbackFor = Exception.class)
    public void withdrawPost(Long postId, Long userId) {
        Post post = getPostOrThrow(postId);
        post.validateOwnership(userId, false);
        post.withdraw();
        postRepository.update(post, null);
        log.info("撤回帖子成功, postId: {}, userId: {}", postId, userId);
    }

    /**
     * 删除帖子
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        Post post = getPostOrThrow(postId);
        post.validateOwnership(userId, isAdmin);
        post.delete();
        postRepository.update(post, null);
        log.info("删除帖子成功, postId: {}, userId: {}, isAdmin: {}", postId, userId, isAdmin);
    }

    // ==================== 查询方法 ====================

    public Optional<Post> getPostById(Long postId) {
        return postRepository.findById(postId);
    }

    public PostWithTags getPostWithTags(Long postId) {
        return postRepository.findByIdWithTags(postId);
    }

    public List<Post> getUserPosts(Long userId) {
        return postRepository.findByUserId(userId);
    }

    public List<Post> getUserPosts(Long userId, Integer status, int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findByUserIdAndStatus(userId, status, offset, pageSize);
    }

    public List<Post> getUserPostsByStatus(Long userId, Integer status, int pageNo, int pageSize) {
        return getUserPosts(userId, status, pageNo, pageSize);
    }

    public List<Post> getLatestPosts(int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findAll(offset, pageSize);
    }

    public List<Post> getPostsByTag(Long tagId, int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findByTagId(tagId, offset, pageSize);
    }

    public List<Post> getHotPosts(int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findHotPosts(offset, pageSize);
    }

    public List<Post> getAllPosts(int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postRepository.findAll(offset, pageSize);
    }

    public List<Post> getPostsByIds(List<Long> postIds) {
        return postRepository.findByIds(postIds);
    }

    public List<Post> getAllPublishedPosts() {
        return postRepository.findAllPublished();
    }

    public List<Post> getPostsByUserIds(List<Long> userIds, int pageNo, int pageSize) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptyList();
        }
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postMapper.findPostsByUserIds(userIds, offset, pageSize);
    }

    public long countPostsByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return 0L;
        }
        return postMapper.countPostsByUserIds(userIds);
    }

    public long countHotPosts() {
        return postMapper.countHotPosts();
    }

    public List<Post> getHotPostsByTag(Long tagId, int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postMapper.findHotPostsByTagId(tagId, offset, pageSize);
    }

    public long countHotPostsByTag(Long tagId) {
        return postMapper.countHotPostsByTagId(tagId);
    }

    public List<Post> getFeaturedPosts(int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postMapper.findFeaturedPosts(offset, pageSize);
    }

    public long countFeaturedPosts() {
        return postMapper.countFeaturedPosts();
    }

    public List<Post> getFeaturedPostsByTag(Long tagId, int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        return postMapper.findFeaturedPostsByTagId(tagId, offset, pageSize);
    }

    public long countFeaturedPostsByTag(Long tagId) {
        return postMapper.countFeaturedPostsByTagId(tagId);
    }

    // ==================== 统计方法 ====================

    public long countAllPosts() {
        return postRepository.countAll();
    }

    public long countPostsByTag(Long tagId) {
        return postRepository.countByTagId(tagId);
    }

    public long countUserPublishedPosts(Long userId) {
        return postRepository.countPublishedByUserId(userId);
    }

    public long countUserDrafts(Long userId) {
        return postRepository.countDraftsByUserId(userId);
    }

    public long countPublishedByUserId(Long userId) {
        return postMapper.countPublishedByUserId(userId);
    }

    public long countPublishedPosts() {
        return postMapper.countAll();
    }
    
    // ==================== 搜索方法 ====================
    
    /**
     * 搜索帖子
     *
     * @param keyword 关键词
     * @param offset 偏移量
     * @param limit 数量限制
     * @return 帖子列表
     */
    public List<Post> searchPosts(String keyword, int offset, int limit) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return postMapper.searchPosts(keyword.trim(), offset, limit);
        } catch (Exception e) {
            log.error("搜索帖子失败: keyword={}", keyword, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 统计搜索结果数量
     *
     * @param keyword 关键词
     * @return 匹配数量
     */
    public long countSearchPosts(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return 0L;
        }
        try {
            Long count = postMapper.countSearchResults(keyword.trim());
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.error("统计搜索结果失败: keyword={}", keyword, e);
            return 0L;
        }
    }

    // ==================== 互动操作 ====================

    public void viewPost(Long postId, Long userId, String clientIp) {
        if (postId == null) {
            return;
        }
        boolean shouldIncrement = false;
        if (userId == null) {
            String ipKey = "post:view:ip:" + postId + ":" + clientIp;
            if (!redisService.hasKey(ipKey)) {
                shouldIncrement = true;
                redisService.set(ipKey, "1", 600);
            }
        } else {
            String userKey = "post:view:user:" + postId + ":" + userId;
            if (!redisService.hasKey(userKey)) {
                shouldIncrement = true;
                redisService.set(userKey, "1", 600);
            }
        }
        if (shouldIncrement) {
            postRepository.incrementViewCount(postId);
            postRepository.updateHotScore(postId);
        }
    }

    public void updateHotScore(Long postId) {
        if (postId != null) {
            postRepository.updateHotScore(postId);
        }
    }

    public void increaseShareCount(Long postId) {
        Post post = getPostOrThrow(postId);
        post.increaseShareCount();
        postRepository.update(post, null);
        postRepository.updateHotScore(postId);
        log.info("增加帖子分享数成功, postId: {}", postId);
    }

    public void increaseCommentCount(Long postId) {
        if (postId != null) {
            Post post = getPostOrThrow(postId);
            post.increaseCommentCount();
            postRepository.update(post, null);
            postRepository.updateHotScore(postId);
        }
    }

    public void decreaseCommentCount(Long postId) {
        if (postId != null) {
            Post post = getPostOrThrow(postId);
            post.decreaseCommentCount();
            postRepository.update(post, null);
            postRepository.updateHotScore(postId);
        }
    }

    public void increaseLikeCount(Long postId) {
        if (postId != null) {
            Post post = getPostOrThrow(postId);
            post.increaseLikeCount();
            postRepository.update(post, null);
            postRepository.updateHotScore(postId);
        }
    }

    public void decreaseLikeCount(Long postId) {
        if (postId != null) {
            Post post = getPostOrThrow(postId);
            post.decreaseLikeCount();
            postRepository.update(post, null);
            postRepository.updateHotScore(postId);
        }
    }

    public void increaseFavoriteCount(Long postId) {
        if (postId != null) {
            postRepository.increaseFavoriteCount(postId);
            postRepository.updateHotScore(postId);
        }
    }

    public void decreaseFavoriteCount(Long postId) {
        if (postId != null) {
            postRepository.decreaseFavoriteCount(postId);
            postRepository.updateHotScore(postId);
        }
    }

    // ==================== 文件上传 ====================

    public String uploadCover(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "图片文件不能为空");
        }
        try {
            String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            String fileName = "posts/covers/" + datePath + "/" + System.currentTimeMillis() + "_" +
                    Objects.requireNonNull(imageFile.getOriginalFilename());
            String fileUrl = fileStorageService.uploadFile(imageFile, fileName);
            log.info("上传帖子封面成功, fileName: {}, url: {}", fileName, fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("上传帖子封面失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传封面失败: " + e.getMessage());
        }
    }

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

    public Optional<Post> findPostById(Long id) {
        return getPostById(id);
    }

    public List<Post> getUserPosts(Long userId, String status, int page, int size) {
        int offset = Math.max(0, (page - 1) * size);
        return postMapper.findPostsByUserIdAndStatus(userId, status, offset, size);
    }

    /**
     * 批量删除帖子（管理员专用）
     * 
     * @param postIds 帖子ID列表
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeletePosts(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        for (Long postId : postIds) {
            // 管理员批量删除，isAdmin=true 跳过所有权验证
            deletePost(postId, null, true);
        }
        log.info("[帖子服务] 管理员批量删除帖子完成 - count: {}", postIds.size());
    }
    
    /**
     * 批量删除帖子（用户删除自己的帖子）
     * 
     * @param postIds 帖子ID列表
     * @param userId 用户ID（用于验证所有权）
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeletePostsByUser(List<Long> postIds, Long userId) {
        if (postIds == null || postIds.isEmpty() || userId == null) {
            return;
        }
        for (Long postId : postIds) {
            // 用户删除，需要验证所有权
            deletePost(postId, userId, false);
        }
        log.info("[帖子服务] 用户批量删除帖子完成 - userId: {}, count: {}", userId, postIds.size());
    }

    // ==================== 管理操作 ====================

    /**
     * 加精/取消加精帖子
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleFeatured(Long postId) {
        Post post = getPostOrThrow(postId);
        int newFeatured = (post.getIsFeatured() == null || post.getIsFeatured() == 0) ? 1 : 0;
        postMapper.updateFeatured(newFeatured, postId);
        log.info("切换帖子加精状态, postId: {}, isFeatured: {}", postId, newFeatured);
    }

    /**
     * 发布/下架帖子
     */
    @Transactional(rollbackFor = Exception.class)
    public void togglePublish(Long postId) {
        Post post = getPostOrThrow(postId);
        int newStatus;
        if (post.getStatus() == null || post.getStatus() == Post.STATUS_DRAFT) {
            newStatus = Post.STATUS_PUBLISHED;
        } else {
            newStatus = Post.STATUS_DRAFT;
        }
        postMapper.updateStatus(newStatus, postId);
        log.info("切换帖子发布状态, postId: {}, status: {}", postId, newStatus);
    }

    // ==================== 私有辅助方法 ====================

    private Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在"));
    }
}
