package cn.xu.service.post;

import cn.xu.cache.RedisService;
import cn.xu.common.ResponseCode;
import cn.xu.event.publisher.ContentEventPublisher;
import cn.xu.integration.file.service.FileStorageService;
import cn.xu.model.entity.Post;
import cn.xu.repository.impl.PostRepository;
import cn.xu.repository.impl.PostRepository.PostWithTags;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.repository.mapper.UserMapper;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 帖子服务
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
    private final UserMapper userMapper;
    private final ContentEventPublisher contentEventPublisher;

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
        log.info("[帖子] 创建草稿成功, userId: {}, postId: {}", userId, postId);
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
        log.info("[帖子] 更新草稿成功, postId: {}, userId: {}", postId, userId);
    }

    /**
     * 发布帖子
     */
    @Transactional(rollbackFor = Exception.class)
    public Long publishPost(Long postId, Long userId, String title, String content,
                            String description, String coverUrl, List<Long> tagIds) {
        Post post;
        boolean isNewPublish = false; // 是否是新发布（用于判断是否增加用户帖子数）
        
        if (postId == null) {
            // 新建并发布
            post = Post.createDraft(userId, title, content, description);
            post.setCoverUrl(coverUrl);
            post.publish();
            postId = postRepository.save(post, tagIds);
            isNewPublish = true;
        } else {
            post = getPostOrThrow(postId);
            post.validateOwnership(userId, false);
            // 记录发布前的状态
            boolean wasPublished = Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus());
            post.updateContent(title, content, description);
            if (coverUrl != null) {
                post.setCoverUrl(coverUrl);
            }
            post.publish();
            postRepository.update(post, tagIds);
            // 只有从草稿变为发布才算新发布
            isNewPublish = !wasPublished;
        }
        postRepository.updateHotScore(postId);
        
        // 更新用户帖子数（仅新发布时）
        if (isNewPublish) {
            userMapper.increasePostCount(userId);
            log.info("增加用户帖子数, userId: {}", userId);
        }
        
        // 事务提交后发布事件（同步ES等）
        final Long finalPostId = postId;
        final String finalTitle = title;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    contentEventPublisher.publishPostCreated(userId, finalPostId, finalTitle);
                    log.info("发布帖子事件成功, postId: {}", finalPostId);
                } catch (Exception e) {
                    log.error("发布帖子事件失败, postId: {}", finalPostId, e);
                }
            }
        });
        
        log.info("发布帖子成功, postId: {}, userId: {}, isNewPublish: {}", postId, userId, isNewPublish);
        return postId;
    }

    /**
     * 撤回帖子
     */
    @Transactional(rollbackFor = Exception.class)
    public void withdrawPost(Long postId, Long userId) {
        Post post = getPostOrThrow(postId);
        post.validateOwnership(userId, false);
        
        // 记录撤回前的状态
        boolean wasPublished = Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus());
        
        post.withdraw();
        postRepository.update(post, null);
        
        // 减少用户帖子数（仅已发布的帖子撤回时）
        if (wasPublished) {
            userMapper.decreasePostCount(userId);
            log.info("撤回帖子，减少用户帖子数, userId: {}", userId);
        }
        
        log.info("撤回帖子成功, postId: {}, userId: {}", postId, userId);
    }

    /**
     * 删除帖子
     */
    @Transactional(rollbackFor = Exception.class)
    public void deletePost(Long postId, Long userId, boolean isAdmin) {
        Post post = getPostOrThrow(postId);
        post.validateOwnership(userId, isAdmin);
        
        // 记录删除前的状态，用于判断是否减少用户帖子数
        boolean wasPublished = Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus());
        Long authorId = post.getUserId();
        
        post.delete();
        postRepository.update(post, null);
        
        // 减少用户帖子数（仅已发布的帖子）
        if (wasPublished) {
            userMapper.decreasePostCount(authorId);
            log.info("减少用户帖子数, userId: {}", authorId);
        }
        
        // 事务提交后发布删除事件（同步ES等）
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    contentEventPublisher.publishPostDeleted(authorId, postId);
                    log.info("发布帖子删除事件成功, postId: {}", postId);
                } catch (Exception e) {
                    log.error("发布帖子删除事件失败, postId: {}", postId, e);
                }
            }
        });
        
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

    /**
     * 按收藏数获取帖子列表
     */
    public List<Post> getPostsByFavoriteCount(int limit) {
        return postMapper.findPostsByFavoriteCount(limit);
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
            // 生成安全的文件路径（格式: yyyyMM/uuid.ext）
            String fileName = cn.xu.common.constants.FilePathConstants.buildPath(
                    imageFile.getOriginalFilename());
            // 上传文件（返回存储的文件名）
            String storedFileName = fileStorageService.uploadFile(imageFile, fileName);
            // 获取完整的访问URL
            String fileUrl = fileStorageService.getFileUrl(storedFileName);
            log.info("上传帖子封面成功, storedFileName: {}, fileUrl: {}", storedFileName, fileUrl);
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
     * 发布/下架帖子（管理员后台使用）
     */
    @Transactional(rollbackFor = Exception.class)
    public void togglePublish(Long postId) {
        Post post = getPostOrThrow(postId);
        int oldStatus = post.getStatus() == null ? Post.STATUS_DRAFT : post.getStatus();
        int newStatus;
        
        if (oldStatus == Post.STATUS_DRAFT) {
            newStatus = Post.STATUS_PUBLISHED;
            // 草稿→发布：增加用户帖子数
            userMapper.increasePostCount(post.getUserId());
            log.info("管理员发布帖子，增加用户帖子数, userId: {}", post.getUserId());
        } else {
            newStatus = Post.STATUS_DRAFT;
            // 发布→草稿：减少用户帖子数
            userMapper.decreasePostCount(post.getUserId());
            log.info("管理员下架帖子，减少用户帖子数, userId: {}", post.getUserId());
        }
        
        postMapper.updateStatus(newStatus, postId);
        log.info("切换帖子发布状态, postId: {}, oldStatus: {}, newStatus: {}", postId, oldStatus, newStatus);
    }

    // ==================== 私有辅助方法 ====================

    private Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在"));
    }
}
