package cn.xu.service.post;

import cn.xu.cache.RedisService;
import cn.xu.common.ResponseCode;
import cn.xu.common.constants.BooleanConstants;
import cn.xu.common.constants.FilePathConstants;
import cn.xu.event.publisher.ContentEventPublisher;
import cn.xu.integration.file.service.FileStorageService;
import cn.xu.model.entity.Post;
import cn.xu.repository.PostRepository;
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

import java.util.List;

/**
 * 帖子命令服务
 * 负责所有帖子的增删改操作
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PostCommandService {

    private final PostRepository postRepository;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final RedisService redisService;
    private final FileStorageService fileStorageService;
    private final ContentEventPublisher contentEventPublisher;

    // ==================== 创建操作 ====================

    /**
     * 创建草稿
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
     * 更新草稿
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
        boolean isNewPublish = false;

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
            boolean wasPublished = Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus());
            post.updateContent(title, content, description);
            if (coverUrl != null) {
                post.setCoverUrl(coverUrl);
            }
            post.publish();
            postRepository.update(post, tagIds);
            isNewPublish = !wasPublished;
        }

        postRepository.updateHotScore(postId);

        // 更新用户帖子数（仅新发布时）
        if (isNewPublish) {
            userMapper.increasePostCount(userId);
            log.info("增加用户帖子数, userId: {}", userId);
        }

        // 事务提交后发布事件
        final Long finalPostId = postId;
        final String finalTitle = title;
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    contentEventPublisher.publishPostCreated(userId, finalPostId, finalTitle);
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

        boolean wasPublished = Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus());
        post.withdraw();
        postRepository.update(post, null);

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

        boolean wasPublished = Integer.valueOf(Post.STATUS_PUBLISHED).equals(post.getStatus());
        Long authorId = post.getUserId();

        post.delete();
        postRepository.update(post, null);

        if (wasPublished) {
            userMapper.decreasePostCount(authorId);
            log.info("减少用户帖子数, userId: {}", authorId);
        }

        // 事务提交后发布删除事件
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    contentEventPublisher.publishPostDeleted(authorId, postId);
                } catch (Exception e) {
                    log.error("发布帖子删除事件失败, postId: {}", postId, e);
                }
            }
        });

        log.info("删除帖子成功, postId: {}, userId: {}, isAdmin: {}", postId, userId, isAdmin);
    }

    /**
     * 批量删除帖子（管理员）
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDelete(List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return;
        }
        for (Long postId : postIds) {
            deletePost(postId, null, true);
        }
        log.info("[帖子服务] 管理员批量删除帖子完成 - count: {}", postIds.size());
    }

    /**
     * 批量删除帖子（用户）
     */
    @Transactional(rollbackFor = Exception.class)
    public void batchDeleteByUser(List<Long> postIds, Long userId) {
        if (postIds == null || postIds.isEmpty() || userId == null) {
            return;
        }
        for (Long postId : postIds) {
            deletePost(postId, userId, false);
        }
        log.info("[帖子服务] 用户批量删除帖子完成 - userId: {}, count: {}", userId, postIds.size());
    }

    // ==================== 管理操作 ====================

    /**
     * 切换加精状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void toggleFeatured(Long postId) {
        Post post = getPostOrThrow(postId);
        int newFeatured = BooleanConstants.toggle(post.getIsFeatured());
        postMapper.updateFeatured(newFeatured, postId);
        log.info("切换帖子加精状态, postId: {}, isFeatured: {}", postId, newFeatured);
    }

    /**
     * 切换发布状态
     */
    @Transactional(rollbackFor = Exception.class)
    public void togglePublish(Long postId) {
        Post post = getPostOrThrow(postId);
        int oldStatus = post.getStatus() == null ? Post.STATUS_DRAFT : post.getStatus();
        int newStatus;

        if (oldStatus == Post.STATUS_DRAFT) {
            newStatus = Post.STATUS_PUBLISHED;
            userMapper.increasePostCount(post.getUserId());
        } else {
            newStatus = Post.STATUS_DRAFT;
            userMapper.decreasePostCount(post.getUserId());
        }

        postMapper.updateStatus(newStatus, postId);
        log.info("切换帖子发布状态, postId: {}, oldStatus: {}, newStatus: {}", postId, oldStatus, newStatus);
    }

    // ==================== 互动操作 ====================

    /**
     * 浏览帖子（带防刷）
     */
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

    public void updateHotScore(Long postId) {
        if (postId != null) {
            postRepository.updateHotScore(postId);
        }
    }

    // ==================== 文件上传 ====================

    public String uploadCover(MultipartFile imageFile) {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "图片文件不能为空");
        }
        try {
            String fileName = FilePathConstants.buildPath(
                    imageFile.getOriginalFilename());
            String storedFileName = fileStorageService.uploadFile(imageFile, fileName);
            String fileUrl = fileStorageService.getFileUrl(storedFileName);
            log.info("上传帖子封面成功, storedFileName: {}, fileUrl: {}", storedFileName, fileUrl);
            return fileUrl;
        } catch (Exception e) {
            log.error("上传帖子封面失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传封面失败: " + e.getMessage());
        }
    }

    // ==================== 私有方法 ====================

    private Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在"));
    }
}
