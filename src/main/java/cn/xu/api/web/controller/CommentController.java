package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.comment.*;
import cn.xu.api.web.model.vo.comment.FindCommentItemVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.application.query.comment.CommentQueryService;
import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.service.ICommentService;
import cn.xu.infrastructure.common.annotation.ApiOperationLog;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Tag(name = "评论接口", description = "评论相关接口")
@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Resource
    private ICommentService commentService;

    @Resource
    private CommentQueryService commentQueryService;

    @Operation(summary = "发表评论")
    @PostMapping("/publish")
    @ApiOperationLog(description = "发表评论")
    public ResponseEntity<?> addComment(@RequestBody CommentAddRequest request) {
        Long commentId = commentService.saveComment(CommentCreatedEvent.builder()
                .targetType(request.getTargetType())
                .userId(StpUtil.getLoginIdAsLong())
                .targetId(request.getTargetId())
                .content(request.getContent())
                .imageUrls(request.getImageUrls())
                .userId(StpUtil.getLoginIdAsLong())
                .build());
        return ResponseEntity.<CommentEntity>builder()
                .info("评论成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @Operation(summary = "回复评论")
    @PostMapping("/reply")
    @ApiOperationLog(description = "回复评论")
    public ResponseEntity<Void> replyComment(@RequestBody ReplyCommentRequest request) {
        commentService.saveComment(CommentCreatedEvent.builder()
                .content(request.getContent())
                .targetId(request.getTargetId())
                .targetType(request.getTargetType())
                .parentId(request.getCommentId())
                .userId(StpUtil.getLoginIdAsLong())
                .replyUserId(request.getReplyUserId())
                .build());
        return ResponseEntity.<Void>builder()
                .info("评论成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping("/list/page")
    @ApiOperationLog(description = "评论分页查询")
    public ResponseEntity<?> getCommentPageList(@RequestBody FindCommentReq request) {
        List<CommentEntity> commentPageList = commentService.findCommentListWithPreview(request);
        return ResponseEntity.builder()
                .data(commentPageList)
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取评论列表成功")
                .build();
    }

    @PostMapping("/list/reply/page")
    @ApiOperationLog(description = "获取评论回复列表")
    public ResponseEntity<List<FindChildCommentItemVO>> getReplyCommentList(@RequestBody FindReplyReq request) {
        List<FindChildCommentItemVO> replyCommentList = commentQueryService.findChildComments(request);
        return ResponseEntity.<List<FindChildCommentItemVO>>builder()
                .data(replyCommentList)
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取评论回复列表成功")
                .build();
    }


    @PostMapping("/page/list")
    @ApiOperationLog(description = "评论分页查询")
    public ResponseEntity<List<FindCommentItemVO>> getCommentPageList1(@RequestBody FindCommentReq request) {
        List<FindCommentItemVO> commentPageList = commentQueryService.findTopComments(request);
        return ResponseEntity.<List<FindCommentItemVO>>builder()
                .data(commentPageList)
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取评论列表成功")
                .build();
    }

    @PostMapping("/reply/list")
    @ApiOperationLog(description = "获取评论回复列表")
    public ResponseEntity<List<FindChildCommentItemVO>> getReplyCommentList1(@RequestBody FindReplyReq request) {
        List<FindChildCommentItemVO> replyCommentList = commentQueryService.findChildComments(request);
        return ResponseEntity.<List<FindChildCommentItemVO>>builder()
                .data(replyCommentList)
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取评论回复列表成功")
                .build();
    }

    @Operation(summary = "删除评论")
    @DeleteMapping("/delete")
    public ResponseEntity<Void> deleteComment(
            @Parameter(description = "评论ID") @RequestParam("commentId") Long commentId) {
        commentService.deleteComment(commentId);
        return ResponseEntity.<Void>builder()
                .info("删除评论成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }
}
