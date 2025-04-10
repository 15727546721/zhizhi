package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.comment.*;
import cn.xu.api.web.model.vo.comment.CommentListVO;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.event.CommentEvent;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.service.ICommentService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.common.annotation.ApiOperationLog;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "评论接口", description = "评论相关接口")
@RestController
@RequestMapping("/api/comment")
public class CommentController {
    @Resource
    private ICommentService commentService;

    @Operation(summary = "发表评论")
    @PostMapping("/publish")
    @ApiOperationLog(description = "发表评论")
    public ResponseEntity<CommentEntity> addComment(@RequestBody CommentAddRequest request) {
        Long commentId = commentService.saveComment(CommentEvent.builder()
                .content(request.getContent())
                .targetId(request.getTargetId())
                .targetType(CommentType.valueOf(request.getTargetType()))
                .userId(StpUtil.getLoginIdAsLong())
                .build());
        CommentEntity comment = commentService.findCommentWithUserById(commentId);
        return ResponseEntity.<CommentEntity>builder()
                .info("评论成功")
                .data(comment)
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @Operation(summary = "回复评论")
    @PostMapping("/reply")
    @ApiOperationLog(description = "回复评论")
    public ResponseEntity<Void> replyComment(@RequestBody ReplyCommentRequest request) {
        commentService.saveComment(CommentEvent.builder()
                .content(request.getContent())
                .targetId(request.getTargetId())
                .targetType(CommentType.valueOf(request.getTargetType()))
                .commentId(request.getCommentId())
                .userId(StpUtil.getLoginIdAsLong())
                .replyUserId(request.getReplyUserId())
                .build());
        return ResponseEntity.<Void>builder()
                .info("评论成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @PostMapping("/page/list")
    @ApiOperationLog(description = "评论分页查询")
    public ResponseEntity<List<FindCommentItemVO>> getCommentPageList(@RequestBody FindCommentReq request) {
        List<FindCommentItemVO> commentPageList = commentService.findCommentPageList(request);
        return ResponseEntity.<List<FindCommentItemVO>>builder()
                .data(commentPageList)
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取评论列表成功")
                .build();
    }

    @PostMapping("/reply/list")
    @ApiOperationLog(description = "获取评论回复列表")
    public ResponseEntity<List<FindChildCommentItemVO>> getReplyCommentList(@RequestBody FindReplyReq request) {
        List<FindChildCommentItemVO> replyCommentList = commentService.findReplyPageList(request);
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
