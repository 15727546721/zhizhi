package cn.xu.api.web.controller.comment;

import cn.xu.api.web.controller.comment.dto.CommentListDTO;
import cn.xu.api.web.controller.comment.request.CommentRequest;
import cn.xu.api.web.model.dto.common.ResponseEntity;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.model.valueobject.CommentType;
import cn.xu.domain.comment.service.ICommentService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
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
    @PostMapping("/add")
    public ResponseEntity addComment(@Valid @RequestBody CommentRequest request) {
        commentService.saveComment(request);
        return ResponseEntity.builder()
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
    public ResponseEntity<List<CommentListDTO>> getCommentList(
            @RequestParam("type") Integer type,
            @RequestParam("targetId") Long targetId,
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        CommentType commentType = CommentType.of(type);
        List<CommentEntity> commentEntityList = commentService.getPagedComments(commentType, targetId, pageNum, pageSize);

        // 收集所有用户ID（包括子评论的用户）
        Set<Long> userIds = new HashSet<>();
        collectUserIds(commentEntityList, userIds);

        // 批量获取用户信息
        Map<Long, UserEntity> userInfoMap = userService.getBatchUserInfo(userIds);

        // 转换为DTO
        List<CommentListDTO> commentListDTOList = convertToDTO(commentEntityList, userInfoMap);

        return ResponseEntity.<List<CommentListDTO>>builder()
                .data(commentListDTOList)
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取评论列表成功")
                .build();
    }

    private List<CommentListDTO> convertToDTO(List<CommentEntity> commentEntities, Map<Long, UserEntity> userInfoMap) {
        if (commentEntities == null || commentEntities.isEmpty()) {
            return Collections.emptyList();
        }

        return commentEntities.stream()
                .map(commentEntity -> convertSingleCommentToDTO(commentEntity, userInfoMap))
                .collect(Collectors.toList());
    }

    private CommentListDTO convertSingleCommentToDTO(CommentEntity commentEntity, Map<Long, UserEntity> userInfoMap) {
        return CommentListDTO.builder()
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
