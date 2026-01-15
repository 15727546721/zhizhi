package cn.xu.service.post;

import cn.xu.cache.core.RedisOperations;
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
import cn.xu.support.log.BizLogger;
import cn.xu.support.log.LogConstants;
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
    private final RedisOperations redisOperations;
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
        
        BizLogger.of(log)
                .module(LogConstants.MODULE_POST)
                .op("创建草稿")
                .userId(userId)
                .param("postId", postId)
                .success();
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
        
        BizLogger.of(log)
                .module(LogConstants.MODULE_POST)
                .op("更新草稿")
                .userId(userId)
                .param("postId", postId)
                .success();
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
            BizLogger.of(log)
                    .module(LogConstants.MODULE_USER)
                    .op("增加帖子数")
                    .userId(userId)
                    .success();
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
                    BizLogger.of(log)
                            .module(LogConstants.MODULE_POST)
                            .op("发布事件")
                            .param("postId", finalPostId)
                            .error("发布帖子事件失败", e);
                }
            }
        });

        BizLogger.of(log)
                .module(LogConstants.MODULE_POST)
                .op(LogConstants.OP_PUBLISH)
                .userId(userId)
                .param("postId", postId)
                .param("isNew", isNewPublish)
                .success();
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
        }

        BizLogger.of(log)
                .module(LogConstants.MODULE_POST)
                .op(LogConstants.OP_WITHDRAW)
                .userId(userId)
                .param("postId", postId)
                .param("wasPublished", wasPublished)
                .success();
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
        }

        // 事务提交后发布删除事件
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                try {
                    contentEventPublisher.publishPostDeleted(authorId, postId);
                } catch (Exception e) {
                    BizLogger.of(log)
                            .module(LogConstants.MODULE_POST)
                            .op("删除事件")
                            .param("postId", postId)
                            .error("发布帖子删除事件失败", e);
                }
            }
        });

        BizLogger.of(log)
                .module(LogConstants.MODULE_POST)
                .op(LogConstants.OP_DELETE)
                .userId(userId)
                .param("postId", postId)
                .param("isAdmin", isAdmin)
                .success();
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
        BizLogger.of(log)
                .module(LogConstants.MODULE_POST)
                .op("批量删除")
                .param("count", postIds.size())
                .param("isAdmin", true)
                .success();
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
        BizLogger.of(log)
                .module(LogConstants.MODULE_POST)
                .op("批量删除")
                .userId(userId)
                .param("count", postIds.size())
                .success();
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
        
        BizLogger.of(log)
                .module(LogConstants.MODULE_POST)
                .op("切换加精")
                .param("postId", postId)
                .param("isFeatured", newFeatured)
                .success();
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
        
        BizLogger.of(log)
                .module(LogConstants.MODULE_POST)
                .op("切换状态")
                .param("postId", postId)
                .param("oldStatus", oldStatus)
                .param("newStatus", newStatus)
                .success();
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
            if (!redisOperations.hasKey(ipKey)) {
                shouldIncrement = true;
                redisOperations.set(ipKey, "1", 600);
            }
        } else {
            String userKey = "post:view:user:" + postId + ":" + userId;
            if (!redisOperations.hasKey(userKey)) {
                shouldIncrement = true;
                redisOperations.set(userKey, "1", 600);
            }
        }

        if (shouldIncrement) {
            postRepository.incrementViewCount(postId);
            postRepository.updateHotScore(postId);
        }
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
            
            BizLogger.of(log)
                    .module(LogConstants.MODULE_FILE)
                    .op(LogConstants.OP_UPLOAD)
                    .param("fileName", storedFileName)
                    .param("url", fileUrl)
                    .success();
            return fileUrl;
        } catch (Exception e) {
            BizLogger.of(log)
                    .module(LogConstants.MODULE_FILE)
                    .op(LogConstants.OP_UPLOAD)
                    .error("上传帖子封面失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传封面失败，请稍后重试");
        }
    }

    // ==================== 私有方法 ====================

    private Post getPostOrThrow(Long postId) {
        return postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子不存在"));
    }
}
