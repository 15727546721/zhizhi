package cn.xu.service.comment;

import cn.xu.cache.service.CacheService;
import cn.xu.event.events.CommentCreatedInternalEvent;
import cn.xu.event.publisher.SocialEventPublisher;
import cn.xu.model.dto.comment.SaveCommentRequest;
import cn.xu.model.entity.Comment;
import cn.xu.model.enums.CommentType;
import cn.xu.repository.CommentRepository;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.repository.mapper.UserMapper;
import cn.xu.service.file.FileManagementService;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 评论命令服务
 * <p>负责评论的写操作（创建、删除）</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final CommentRepository commentRepository;
    private final SocialEventPublisher socialEventPublisher;
    private final FileManagementService fileManagementService;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final CacheService cacheService;

    private static final String COMMENT_HOT_PAGE_KEY_PREFIX = "comment:hot:page:";

    // ==================== 创建评论 ====================

    /**
     * 保存评论
     * <p>事务内只做数据库操作，通知等异步操作在事务提交后执行</p>
     */
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(SaveCommentRequest request) {
        Comment comment = buildComment(request);
        Long commentId = commentRepository.save(comment);
        comment.setId(commentId);

        // 更新热度分数
        updateHotScore(comment);
        commentRepository.update(comment);

        // 更新父评论回复数
        if (request.getParentId() != null && request.getParentId() > 0) {
            commentRepository.incrementReplyCount(request.getParentId());
            log.info("[评论] 增加父评论回复数 - parentId: {}", request.getParentId());
        }

        // 标记图片为正式文件
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            fileManagementService.markFilesAsPermanent(request.getImageUrls());
            log.info("[评论] 图片已标记为正式 - commentId: {}, count: {}", commentId, request.getImageUrls().size());
        }

        // 更新帖子评论数
        if (request.getTargetType() != null && request.getTargetType().equals(CommentType.POST.getValue())) {
            postMapper.updateCommentCount(request.getTargetId(), 1);
            log.info("[评论] 增加帖子评论数 - postId: {}", request.getTargetId());
        }

        // 更新用户评论数
        userMapper.increaseCommentCount(request.getUserId());

        // 发布内部事件（事务提交后处理通知、缓存等）
        applicationEventPublisher.publishEvent(new CommentCreatedInternalEvent(this, request, commentId));

        return commentId;
    }

    // ==================== 删除评论 ====================

    /**
     * 删除评论（带权限校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteWithPermission(Long commentId, Long operatorId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }

        // 权限校验：评论作者 或 帖子作者 可删除
        boolean isCommentOwner = operatorId.equals(comment.getUserId());
        boolean isPostOwner = false;
        if (comment.getTargetType() == CommentType.POST.getValue()) {
            cn.xu.model.entity.Post post = postMapper.findById(comment.getTargetId());
            if (post != null) {
                isPostOwner = operatorId.equals(post.getUserId());
            }
        }

        if (!isCommentOwner && !isPostOwner) {
            throw new BusinessException("无权删除该评论");
        }

        doDelete(comment);
    }

    /**
     * 管理员删除评论
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        doDelete(comment);
        log.info("[评论] 管理员删除成功 - commentId: {}", commentId);
    }

    /**
     * 执行删除操作
     * <p>先删除再统计，使用实际删除数量更新计数</p>
     */
    private void doDelete(Comment comment) {
        Long commentId = comment.getId();
        Integer targetType = comment.getTargetType();
        Long targetId = comment.getTargetId();

        // 更新父评论的回复数
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentRepository.decrementReplyCount(comment.getParentId());
        }

        // 清理评论图片
        cleanupCommentImages(comment);

        // 清理子评论图片并更新用户评论数
        List<Comment> children = commentRepository.findByParentId(commentId);
        for (Comment child : children) {
            cleanupCommentImages(child);
            userMapper.decreaseCommentCount(child.getUserId());
        }

        // 先删除子评论，获取实际删除数量
        int childDeleteCount = commentRepository.deleteByParentId(commentId);
        
        // 再删除主评论
        commentRepository.deleteById(commentId);
        
        // 用实际删除数量更新帖子评论数
        int totalDeleted = 1 + childDeleteCount;
        if (targetType != null && targetType == CommentType.POST.getValue()) {
            postMapper.updateCommentCount(targetId, -totalDeleted);
        }

        // 更新用户评论数
        userMapper.decreaseCommentCount(comment.getUserId());

        // 发布删除事件
        socialEventPublisher.publishCommentDeleted(comment.getUserId(), targetId, commentId);

        // 清除缓存
        clearCommentCache(targetType, targetId);

        log.info("[评论] 删除完成 - commentId: {}, 共删除: {}条", commentId, totalDeleted);
    }

    /**
     * 清除评论缓存
     */
    private void clearCommentCache(Integer targetType, Long targetId) {
        if (targetType == null || targetId == null) {
            return;
        }
        try {
            String pattern = String.format("%s%d:%d:*", COMMENT_HOT_PAGE_KEY_PREFIX, targetType, targetId);
            cacheService.evictByPattern(pattern);
            log.debug("[评论缓存] 清除缓存 - pattern: {}", pattern);
        } catch (Exception e) {
            log.warn("[评论缓存] 清除缓存失败 - targetType: {}, targetId: {}", targetType, targetId, e);
        }
    }

    // ==================== 私有方法 ====================

    private Comment buildComment(SaveCommentRequest request) {
        Comment comment = new Comment();
        comment.setTargetType(Integer.valueOf(request.getTargetType()));
        comment.setTargetId(request.getTargetId());
        comment.setParentId(request.getParentId());
        comment.setUserId(request.getUserId());
        comment.setContent(request.getContent());
        comment.setImageUrls(request.getImageUrls());
        comment.setReplyUserId(request.getReplyUserId());
        comment.setLikeCount(0L);
        comment.setReplyCount(0L);
        comment.setHotScore(java.math.BigDecimal.ZERO);  // 初始化热度分数
        comment.setCreateTime(LocalDateTime.now());
        return comment;
    }

    private void updateHotScore(Comment comment) {
        // 初始热度分数计算
        comment.setHotScore(java.math.BigDecimal.ZERO);
    }

    private void cleanupCommentImages(Comment comment) {
        if (comment.getImageUrls() != null && !comment.getImageUrls().isEmpty()) {
            fileManagementService.deleteFiles(comment.getImageUrls());
            log.info("[评论] 删除图片文件 - commentId: {}", comment.getId());
        }
    }
}
