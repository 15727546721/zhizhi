package cn.xu.api.controller.web.comment;


import cn.xu.common.ResponseEntity;
import cn.xu.domain.comment.model.CommentEntity;
import cn.xu.domain.comment.service.ICommentService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.infrastructure.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Tag(name = "评论接口", description = "评论相关接口")
@RestController
@RequestMapping("/api/comment")
public class CommentApiController {

    @Resource
    private ICommentService commentService;
    @Resource
    private IUserService userService;

    @Operation(summary = "评论")
    @PostMapping("/add")
    public ResponseEntity addComment(@RequestBody CommentRequest comment) {
        commentService.addComment(comment);
        return ResponseEntity.builder()
                .info("评论成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @Operation(summary = "回复评论")
    @PostMapping("/reply")
    public ResponseEntity replyComment(@RequestBody CommentRequest comment) {
        commentService.replyComment(comment);
        return ResponseEntity.builder()
                .info("回复评论成功")
                .code(ResponseCode.SUCCESS.getCode())
                .build();
    }

    @Operation(summary = "获取文章评论列表")
    @GetMapping("/getArticleComments/{articleId}")
    public ResponseEntity<List<CommentListDTO>> getArticleComments(@PathVariable("articleId") Long articleId) {
        List<CommentEntity> commentEntityList = commentService.getArticleComments(articleId);

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
                .build();
    }

    private List<CommentListDTO> convertToDTO(List<CommentEntity> commentEntities, Map<Long, UserEntity> userInfoMap) {
        if (commentEntities == null || commentEntities.isEmpty() || userInfoMap == null || userInfoMap.isEmpty()) {
            return Collections.emptyList();
        }

        return commentEntities.stream()
                .map(commentEntity -> CommentListDTO.builder()
                        .id(commentEntity.getId())
                        .userId(commentEntity.getUserId())
                        .replyToUserId(commentEntity.getReplyToUserId())
                        .content(commentEntity.getContent())
                        .createTime(commentEntity.getCreateTime())
                        .userInfo(userInfoMap.get(commentEntity.getUserId()))
                        .replyComment(convertToDTO(commentEntity.getChildren(), userInfoMap))
                        .build())
                .collect(Collectors.toList());
    }

    // 递归收集用户ID
    private void collectUserIds(List<CommentEntity> comments, Set<Long> userIds) {
        if (comments == null || comments.isEmpty()) {
            return;
        }

        for (CommentEntity comment : comments) {
            userIds.add(comment.getUserId());
            if (comment.getChildren() != null) {
                collectUserIds(comment.getChildren(), userIds);
            }
        }
    }
}
