package cn.xu.service.comment;

import cn.xu.common.constant.CommentSortType;
import cn.xu.common.constant.CommentType;
import cn.xu.event.comment.CommentEvent;
import cn.xu.event.comment.CommentEventPublisher;
import cn.xu.model.dto.comment.FindCommentRequest;
import cn.xu.model.dto.comment.FindReplyRequest;
import cn.xu.model.entity.Comment;
import cn.xu.model.entity.User;
import cn.xu.repository.ICommentRepository;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.service.file.FileManagementService;
import cn.xu.service.user.IUserService;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 评论服务实现（V3.0 - 彻底简化版）
 * 直接使用Comment PO，移除Entity层
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentService {

    private final ICommentRepository commentRepository;
    private final CommentEventPublisher commentEventPublisher;
    private final IUserService userService;
    private final RedisTemplate<String, Object> redisTemplate;
    private final FileManagementService fileManagementService;
    private final PostMapper postMapper;

    private static final String COMMENT_HOT_PAGE_KEY = "comment:hot:page:";
    private static final long CACHE_EXPIRE_MINUTES = 10;

    // ==================== 接口实现 ====================
    @Transactional(rollbackFor = Exception.class)
    public Long saveComment(CommentEvent event) {
        validateCommentParams(event);
        
        Comment comment = buildComment(event);
        Long commentId = commentRepository.save(comment);
        comment.setId(commentId);
        
        updateHotScore(comment);
        commentRepository.update(comment);
        
        // 如果是回复（有 parentId），使用原子操作增加父评论的 replyCount
        if (event.getParentId() != null && event.getParentId() > 0) {
            commentRepository.incrementReplyCount(event.getParentId());
            log.info("增加父评论 {} 的回复数", event.getParentId());
        }
        
        // 标记图片为正式文件（防止被临时文件清理任务删除）
        if (event.getImageUrls() != null && !event.getImageUrls().isEmpty()) {
            fileManagementService.markFilesAsPermanent(event.getImageUrls());
            log.info("评论 {} 的 {} 张图片已标记为正式", commentId, event.getImageUrls().size());
        }
        
        // 更新帖子评论数（仅对帖子类型的评论）
        // 注意：不捕获异常，让失败触发事务回滚，保证评论表和帖子表的计数一致性
        if (event.getTargetType() != null && event.getTargetType() == CommentType.POST.getValue()) {
            postMapper.updateCommentCount(event.getTargetId(), 1);
            log.info("增加帖子 {} 的评论数", event.getTargetId());
        }
        
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
            log.error("查询评论列表失败: targetType={}, targetId={}", 
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
    @Transactional(rollbackFor = Exception.class)
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment != null) {
            // 如果是回复（二级评论），减少父评论的 replyCount
            if (comment.getParentId() != null && comment.getParentId() > 0) {
                commentRepository.decrementReplyCount(comment.getParentId());
                log.info("删除回复后，减少父评论 {} 的回复数", comment.getParentId());
            }
            
            // 清理评论关联的图片
            cleanupCommentImages(comment);
            
            // 计算需要减少的评论数（当前评论 + 子评论数）
            int deleteCount = 1;
            Long childCount = comment.getReplyCount();
            if (childCount != null && childCount > 0) {
                deleteCount += childCount.intValue();
                // 清理子评论的图片
                List<Comment> children = commentRepository.findByParentId(commentId);
                for (Comment child : children) {
                    cleanupCommentImages(child);
                }
            }
            
            // 更新帖子评论数（仅对帖子类型的评论）
            if (comment.getTargetType() != null && comment.getTargetType() == CommentType.POST.getValue()) {
                postMapper.updateCommentCount(comment.getTargetId(), -deleteCount);
                log.info("减少帖子 {} 的评论数: -{}", comment.getTargetId(), deleteCount);
            }
            
            CommentEvent event = CommentEvent.builder()
                    .action(CommentEvent.CommentAction.DELETED)
                    .commentId(commentId)
                    .targetType(comment.getTargetType())
                    .targetId(comment.getTargetId())
                    .isRootComment(comment.getParentId() == null)
                    .build();
            commentEventPublisher.publishEvent(event);
        }
        commentRepository.deleteById(commentId);
        commentRepository.deleteByParentId(commentId);
    }
    @Transactional(rollbackFor = Exception.class)
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId);
        if (comment != null) {
            // 如果是回复（二级评论），减少父评论的 replyCount
            if (comment.getParentId() != null && comment.getParentId() > 0) {
                commentRepository.decrementReplyCount(comment.getParentId());
                log.info("管理员删除回复后，减少父评论 {} 的回复数", comment.getParentId());
            }
            
            // 清理评论关联的图片
            cleanupCommentImages(comment);
            
            // 计算需要减少的评论数（当前评论 + 子评论数）
            int deleteCount = 1;
            Long childCount = comment.getReplyCount();
            if (childCount != null && childCount > 0) {
                deleteCount += childCount.intValue();
                // 清理子评论的图片
                List<Comment> children = commentRepository.findByParentId(commentId);
                for (Comment child : children) {
                    cleanupCommentImages(child);
                }
            }
            
            // 更新帖子评论数（仅对帖子类型的评论）
            if (comment.getTargetType() != null && comment.getTargetType() == CommentType.POST.getValue()) {
                try {
                    postMapper.updateCommentCount(comment.getTargetId(), -deleteCount);
                    log.info("管理员删除评论后，减少帖子 {} 的评论数: -{}", comment.getTargetId(), deleteCount);
                } catch (Exception e) {
                    log.error("更新帖子评论数失败 - postId: {}", comment.getTargetId(), e);
                }
            }
            
            CommentEvent event = CommentEvent.builder()
                    .action(CommentEvent.CommentAction.DELETED)
                    .commentId(commentId)
                    .targetType(comment.getTargetType())
                    .targetId(comment.getTargetId())
                    .isRootComment(comment.getParentId() == null)
                    .build();
            commentEventPublisher.publishEvent(event);
        }
        commentRepository.deleteById(commentId);
        commentRepository.deleteByParentId(commentId);
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

    // ==================== 查询方法 ====================
    
    private List<Comment> findCommentsByTimeSort(FindCommentRequest request) {
        return commentRepository.findRootCommentsByTime(
                request.getTargetType(), request.getTargetId(), 
                request.getPageNo(), request.getPageSize());
    }

    @SuppressWarnings("unchecked")
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
            log.error("缓存操作失败，降级查询数据库", e);
            return commentRepository.findRootCommentsByHot(
                    request.getTargetType(), request.getTargetId(),
                    request.getPageNo(), request.getPageSize());
        }
    }

    // ==================== 填充用户信息 ====================
    
    private void fillUserInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
            log.warn("[fillUserInfo] comments为空，跳过");
            return;
        }
        
        // 调试：打印评论的userId
        comments.forEach(c -> {
            log.info("[fillUserInfo] 评论ID: {}, userId: {}, replyUserId: {}", 
                    c.getId(), c.getUserId(), c.getReplyUserId());
        });
        
        Set<Long> userIds = comments.stream()
                .flatMap(c -> {
                    List<Long> ids = new ArrayList<>();
                    if (c.getUserId() != null) ids.add(c.getUserId());
                    if (c.getReplyUserId() != null) ids.add(c.getReplyUserId());
                    if (c.getChildren() != null) {
                        c.getChildren().forEach(child -> {
                            if (child.getUserId() != null) ids.add(child.getUserId());
                            if (child.getReplyUserId() != null) ids.add(child.getReplyUserId());
                        });
                    }
                    return ids.stream();
                })
                .collect(Collectors.toSet());
        
        log.info("[fillUserInfo] 收集到的userIds: {}", userIds);
        
        if (userIds.isEmpty()) {
            log.warn("[fillUserInfo] userIds为空，跳过填充用户信息");
            return;
        }
        
        List<User> users = userService.batchGetUserInfo(new ArrayList<>(userIds));
        log.info("[fillUserInfo] 查询到的用户数量: {}", users.size());
        
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u));
        
        comments.forEach(c -> {
            c.setUser(userMap.get(c.getUserId()));
            c.setReplyUser(userMap.get(c.getReplyUserId()));
            if (c.getChildren() != null) {
                c.getChildren().forEach(child -> {
                    child.setUser(userMap.get(child.getUserId()));
                    child.setReplyUser(userMap.get(child.getReplyUserId()));
                });
            }
        });
    }

    private void enrichCommentsWithChildren(List<Comment> rootComments, CommentSortType sortType) {
        if (rootComments.isEmpty()) {
            log.debug("[enrichCommentsWithChildren] rootComments 为空，跳过");
            return;
        }
        
        List<Long> parentIds = rootComments.stream()
                .map(Comment::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        
        log.info("[enrichCommentsWithChildren] 查询子评论，parentIds: {}, sortType: {}", parentIds, sortType);
        
        // 每个一级评论最多预览2条回复
        final int PREVIEW_SIZE = 2;
        List<Comment> childComments = (sortType == CommentSortType.HOT)
                ? commentRepository.findRepliesByParentIdsByHot(parentIds, PREVIEW_SIZE)
                : commentRepository.findRepliesByParentIdsByTime(parentIds, PREVIEW_SIZE);
        
        log.info("[enrichCommentsWithChildren] 查询到子评论数量: {}", childComments != null ? childComments.size() : 0);
        
        Map<Long, List<Comment>> childrenMap = childComments.stream()
                .collect(Collectors.groupingBy(Comment::getParentId));
        
        rootComments.forEach(root -> {
            List<Comment> children = childrenMap.get(root.getId());
            if (children != null && !children.isEmpty()) {
                root.setChildren(children);
            }
        });
    }

    // ==================== 构建和验证 ====================
    
    private void validateCommentParams(CommentEvent event) {
        if (event.getTargetType() == null || event.getTargetId() == null) {
            throw new BusinessException("评论目标类型和ID不能为空");
        }
        if (event.getUserId() == null) {
            throw new BusinessException("评论用户ID不能为空");
        }
        if (event.getContent() == null || event.getContent().trim().isEmpty()) {
            throw new BusinessException("评论内容不能为空");
        }
    }

    private Comment buildComment(CommentEvent event) {
        Comment comment = Comment.builder()
                .targetType(event.getTargetType())
                .targetId(event.getTargetId())
                .parentId(event.getParentId())
                .userId(event.getUserId())
                .replyUserId(event.getReplyUserId())
                .content(event.getContent())
                .likeCount(0L)
                .replyCount(0L)
                .hotScore(BigDecimal.ZERO)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
        
        // 设置图片URL（使用comment表的imageUrl字段，逗号分隔存储）
        if (event.getImageUrls() != null && !event.getImageUrls().isEmpty()) {
            comment.setImageUrls(event.getImageUrls());
        }
        
        return comment;
    }

    private void updateHotScore(Comment comment) {
        long likeCount = comment.getLikeCount() != null ? comment.getLikeCount() : 0;
        long replyCount = comment.getReplyCount() != null ? comment.getReplyCount() : 0;
        double score = likeCount * 2.0 + replyCount;
        comment.setHotScore(BigDecimal.valueOf(score));
    }

    /**
     * 清理评论关联的图片
     * 评论删除时，将关联图片标记为删除状态
     */
    private void cleanupCommentImages(Comment comment) {
        if (comment == null || !comment.hasImages()) {
            return;
        }
        try {
            List<String> imageUrls = comment.getImageUrls();
            fileManagementService.deleteFiles(imageUrls);
            log.info("评论 {} 的 {} 张图片已清理", comment.getId(), imageUrls.size());
        } catch (Exception e) {
            // 图片清理失败不影响评论删除
            log.warn("清理评论 {} 图片失败: {}", comment.getId(), e.getMessage());
        }
    }

    private CommentSortType validateAndGetSortType(String sortTypeStr) {
        if (sortTypeStr == null || sortTypeStr.trim().isEmpty()) {
            return CommentSortType.NEW;
        }
        try {
            return CommentSortType.valueOf(sortTypeStr.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommentSortType.NEW;
        }
    }

    // ==================== 统计方法 ====================
    
    public Long getCommentCount(Long targetId, CommentType type) {
        if (targetId == null || type == null) {
            return 0L;
        }
        return commentRepository.countByTargetTypeAndTargetId(type.getValue(), targetId);
    }

    public Map<Long, Long> batchGetCommentCount(List<Long> targetIds, CommentType type) {
        if (targetIds == null || targetIds.isEmpty() || type == null) {
            return new HashMap<>();
        }
        return commentRepository.batchCountByTargetIds(type.getValue(), targetIds);
    }

    // ==================== 验证方法 ====================
    public void validateCommentCreateParams(Integer type, Long targetId, String content) {
        if (type == null) {
            throw new BusinessException(cn.xu.common.ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论类型不能为空");
        }
        try {
            CommentType.valueOf(type);
        } catch (Exception e) {
            throw new BusinessException(cn.xu.common.ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论类型不正确");
        }
        if (targetId == null) {
            throw new BusinessException(cn.xu.common.ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论目标ID不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(cn.xu.common.ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论内容不能为空");
        }
        if (content.length() > 1000) {
            throw new BusinessException(cn.xu.common.ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论内容不能超过1000字");
        }
    }
    public void validateCommentReplyParams(Integer type, Long targetId, Long commentId, Long replyUserId, String content) {
        validateCommentCreateParams(type, targetId, content);
        if (commentId == null) {
            throw new BusinessException(cn.xu.common.ResponseCode.ILLEGAL_PARAMETER.getCode(), "父评论ID不能为空");
        }
        if (replyUserId == null) {
            throw new BusinessException(cn.xu.common.ResponseCode.ILLEGAL_PARAMETER.getCode(), "被回复用户ID不能为空");
        }
    }
    public void validatePageParams(Integer pageNo, Integer pageSize) {
        if (pageNo == null || pageNo < 1) {
            throw new BusinessException(cn.xu.common.ResponseCode.ILLEGAL_PARAMETER.getCode(), "页码不能小于1");
        }
        if (pageSize == null || pageSize < 1 || pageSize > 100) {
            throw new BusinessException(cn.xu.common.ResponseCode.ILLEGAL_PARAMETER.getCode(), "每页数量必须在1-100之间");
        }
    }
    public List<Comment> findAllRootComments(Integer type, Long userId, int pageNo, int pageSize) {
        int offset = Math.max(0, (pageNo - 1) * pageSize);
        List<Comment> comments = commentRepository.findRootComments(type, null, userId, offset, pageSize);
        
        // 填充用户信息
        if (!comments.isEmpty()) {
            List<Long> userIds = comments.stream()
                    .map(Comment::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            if (!userIds.isEmpty()) {
                Map<Long, User> userMap = userService.batchGetUserInfo(userIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
                
                comments.forEach(comment -> {
                    User user = userMap.get(comment.getUserId());
                    if (user != null) {
                        comment.setUser(user);
                    }
                });
            }
        }
        return comments;
    }
    public long countAllRootComments(Integer type, Long userId) {
        return commentRepository.countRootComments(type, null, userId);
    }
}
