package cn.xu.service.comment;

import cn.xu.common.constant.CommentSortType;
import cn.xu.common.constant.CommentType;
import cn.xu.event.publisher.SocialEventPublisher;
import cn.xu.model.dto.comment.FindCommentRequest;
import cn.xu.model.dto.comment.FindReplyRequest;
import cn.xu.model.dto.comment.SaveCommentRequest;
import cn.xu.model.entity.Comment;
import cn.xu.model.entity.Notification;
import cn.xu.model.entity.User;
import cn.xu.repository.ICommentRepository;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.repository.mapper.UserMapper;
import cn.xu.service.file.FileManagementService;
import cn.xu.service.follow.FollowService;
import cn.xu.service.notification.NotificationService;
import cn.xu.service.user.IUserService;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 帖子评论服务
 * <p>提供评论发表、回复、删除、查询等功能</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final ICommentRepository commentRepository;
    private final SocialEventPublisher socialEventPublisher;
    private final IUserService IUserService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FileManagementService fileManagementService;
    private final PostMapper postMapper;
    private final UserMapper userMapper;
    private final NotificationService notificationService;
    private final FollowService followService;

    private static final String COMMENT_HOT_PAGE_KEY = "comment:hot:page:";
    private static final long CACHE_EXPIRE_MINUTES = 10;

    // ==================== 接口实现 ====================
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(SaveCommentRequest request) {
        validateCommentParams(request);

        Comment comment = buildComment(request);
        Long commentId = commentRepository.save(comment);
        comment.setId(commentId);

        updateHotScore(comment);
        commentRepository.update(comment);

        // 如果是回复（有parentId），使用原子操作增加父评论的 replyCount
        if (request.getParentId() != null && request.getParentId() > 0) {
            commentRepository.incrementReplyCount(request.getParentId());
            log.info("[评论] 增加父评论 {} 的回复数", request.getParentId());
        }

        // 标记图片为正式文件（防止被临时文件清理任务删除）
        if (request.getImageUrls() != null && !request.getImageUrls().isEmpty()) {
            fileManagementService.markFilesAsPermanent(request.getImageUrls());
            log.info("[评论] 评论 {} 的 {} 张图片已标记为正式", commentId, request.getImageUrls().size());
        }

        // 更新帖子评论数（仅对帖子类型的评论）
        // 注意：不捕获异常，让失败触发事务回滚，保证评论表和帖子表的计数一致性
        if (request.getTargetType() != null && request.getTargetType().equals(CommentType.POST.getValue())) {
            postMapper.updateCommentCount(request.getTargetId(), 1);
            log.info("[评论] 增加帖子 {} 的评论数", request.getTargetId());
        }

        // 更新用户评论数
        userMapper.increaseCommentCount(request.getUserId());
        log.info("[评论] 增加用户 {} 的评论数", request.getUserId());

        // 发布评论事件（用于触发通知）
        publishCommentEvent(request, commentId);

        return commentId;
    }

    public List<Comment> findCommentListWithPreview(FindCommentRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }

        CommentSortType sortType = validateAndGetSortType(request.getSortType());

        try {
            List<Comment> rootComments = (sortType == CommentSortType.HOT)
                    ? findCommentsByHotSort(request)
                    : findCommentsByTimeSort(request);

            if (rootComments.isEmpty()) {
                return Collections.emptyList();
            }

            enrichCommentsWithChildren(rootComments, sortType);
            fillUserInfo(rootComments);

            return rootComments;
        } catch (Exception e) {
            log.error("查询评论发生错误: targetType={}, targetId={}",
                    request.getTargetType(), request.getTargetId(), e);
            return Collections.emptyList();
        }
    }

    public List<Comment> findChildCommentList(FindReplyRequest request) {
        List<Comment> replies;
        if (CommentSortType.HOT.name().equalsIgnoreCase(request.getSortType())) {
            replies = commentRepository.findRepliesByParentIdByHot(
                    request.getParentId(), request.getPageNo(), request.getPageSize());
        } else {
            replies = commentRepository.findRepliesByParentIdByTime(
                    request.getParentId(), request.getPageNo(), request.getPageSize());
        }
        fillUserInfo(replies);
        return replies;
    }

    /**
     * 删除评论（带权限校验）
     * <p>仅评论作者或帖子作者可删除</p>
     *
     * @param commentId 评论ID
     * @param operatorId 操作者ID
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentWithPermission(Long commentId, Long operatorId) {
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
        
        // 执行删除
        doDeleteComment(comment);
    }
    
    /**
     * 删除评论（内部方法，无权限校验）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            log.info("[评论] 评论不存在 - commentId: {}", commentId);
            return;
        }
        doDeleteComment(comment);
    }
    
    /**
     * 执行删除评论的实际操作
     */
    private void doDeleteComment(Comment comment) {
        Long commentId = comment.getId();
        
        // 更新父评论的回复数
        if (comment.getParentId() != null && comment.getParentId() > 0) {
            commentRepository.decrementReplyCount(comment.getParentId());
            log.info("[评论] 更新父评论回复数 - parentId: {}", comment.getParentId());
        }

        // 清理评论图片
        cleanupCommentImages(comment);

        // 统计子评论数量
        Long childCount = commentRepository.countByParentId(commentId);
        int deleteCount = 1 + childCount.intValue();

        // 清理子评论图片
        if (childCount > 0) {
            List<Comment> children = commentRepository.findByParentId(commentId);
            for (Comment child : children) {
                cleanupCommentImages(child);
            }
        }

        // 更新帖子评论数
        if (comment.getTargetType() == CommentType.POST.getValue()) {
            postMapper.updateCommentCount(comment.getTargetId(), -deleteCount);
            log.info("[评论] 更新帖子评论数 - postId: {}, delta: -{}", comment.getTargetId(), deleteCount);
        }

        // 更新用户评论数（主评论作者）
        userMapper.decreaseCommentCount(comment.getUserId());
        log.info("[评论] 减少用户 {} 的评论数", comment.getUserId());
        
        // 更新子评论作者的评论数
        if (childCount > 0) {
            List<Comment> children = commentRepository.findByParentId(commentId);
            for (Comment child : children) {
                userMapper.decreaseCommentCount(child.getUserId());
            }
            log.info("[评论] 减少 {} 个子评论作者的评论数", childCount);
        }

        // 发布删除事件
        socialEventPublisher.publishCommentDeleted(comment.getUserId(), comment.getTargetId(), commentId);

        // 执行删除
        commentRepository.deleteById(commentId);
        commentRepository.deleteByParentId(commentId);
        
        log.info("[评论] 删除完成 - commentId: {}, 共删除: {}条", commentId, deleteCount);
    }

    public Comment getCommentById(Long commentId) {
        if (commentId == null) {
            throw new BusinessException("评论ID不能为空");
        }
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        return comment;
    }

    /**
     * 查询用户评论列表
     */
    public List<Comment> findByUserId(Long userId, int offset, int pageSize) {
        return commentRepository.findByUserId(userId, offset, pageSize);
    }

    /**
     * 查询用户评论总数
     */
    public Long countByUserId(Long userId) {
        return commentRepository.countByUserId(userId);
    }

    /**
     * 获取对话链（根据replyId追溯上下文）
     * @param replyId 当前回复ID
     * @return 对话链列表（从最新到最早）
     */
    public List<Comment> getConversationChain(Long replyId) {
        Comment current = commentRepository.findById(replyId);
        if (current == null) {
            return new ArrayList<>();
        }
        
        List<Comment> chain = new ArrayList<>();
        chain.add(current);
        fillUserInfo(chain);
        
        // 获取同一父评论下的所有子评论
        Long parentId = current.getParentId();
        if (parentId == null || parentId == 0) {
            return chain; // 是根评论，无对话链
        }
        
        List<Comment> siblings = commentRepository.findByParentId(parentId);
        fillUserInfo(siblings);
        
        // 根据replyUserId向上追溯
        Set<Long> visited = new HashSet<>();
        visited.add(current.getId());
        
        Long targetUserId = current.getReplyUserId();
        while (targetUserId != null) {
            Comment parent = null;
            for (Comment c : siblings) {
                if (!visited.contains(c.getId()) && c.getUserId().equals(targetUserId)) {
                    parent = c;
                    break;
                }
            }
            if (parent == null) break;
            
            visited.add(parent.getId());
            chain.add(0, parent); // 插入到头部
            targetUserId = parent.getReplyUserId();
        }
        
        return chain;
    }

    // ==================== 查询方法 ====================

    private List<Comment> findCommentsByTimeSort(FindCommentRequest request) {
        return commentRepository.findRootCommentsByTime(
                request.getTargetType(), request.getTargetId(),
                request.getPageNo(), request.getPageSize());
    }

    private List<Comment> findCommentsByHotSort(FindCommentRequest request) {
        String cacheKey = String.format("%s%d:%d:%d:%d",
                COMMENT_HOT_PAGE_KEY,
                request.getTargetType(),
                request.getTargetId(),
                request.getPageNo(),
                request.getPageSize()
        );

        try {
            List<Comment> cached = (List<Comment>) redisTemplate.opsForValue().get(cacheKey);
            if (cached != null) {
                return cached;
            }

            List<Comment> comments = commentRepository.findRootCommentsByHot(
                    request.getTargetType(), request.getTargetId(),
                    request.getPageNo(), request.getPageSize());

            if (!comments.isEmpty()) {
                redisTemplate.opsForValue().set(cacheKey, comments, CACHE_EXPIRE_MINUTES, TimeUnit.MINUTES);
            }
            return comments;
        } catch (Exception e) {
            log.error("缓存操作发生错误，回退查询数据库", e);
            return commentRepository.findRootCommentsByHot(
                    request.getTargetType(), request.getTargetId(),
                    request.getPageNo(), request.getPageSize());
        }
    }

    // ==================== 用户信息填充 ====================

    private void fillUserInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            log.warn("[fillUserInfo] 评论列表为空");
            return;
        }

        Set<Long> userIds = comments.stream()
                .flatMap(c -> {
                    List<Long> ids = new ArrayList<>();
                    if (c.getUserId() != null) ids.add(c.getUserId());
                    if (c.getReplyUserId() != null) ids.add(c.getReplyUserId());
                    return ids.stream();
                })
                .collect(Collectors.toSet());

        if (!userIds.isEmpty()) {
            Map<Long, User> userMap = IUserService.findUserInfo(userIds);
            comments.forEach(comment -> {
                if (comment.getUserId() != null) {
                    comment.setUser(userMap.get(comment.getUserId()));
                }
                if (comment.getReplyUserId() != null) {
                    comment.setReplyUser(userMap.get(comment.getReplyUserId()));
                }
            });
        }
    }

    // ==================== 辅助方法 ====================

    private void validateCommentParams(SaveCommentRequest request) {
        // 参数校验代码
    }

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
        comment.setCreateTime(LocalDateTime.now());
        return comment;
    }

    private void updateHotScore(Comment comment) {
        // 更新热度分数的代码
    }

    private void cleanupCommentImages(Comment comment) {
        if (comment.getImageUrls() != null && !comment.getImageUrls().isEmpty()) {
            fileManagementService.deleteFiles(comment.getImageUrls());
            log.info("删除评论 {} 相关的图片文件", comment.getId());
        }
    }

    private CommentSortType validateAndGetSortType(String sortType) {
        if (sortType == null || sortType.isEmpty()) {
            return CommentSortType.TIME;
        }
        return CommentSortType.valueOf(sortType.toUpperCase());
    }

    private void enrichCommentsWithChildren(List<Comment> rootComments, CommentSortType sortType) {
        rootComments.forEach(comment -> {
            List<Comment> childComments = (sortType == CommentSortType.HOT)
                    ? commentRepository.findRepliesByParentIdByHot(comment.getId())
                    : commentRepository.findRepliesByParentIdByTime(comment.getId());
            // 为子评论填充用户信息
            if (childComments != null && !childComments.isEmpty()) {
                fillUserInfo(childComments);
            }
            comment.setChildren(childComments);
        });
    }
    
    // ==================== 管理后台方法 ====================
    
    /**
     * 管理员删除评论（完整删除，包含子评论和计数更新）
     */
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment == null) {
            throw new BusinessException("评论不存在");
        }
        doDeleteComment(comment);
        log.info("[评论] 管理员删除成功 - commentId: {}", commentId);
    }
    
    /** 查询根评论列表（后台用） */
    public List<Comment> findAllRootComments(Integer targetType, Long targetId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return commentRepository.findAllRootComments(targetType, targetId, offset, size);
    }
    
    /** 统计根评论数量（后台用） */
    public long countAllRootComments(Integer targetType, Long targetId) {
        return commentRepository.countAllRootComments(targetType, targetId);
    }
    
    // ==================== 参数验证方法 ====================
    
    /**
     * 验证分页参数
     */
    public void validatePageParams(Integer page, Integer size) {
        if (page == null || page < 1) {
            throw new BusinessException("页码必须大于0");
        }
        if (size == null || size < 1 || size > 100) {
            throw new BusinessException("每页数量必须在1-100之间");
        }
    }
    
    /**
     * 验证评论创建参数
     */
    public void validateCommentCreateParams(Integer targetType, Long targetId, String content) {
        if (targetType == null) {
            throw new BusinessException("评论目标类型不能为空");
        }
        if (targetId == null) {
            throw new BusinessException("评论目标ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
        if (content.length() > 1000) {
            throw new BusinessException("评论内容不能超过1000字");
        }
    }
    
    /**
     * 验证评论回复参数
     */
    public void validateCommentReplyParams(Integer targetType, Long targetId, Long parentId, Long replyUserId, String content) {
        validateCommentCreateParams(targetType, targetId, content);
        if (parentId == null) {
            throw new BusinessException("父评论ID不能为空");
        }
        if (replyUserId == null) {
            throw new BusinessException("被回复用户ID不能为空");
        }
    }

    /**
     * 发布评论事件（用于触发通知）
     */
    private void publishCommentEvent(SaveCommentRequest request, Long commentId) {
        try {
            // 只处理帖子评论类型
            if (request.getTargetType() == null || !request.getTargetType().equals(CommentType.POST.getValue())) {
                return;
            }

            if (request.getParentId() != null && request.getParentId() > 0) {
                // 回复评论
                socialEventPublisher.publishReplyCreated(
                        request.getUserId(),
                        request.getTargetId(),  // postId
                        commentId,
                        request.getParentId(),
                        request.getReplyUserId(),
                        request.getContent()
                );
                log.debug("[评论] 发布回复事件 - commentId: {}, parentId: {}", commentId, request.getParentId());
            } else {
                // 一级评论
                socialEventPublisher.publishCommentCreated(
                        request.getUserId(),
                        request.getTargetId(),  // postId
                        commentId,
                        request.getContent()
                );
                log.debug("[评论] 发布评论事件 - commentId: {}, postId: {}", commentId, request.getTargetId());
            }
        } catch (Exception e) {
            // 事件发布失败不影响评论创建
            log.error("[评论] 发布评论事件失败 - commentId: {}", commentId, e);
        }

        // 发送@提及通知
        sendMentionNotifications(request);
    }

    /**
     * 发送@提及通知
     * 安全校验：
     * 1. 去重 - 同一用户只通知一次
     * 2. 数量限制 - 最多@10个用户
     * 3. 关注校验 - 只能@已关注的用户
     */
    private void sendMentionNotifications(SaveCommentRequest request) {
        List<Long> mentionedUserIds = request.getMentionedUserIds();
        if (mentionedUserIds == null || mentionedUserIds.isEmpty()) {
            return;
        }

        try {
            Long senderId = request.getUserId();
            Long postId = request.getTargetId();
            String content = request.getContent();

            // 1. 去重
            java.util.Set<Long> uniqueUserIds = new java.util.LinkedHashSet<>(mentionedUserIds);
            
            // 2. 数量限制（最多@10个用户）
            int maxMentions = 10;
            int count = 0;
            
            // 3. 获取当前用户的关注列表（用于校验）
            List<Long> followingUserIds = followService.getFollowingUserIds(senderId, 500);
            java.util.Set<Long> followingSet = new java.util.HashSet<>(followingUserIds);

            for (Long receiverId : uniqueUserIds) {
                // 不通知自己
                if (senderId.equals(receiverId)) {
                    continue;
                }
                
                // 校验是否关注了该用户（安全校验）
                if (!followingSet.contains(receiverId)) {
                    log.warn("[评论] @校验失败：用户{}未关注用户{}", senderId, receiverId);
                    continue;
                }
                
                // 数量限制
                if (count >= maxMentions) {
                    log.warn("[评论] @数量超限，最多{}个", maxMentions);
                    break;
                }

                Notification notification = Notification.createMentionNotification(
                        senderId, receiverId, postId, content);
                notificationService.sendNotification(notification);
                count++;
                log.debug("[评论] 发送@提及通知 - sender: {}, receiver: {}", senderId, receiverId);
            }
        } catch (Exception e) {
            log.error("[评论] 发送@提及通知失败", e);
        }
    }
}
