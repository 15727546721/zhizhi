package cn.xu.service.comment;

import cn.xu.cache.service.CacheService;
import cn.xu.model.dto.comment.FindCommentRequest;
import cn.xu.model.dto.comment.FindReplyRequest;
import cn.xu.model.entity.Comment;
import cn.xu.model.entity.User;
import cn.xu.model.enums.CommentSortType;
import cn.xu.repository.CommentRepository;
import cn.xu.service.user.UserService;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论查询服务
 * <p>负责评论的读取操作</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final UserService userService;
    private final CacheService cacheService;

    private static final String COMMENT_HOT_PAGE_KEY = "comment:hot:page:";
    private static final long CACHE_EXPIRE_SECONDS = 600;

    // ==================== 评论列表查询 ====================

    /**
     * 查询评论列表（带子评论预览）
     */
    public List<Comment> findCommentListWithPreview(FindCommentRequest request) {
        if (request == null) {
            return Collections.emptyList();
        }

        CommentSortType sortType = parseSortType(request.getSortType());

        try {
            List<Comment> rootComments = (sortType == CommentSortType.HOT)
                    ? findCommentsByHotSort(request)
                    : findCommentsByTimeSort(request);

            if (rootComments.isEmpty()) {
                return Collections.emptyList();
            }

            // 加载子评论
            enrichCommentsWithChildren(rootComments, sortType);
            
            // 填充用户信息
            fillUserInfo(rootComments);

            return rootComments;
        } catch (Exception e) {
            log.error("[评论查询] 查询评论失败 - targetType: {}, targetId: {}",
                    request.getTargetType(), request.getTargetId(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 查询子评论列表
     */
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
     * 根据ID获取评论
     */
    public Comment getById(Long commentId) {
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
     * 统计用户评论数
     */
    public Long countByUserId(Long userId) {
        return commentRepository.countByUserId(userId);
    }

    /**
     * 获取对话链
     * <p>从当前回复向上追溯对话，找到完整的对话上下文</p>
     */
    public List<Comment> getConversationChain(Long replyId) {
        Comment current = commentRepository.findById(replyId);
        if (current == null) {
            return new ArrayList<>();
        }

        List<Comment> chain = new ArrayList<>();
        chain.add(current);

        Long parentId = current.getParentId();
        if (parentId == null || parentId == 0) {
            // 当前评论是根评论，没有对话链
            fillUserInfo(chain);
            return chain;
        }

        // 获取同一父评论下的所有兄弟评论
        List<Comment> siblings = commentRepository.findByParentId(parentId);
        if (siblings == null || siblings.isEmpty()) {
            fillUserInfo(chain);
            return chain;
        }

        // 构建 ID -> Comment 的映射，避免重复遍历
        Map<Long, Comment> siblingMap = siblings.stream()
                .collect(Collectors.toMap(Comment::getId, c -> c, (a, b) -> a));

        // 向上追溯对话链，最多10条
        Set<Long> visited = new HashSet<>();
        visited.add(current.getId());

        Long targetUserId = current.getReplyUserId();
        int maxDepth = 10;

        while (targetUserId != null && chain.size() < maxDepth) {
            // 在兄弟评论中找到被回复用户发表的最近一条评论
            Comment parent = null;
            for (Comment c : siblings) {
                if (!visited.contains(c.getId()) && c.getUserId().equals(targetUserId)) {
                    // 找到被回复用户的评论
                    if (parent == null || c.getCreateTime().isAfter(parent.getCreateTime())) {
                        // 如果有多条，取最新的（或者可以取时间最接近当前评论的）
                        parent = c;
                    }
                }
            }

            if (parent == null) {
                break;
            }

            visited.add(parent.getId());
            chain.add(0, parent);
            targetUserId = parent.getReplyUserId();
        }

        fillUserInfo(chain);
        return chain;
    }

    // ==================== 管理后台查询 ====================

    /**
     * 查询根评论列表（后台用）
     */
    public List<Comment> findAllRootComments(Integer targetType, Long targetId, Integer page, Integer size) {
        int offset = (page - 1) * size;
        return commentRepository.findAllRootComments(targetType, targetId, offset, size);
    }

    /**
     * 统计根评论数量（后台用）
     */
    public long countAllRootComments(Integer targetType, Long targetId) {
        return commentRepository.countAllRootComments(targetType, targetId);
    }

    /**
     * 统计根评论数量（前台用）
     */
    public long countRootComments(Integer targetType, Long targetId) {
        Long count = commentRepository.countRootComments(targetType, targetId, null);
        return count != null ? count : 0L;
    }

    // ==================== 私有方法 ====================

    private List<Comment> findCommentsByTimeSort(FindCommentRequest request) {
        return commentRepository.findRootCommentsByTime(
                request.getTargetType(), request.getTargetId(),
                request.getPageNo(), request.getPageSize());
    }

    /**
     * 热门评论查询（带分布式锁防止缓存击穿）
     */
    private List<Comment> findCommentsByHotSort(FindCommentRequest request) {
        String cacheKey = String.format("%s%d:%d:%d:%d",
                COMMENT_HOT_PAGE_KEY,
                request.getTargetType(),
                request.getTargetId(),
                request.getPageNo(),
                request.getPageSize()
        );

        // 使用带锁的列表缓存方法，防止缓存击穿
        return cacheService.getListOrLoadWithLock(cacheKey, () -> 
            commentRepository.findRootCommentsByHot(
                request.getTargetType(), request.getTargetId(),
                request.getPageNo(), request.getPageSize()),
            CACHE_EXPIRE_SECONDS
        );
    }

    /**
     * 批量加载子评论（修复 N+1 问题）
     * 原来：每个根评论执行一次查询
     * 现在：一次批量查询所有子评论
     */
    private void enrichCommentsWithChildren(List<Comment> rootComments, CommentSortType sortType) {
        if (rootComments == null || rootComments.isEmpty()) {
            return;
        }

        // 收集所有根评论ID
        List<Long> parentIds = rootComments.stream()
                .map(Comment::getId)
                .collect(Collectors.toList());

        // 一次批量查询所有子评论（每个父评论最多3条预览）
        List<Comment> allChildren = (sortType == CommentSortType.HOT)
                ? commentRepository.findRepliesByParentIdsByHot(parentIds, 3)
                : commentRepository.findRepliesByParentIdsByTime(parentIds, 3);

        // 批量填充用户信息
        if (allChildren != null && !allChildren.isEmpty()) {
            fillUserInfo(allChildren);
        }

        // 按父评论ID分组
        Map<Long, List<Comment>> childrenMap = (allChildren == null) 
                ? Collections.emptyMap()
                : allChildren.stream().collect(Collectors.groupingBy(Comment::getParentId));

        // 设置子评论
        rootComments.forEach(comment -> {
            comment.setChildren(childrenMap.getOrDefault(comment.getId(), Collections.emptyList()));
        });
    }

    private void fillUserInfo(List<Comment> comments) {
        if (comments == null || comments.isEmpty()) {
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
            Map<Long, User> userMap = userService.findUserInfo(userIds);
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

    private CommentSortType parseSortType(String sortType) {
        if (sortType == null || sortType.isEmpty()) {
            return CommentSortType.TIME;
        }
        try {
            return CommentSortType.valueOf(sortType.toUpperCase());
        } catch (IllegalArgumentException e) {
            return CommentSortType.TIME;
        }
    }
}
