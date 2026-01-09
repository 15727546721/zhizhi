package cn.xu.service.comment;

import cn.xu.model.dto.comment.FindCommentRequest;
import cn.xu.model.dto.comment.FindReplyRequest;
import cn.xu.model.dto.comment.SaveCommentRequest;
import cn.xu.model.entity.Comment;
import cn.xu.model.entity.Like;
import cn.xu.model.entity.Post;
import cn.xu.model.vo.comment.CommentVO;
import cn.xu.service.like.LikeService;
import cn.xu.service.post.PostQueryService;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 评论应用服务（门面）
 * <p>Controller 层调用入口，协调各子服务</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CommentApplicationService {

    private final CommentQueryService queryService;
    private final CommentCommandService commandService;
    private final CommentConverter converter;
    private final LikeService likeService;
    private final PostQueryService postQueryService;

    // ==================== 查询操作 ====================

    /**
     * 获取评论列表（带点赞状态）
     *
     * @param request       查询请求
     * @param currentUserId 当前用户ID（可为null）
     */
    public List<CommentVO> getCommentList(FindCommentRequest request, Long currentUserId) {
        validatePageParams(request.getPageNo(), request.getPageSize());
        
        List<Comment> comments = queryService.findCommentListWithPreview(request);
        if (comments.isEmpty()) {
            return new ArrayList<>();
        }

        // 获取帖子作者ID
        Long authorId = getPostAuthorId(request.getTargetId());

        // 收集所有评论ID
        List<Long> allIds = converter.collectAllIds(comments);

        // 批量查询点赞状态
        Set<Long> userLikeSet = getUserLikeSet(currentUserId, allIds);
        Set<Long> authorLikeSet = getAuthorLikeSet(authorId, currentUserId, allIds, userLikeSet);

        return converter.toVOList(comments, userLikeSet, authorLikeSet, authorId);
    }

    /**
     * 获取回复列表
     */
    public List<CommentVO> getReplyList(FindReplyRequest request, Long currentUserId) {
        validatePageParams(request.getPageNo(), request.getPageSize());
        
        List<Comment> replies = queryService.findChildCommentList(request);
        if (replies.isEmpty()) {
            return new ArrayList<>();
        }

        List<Long> allIds = converter.collectAllIds(replies);
        Set<Long> userLikeSet = getUserLikeSet(currentUserId, allIds);

        return converter.toVOList(replies, userLikeSet, null, null);
    }

    /**
     * 获取用户评论列表
     */
    public List<Comment> getUserComments(Long userId, Integer pageNo, Integer pageSize) {
        validatePageParams(pageNo, pageSize);
        int offset = (pageNo - 1) * pageSize;
        return queryService.findByUserId(userId, offset, pageSize);
    }

    /**
     * 统计用户评论数
     */
    public Long countUserComments(Long userId) {
        return queryService.countByUserId(userId);
    }

    /**
     * 获取对话链
     */
    public List<CommentVO> getConversationChain(Long replyId) {
        List<Comment> chain = queryService.getConversationChain(replyId);
        return converter.toVOList(chain, null, null, null);
    }

    // ==================== 写操作 ====================

    /**
     * 发表评论
     */
    public Long addComment(SaveCommentRequest request) {
        validateCommentCreateParams(request.getTargetType(), request.getTargetId(), request.getContent());
        return commandService.saveComment(request);
    }

    /**
     * 回复评论
     */
    public Long replyComment(SaveCommentRequest request) {
        validateCommentReplyParams(request);
        return commandService.saveComment(request);
    }

    /**
     * 删除评论
     */
    public void deleteComment(Long commentId, Long operatorId) {
        commandService.deleteWithPermission(commentId, operatorId);
    }

    /**
     * 管理员删除评论
     */
    public void deleteCommentByAdmin(Long commentId) {
        commandService.deleteByAdmin(commentId);
    }

    // ==================== 参数验证 ====================

    public void validatePageParams(Integer page, Integer size) {
        if (page == null || page < 1) {
            throw new BusinessException("页码必须大于0");
        }
        if (size == null || size < 1 || size > 100) {
            throw new BusinessException("每页数量必须在1-100之间");
        }
    }

    private void validateCommentCreateParams(Integer targetType, Long targetId, String content) {
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

    private void validateCommentReplyParams(SaveCommentRequest request) {
        validateCommentCreateParams(request.getTargetType(), request.getTargetId(), request.getContent());
        if (request.getParentId() == null) {
            throw new BusinessException("父评论ID不能为空");
        }
        if (request.getReplyUserId() == null) {
            throw new BusinessException("被回复用户ID不能为空");
        }
    }

    // ==================== 私有方法 ====================

    private Long getPostAuthorId(Long postId) {
        if (postId == null) {
            return null;
        }
        Optional<Post> postOpt = postQueryService.getById(postId);
        return postOpt.map(Post::getUserId).orElse(null);
    }

    private Set<Long> getUserLikeSet(Long userId, List<Long> commentIds) {
        if (userId == null || commentIds.isEmpty()) {
            return new HashSet<>();
        }
        return likeService.batchCheckStatus(userId, Like.LikeType.COMMENT.getCode(), commentIds);
    }

    private Set<Long> getAuthorLikeSet(Long authorId, Long currentUserId, List<Long> commentIds, Set<Long> userLikeSet) {
        if (authorId == null || commentIds.isEmpty()) {
            return new HashSet<>();
        }
        // 如果作者就是当前用户，复用已查询的结果
        if (authorId.equals(currentUserId)) {
            return userLikeSet;
        }
        return likeService.batchCheckStatus(authorId, Like.LikeType.COMMENT.getCode(), commentIds);
    }
}
