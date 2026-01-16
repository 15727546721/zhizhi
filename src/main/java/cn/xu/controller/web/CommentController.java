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
import cn.xu.model.vo.comment.CommentVO;
import cn.xu.service.comment.CommentApplicationService;
import cn.xu.service.like.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * 帖子评论控制器
 * <p>提供评论发表、回复、删除、点赞等功能</p>
 */
@Slf4j
@RestController
@RequestMapping("/api/comment")
@Tag(name = "评论接口", description = "评论相关API")
@RequiredArgsConstructor
public class CommentController {

    private final CommentApplicationService commentService;
    private final LikeService likeService;

    /**
     * 获取评论列表
     */
    @PostMapping("/list")
    @Operation(summary = "获取评论列表")
    public ResponseEntity<PageResponse<List<CommentVO>>> getCommentList(@RequestBody FindCommentRequest req) {
        Long currentUserId = getLoginUserIdOrNull();
        PageResponse<List<CommentVO>> result = commentService.getCommentListWithPage(req, currentUserId);
        return ResponseEntity.<PageResponse<List<CommentVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    /**
     * 获取回复列表
     */
    @PostMapping("/reply/list")
    @Operation(summary = "获取回复列表")
    public ResponseEntity<List<CommentVO>> getReplyList(@RequestBody FindReplyRequest req) {
        Long currentUserId = getLoginUserIdOrNull();
        List<CommentVO> result = commentService.getReplyList(req, currentUserId);
        return ResponseEntity.<List<CommentVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    /**
     * 获取用户评论列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户评论列表")
    public ResponseEntity<PageResponse<List<CommentVO>>> getUserComments(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "10") Integer size) {
        
        List<Comment> comments = commentService.getUserComments(userId, page, size);
        Long total = commentService.countUserComments(userId);
        
        // 简单转换（用户评论列表不需要点赞状态）
        List<CommentVO> result = comments.stream()
                .map(this::simpleConvert)
                .collect(java.util.stream.Collectors.toList());
        
        PageResponse<List<CommentVO>> pageResponse = PageResponse.ofList(page, size, total != null ? total : 0L, result);
        return ResponseEntity.<PageResponse<List<CommentVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(pageResponse)
                .build();
    }

    /**
     * 发表评论
     */
    @PostMapping("/add")
    @SaCheckLogin
    @Operation(summary = "发表评论")
    public ResponseEntity<Long> addComment(@RequestBody CommentCreateRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();
        SaveCommentRequest saveRequest = buildSaveRequest(req, userId);
        Long commentId = commentService.addComment(saveRequest);
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
    @Operation(summary = "回复评论")
    public ResponseEntity<Long> replyComment(@RequestBody CommentCreateRequest req) {
        Long userId = StpUtil.getLoginIdAsLong();
        SaveCommentRequest saveRequest = buildSaveRequest(req, userId);
        Long commentId = commentService.replyComment(saveRequest);
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(commentId)
                .info("success")
                .build();
    }

    /**
     * 删除评论
     */
    @PostMapping("/delete/{id}")
    @SaCheckLogin
    @Operation(summary = "删除评论")
    public ResponseEntity<String> deleteComment(@PathVariable("id") Long id) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
        commentService.deleteComment(id, currentUserId);
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
    @Operation(summary = "点赞评论")
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
    @Operation(summary = "取消点赞评论")
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
    @Operation(summary = "获取对话链")
    public ResponseEntity<List<CommentVO>> getConversationChain(@PathVariable("replyId") Long replyId) {
        List<CommentVO> result = commentService.getConversationChain(replyId);
        return ResponseEntity.<List<CommentVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    // ==================== 私有方法 ====================

    private Long getLoginUserIdOrNull() {
        try {
            return StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            return null;
        }
    }

    private SaveCommentRequest buildSaveRequest(CommentCreateRequest req, Long userId) {
        return SaveCommentRequest.builder()
                .targetType(req.getType())
                .targetId(req.getTargetId())
                .parentId(req.getParentId())
                .userId(userId)
                .replyUserId(req.getReplyUserId())
                .content(req.getContent())
                .imageUrls(req.getImageUrls() != null ? req.getImageUrls() : Collections.emptyList())
                .mentionedUserIds(req.getMentionUserIds())
                .build();
    }

    private CommentVO simpleConvert(Comment c) {
        if (c == null) return null;
        return CommentVO.builder()
                .id(c.getId())
                .postId(c.getTargetId())
                .content(c.getContent())
                .imageUrls(c.getImageUrls())
                .parentId(c.getParentId())
                .userId(c.getUserId())
                .nickname(c.getUser() != null ? c.getUser().getNickname() : null)
                .avatar(c.getUser() != null ? c.getUser().getAvatar() : null)
                .likeCount(c.getLikeCount())
                .replyCount(c.getReplyCount())
                .createTime(c.getCreateTime())
                .build();
    }
}