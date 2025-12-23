package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.comment.CommentCreateRequest;
import cn.xu.model.dto.comment.FindCommentRequest;
import cn.xu.model.dto.comment.FindReplyRequest;
import cn.xu.model.dto.comment.SaveCommentRequest;
import cn.xu.model.entity.Comment;
import cn.xu.model.entity.Like;
import cn.xu.model.entity.Post;
import cn.xu.model.vo.comment.CommentVO;
import cn.xu.service.comment.CommentService;
import cn.xu.service.like.LikeService;
import cn.xu.service.post.PostService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

/**
 * 帖子评论控制器
 * <p>提供评论发表、回复、删除、点赞等功能</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/comment")
@Tag(name = "评论接口", description = "评论相关API")
public class CommentController {

    @Resource
    private CommentService commentService;
    
    @Resource
    private LikeService likeService;
    
    @Resource(name = "postService")
    private PostService postService;

    /**
     * 获取评论列表
     */
    @PostMapping("/list")
    @Operation(summary = "getCommentList")
    public ResponseEntity<List<CommentVO>> getCommentList(@RequestBody FindCommentRequest req) {
        commentService.validatePageParams(req.getPageNo(), req.getPageSize());
        List<Comment> comments = commentService.findCommentListWithPreview(req);
        List<CommentVO> result = convertToResponseList(comments, req.getTargetId());
        return ResponseEntity.<List<CommentVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    /**
     * 获取回复列表
     */
    @PostMapping("/reply/list")
    @Operation(summary = "getReplyList")
    public ResponseEntity<List<CommentVO>> getReplyList(@RequestBody FindReplyRequest req) {
        commentService.validatePageParams(req.getPageNo(), req.getPageSize());
        List<Comment> comments = commentService.findChildCommentList(req);
        List<CommentVO> result = convertToResponseList(comments, null);
        return ResponseEntity.<List<CommentVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    /**
     * 获取用户评论列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "getUserComments")
    public ResponseEntity<PageResponse<List<CommentVO>>> getUserComments(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        commentService.validatePageParams(pageNo, pageSize);
        int offset = (pageNo - 1) * pageSize;
        List<Comment> comments = commentService.findByUserId(userId, offset, pageSize);
        Long total = commentService.countByUserId(userId);
        List<CommentVO> result = convertToResponseList(comments, null);
        PageResponse<List<CommentVO>> page = PageResponse.ofList(pageNo, pageSize, total != null ? total : 0L, result);
        return ResponseEntity.<PageResponse<List<CommentVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(page)
                .build();
    }

    /**
     * 发表评论
     */
    @PostMapping("/add")
    @SaCheckLogin
    @Operation(summary = "addComment")
    public ResponseEntity<Long> addComment(@RequestBody CommentCreateRequest req) {
        commentService.validateCommentCreateParams(req.getType(), req.getTargetId(), req.getContent());
        Long userId = StpUtil.getLoginIdAsLong();
        SaveCommentRequest saveRequest = SaveCommentRequest.builder()
                .targetType(req.getType())
                .targetId(req.getTargetId())
                .parentId(req.getParentId())
                .userId(userId)
                .replyUserId(req.getReplyUserId())
                .content(req.getContent())
                .imageUrls(req.getImageUrls() != null ? req.getImageUrls() : Collections.emptyList())
                .mentionedUserIds(req.getMentionUserIds())
                .build();
        Long commentId = commentService.saveComment(saveRequest);
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(commentId)
                .info("success")
                .build();
    }

    /**
     * 回复评论
     */
    @PostMapping("/reply")
    @SaCheckLogin
    @Operation(summary = "replyComment")
    public ResponseEntity<Long> replyComment(@RequestBody CommentCreateRequest req) {
        commentService.validateCommentReplyParams(req.getType(), req.getTargetId(), req.getParentId(), req.getReplyUserId(), req.getContent());
        Long userId = StpUtil.getLoginIdAsLong();
        SaveCommentRequest saveRequest = SaveCommentRequest.builder()
                .targetType(req.getType())
                .targetId(req.getTargetId())
                .parentId(req.getParentId())
                .userId(userId)
                .replyUserId(req.getReplyUserId())
                .content(req.getContent())
                .imageUrls(req.getImageUrls() != null ? req.getImageUrls() : Collections.emptyList())
                .mentionedUserIds(req.getMentionUserIds())
                .build();
        Long commentId = commentService.saveComment(saveRequest);
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(commentId)
                .info("success")
                .build();
    }

    /**
     * 删除评论
     */
    @DeleteMapping("/delete/{id}")
    @SaCheckLogin
    @Operation(summary = "deleteComment")
    public ResponseEntity<String> deleteComment(@PathVariable("id") Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        commentService.deleteCommentWithPermission(id, currentUserId);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("success")
                .build();
    }

    /**
     * 点赞评论
     */
    @PostMapping("/like/{id}")
    @SaCheckLogin
    @Operation(summary = "likeComment")
    public ResponseEntity<String> likeComment(@PathVariable("id") Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        likeService.like(userId, Like.LikeType.COMMENT.getCode(), id);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("success")
                .build();
    }

    /**
     * 取消点赞评论
     */
    @PostMapping("/unlike/{id}")
    @SaCheckLogin
    @Operation(summary = "unlikeComment")
    public ResponseEntity<String> unlikeComment(@PathVariable("id") Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        likeService.unlike(userId, Like.LikeType.COMMENT.getCode(), id);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("success")
                .build();
    }

    /**
     * 获取对话链
     */
    @GetMapping("/conversation/{replyId}")
    @Operation(summary = "getConversationChain")
    public ResponseEntity<List<CommentVO>> getConversationChain(@PathVariable("replyId") Long replyId) {
        List<Comment> chain = commentService.getConversationChain(replyId);
        List<CommentVO> result = chain.stream()
                .map(c -> convertOne(c, new HashMap<>(), new HashMap<>(), null))
                .collect(java.util.stream.Collectors.toList());
        return ResponseEntity.<List<CommentVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    private List<CommentVO> convertToResponseList(List<Comment> comments, Long postId) {
        if (comments == null || comments.isEmpty()) return new ArrayList<>();
        // 获取当前登录用户（未登录时为null，静默处理异常是intentional的）
        Long currentUserId = null;
        try { 
            currentUserId = StpUtil.getLoginIdAsLong(); 
        } catch (Exception ignored) { 
            // 用户未登录时会抛出异常，此处忽略，currentUserId保持为null
        }
        Long authorId = null;
        if (postId != null) {
            Optional<Post> postOpt = postService.getPostById(postId);
            if (postOpt.isPresent()) authorId = postOpt.get().getUserId();
        }
        List<Long> allIds = collectIds(comments);
        Map<Long, Boolean> userLikeMap = new HashMap<>();
        if (currentUserId != null && !allIds.isEmpty()) {
            Set<Long> liked = likeService.batchCheckStatus(currentUserId, Like.LikeType.COMMENT.getCode(), allIds);
            for (Long id : liked) userLikeMap.put(id, true);
        }
        Map<Long, Boolean> authorLikeMap = new HashMap<>();
        if (authorId != null && !allIds.isEmpty() && !authorId.equals(currentUserId)) {
            Set<Long> liked = likeService.batchCheckStatus(authorId, Like.LikeType.COMMENT.getCode(), allIds);
            for (Long id : liked) authorLikeMap.put(id, true);
        } else if (authorId != null && authorId.equals(currentUserId)) {
            authorLikeMap = userLikeMap;
        }
        List<CommentVO> result = new ArrayList<>();
        for (Comment c : comments) result.add(convertOne(c, userLikeMap, authorLikeMap, authorId));
        return result;
    }

    private List<Long> collectIds(List<Comment> comments) {
        List<Long> ids = new ArrayList<>();
        if (comments == null) return ids;
        for (Comment c : comments) {
            if (c.getId() != null) ids.add(c.getId());
            if (c.getChildren() != null) ids.addAll(collectIds(c.getChildren()));
        }
        return ids;
    }

    private CommentVO convertOne(Comment c, Map<Long, Boolean> userLikeMap, Map<Long, Boolean> authorLikeMap, Long authorId) {
        if (c == null) return null;
        List<CommentVO> children = new ArrayList<>();
        if (c.getChildren() != null) {
            for (Comment child : c.getChildren()) {
                CommentVO vo = convertOne(child, userLikeMap, authorLikeMap, authorId);
                if (vo != null) children.add(vo);
            }
        }
        return CommentVO.builder()
                .id(c.getId())
                .postId(c.getTargetId())
                .content(c.getContent())
                .imageUrls(c.getImageUrls())
                .parentId(c.getParentId())
                .replyToUserId(c.getReplyUserId())
                .replyToNickname(c.getReplyUser() != null ? c.getReplyUser().getNickname() : null)
                .level(calculateLevel(c))
                .userId(c.getUserId())
                .nickname(c.getUser() != null ? c.getUser().getNickname() : null)
                .avatar(c.getUser() != null ? c.getUser().getAvatar() : null)
                .userType(c.getUser() != null ? c.getUser().getUserType() : 1)
                .likeCount(c.getLikeCount())
                .replyCount(c.getReplyCount())
                .isLiked(userLikeMap.getOrDefault(c.getId(), false))
                .isTop(false)
                .isHot(c.getLikeCount() != null && c.getLikeCount() > 10)
                .isAuthor(authorId != null && authorId.equals(c.getUserId()))
                .status(1)
                .createTime(c.getCreateTime())
                .updateTime(c.getUpdateTime())
                .replies(children)
                .hasMoreReplies(c.getReplyCount() != null && c.getReplyCount() > children.size())
                .build();
    }

    private Integer calculateLevel(Comment c) {
        if (c.getParentId() == null || c.getParentId() == 0) return 1;
        return 2;
    }
}