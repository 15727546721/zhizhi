package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.comment.CommentAddRequest;
import cn.xu.api.web.model.dto.comment.ReplyCommentRequest;
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
    @Resource
    private IUserService userService;

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


    @Operation(summary = "获取评论列表")
    @Parameters({
            @Parameter(name = "type", description = "评论类型", required = true),
            @Parameter(name = "targetId", description = "目标ID", required = true),
            @Parameter(name = "pageNum", description = "页码", required = true),
            @Parameter(name = "pageSize", description = "每页数量", required = true)
    })
    @GetMapping("/list")
    public ResponseEntity<List<CommentListVO>> getCommentList(
            @RequestParam("type") Integer type,
            @RequestParam("targetId") Long targetId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {

        CommentType commentType = CommentType.valueOf(type);
        List<CommentEntity> commentEntityList = commentService.getPagedComments(commentType, targetId, pageNum, pageSize);

        // 收集所有用户ID（包括子评论的用户）
        Set<Long> userIds = new HashSet<>();
        collectUserIds(commentEntityList, userIds);

        // 批量获取用户信息
        Map<Long, UserEntity> userInfoMap = userService.getBatchUserInfo(userIds);

        // 转换为DTO
        List<CommentListVO> commentListDTOList = convertToDTO(commentEntityList, userInfoMap);

        return ResponseEntity.<List<CommentListVO>>builder()
                .data(commentListDTOList)
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取评论列表成功")
                .build();
    }

    private List<CommentListVO> convertToDTO(List<CommentEntity> commentEntities, Map<Long, UserEntity> userInfoMap) {
        if (commentEntities == null || commentEntities.isEmpty()) {
            return Collections.emptyList();
        }

        return commentEntities.stream()
                .map(commentEntity -> convertSingleCommentToDTO(commentEntity, userInfoMap))
                .collect(Collectors.toList());
    }

    private CommentListVO convertSingleCommentToDTO(CommentEntity commentEntity, Map<Long, UserEntity> userInfoMap) {
        return CommentListVO.builder()
                .id(commentEntity.getId())
                .userId(commentEntity.getUserId())
                .replyUserId(commentEntity.getReplyUserId())
                .content(commentEntity.getContent())
                .createTime(commentEntity.getCreateTime())
                .userInfo(userInfoMap.get(commentEntity.getUserId()))
                .replyComment(convertToDTO(commentEntity.getChildren(), userInfoMap))
                .build();
    }

    private void collectUserIds(List<CommentEntity> comments, Set<Long> userIds) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        comments.forEach(comment -> {
            userIds.add(comment.getUserId());
            Optional.ofNullable(comment.getReplyUserId()).ifPresent(userIds::add);
            Optional.ofNullable(comment.getChildren()).ifPresent(children -> collectUserIds(children, userIds));
        });
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
