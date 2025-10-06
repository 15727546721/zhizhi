package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.comment.CommentCreateRequest;
import cn.xu.api.web.model.dto.comment.FindCommentRequest;
import cn.xu.api.web.model.dto.comment.FindReplyRequest;
import cn.xu.api.web.model.dto.report.ReportRequestDTO;
import cn.xu.api.web.model.vo.comment.CommentResponse;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.comment.event.CommentCreatedEvent;
import cn.xu.domain.comment.model.entity.CommentEntity;
import cn.xu.domain.comment.service.CommentValidationService;
import cn.xu.domain.comment.service.ICommentService;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.domain.report.model.entity.ReportEntity;
import cn.xu.domain.report.service.ReportDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/comment")
@Tag(name = "评论接口", description = "评论相关接口")
public class CommentController {

    @Resource
    private ICommentService commentService;
    @Resource
    private ILikeService likeService;
    @Resource
    private ReportDomainService reportDomainService;
    @Resource
    private CommentValidationService commentValidationService;

    @PostMapping("/list")
    @Operation(summary = "获取评论列表")
    @ApiOperationLog(description = "获取评论列表")
    public ResponseEntity<List<CommentResponse>> getCommentList(@RequestBody FindCommentRequest findCommentReq) {
        // 参数校验
        commentValidationService.validatePageParams(findCommentReq.getPageNo(), findCommentReq.getPageSize());
        
        List<CommentEntity> commentEntities = commentService.findCommentListWithPreview(findCommentReq);
        // 将CommentEntity转换为CommentResponse
        List<CommentResponse> commentVOList = convertToCommentVOList(commentEntities);
        return ResponseEntity.<List<CommentResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(commentVOList)
                .build();
    }

    /**
     * 将CommentEntity列表转换为CommentResponse列表
     * @param commentEntities 评论实体列表
     * @return 评论Response列表
     */
    private List<CommentResponse> convertToCommentVOList(List<CommentEntity> commentEntities) {
        if (commentEntities == null || commentEntities.isEmpty()) {
            return new ArrayList<>();
        }
        
        List<CommentResponse> commentVOList = new ArrayList<>();
        for (CommentEntity entity : commentEntities) {
            CommentResponse vo = convertToCommentVO(entity);
            commentVOList.add(vo);
        }
        return commentVOList;
    }
    
    /**
     * 将CommentEntity转换为CommentResponse
     * @param entity 评论实体
     * @return 评论Response
     */
    private CommentResponse convertToCommentVO(CommentEntity entity) {
        if (entity == null) {
            return null;
        }
        
        return CommentResponse.builder()
                .id(entity.getId())
                .type(entity.getTargetType())
                .targetId(entity.getTargetId())
                .parentId(entity.getParentId())
                .user(entity.getUser())
                .replyUser(entity.getReplyUser())
                .content(entity.getContentValue())
                .imageUrls(entity.getImageUrls())
                .likeCount(entity.getLikeCount() != null ? entity.getLikeCount().intValue() : 0)
                .replyCount(entity.getReplyCount() != null ? entity.getReplyCount().intValue() : 0)
                .children(convertToCommentVOList(entity.getChildren()))
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    @PostMapping("/add")
    @SaCheckLogin
    @Operation(summary = "添加评论")
    @ApiOperationLog(description = "添加评论")
    public ResponseEntity<String> addComment(@RequestBody CommentCreateRequest commentRequest) {
        try {
            // 参数校验
            commentValidationService.validateCommentCreateParams(
                commentRequest.getType(),
                commentRequest.getTargetId(),
                commentRequest.getContent()
            );
            
            Long userId = StpUtil.getLoginIdAsLong();
            // 需要创建CommentCreatedEvent对象
            CommentCreatedEvent event = CommentCreatedEvent.builder()
                    .targetType(commentRequest.getType())
                    .targetId(commentRequest.getTargetId())
                    .parentId(commentRequest.getParentId())
                    .userId(userId)
                    .replyUserId(commentRequest.getReplyUserId())
                    .content(commentRequest.getContent())
                    .createTime(LocalDateTime.now())
                    .imageUrls(Collections.emptyList()) // 如果需要图片URL，可以从commentRequest中获取
                    .build();
            commentService.saveComment(event);
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("评论成功")
                    .build();
        } catch (Exception e) {
            log.error("评论失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论失败");
        }
    }

    @PostMapping("/reply")
    @SaCheckLogin
    @Operation(summary = "回复评论")
    @ApiOperationLog(description = "回复评论")
    public ResponseEntity<String> replyComment(@RequestBody CommentCreateRequest commentRequest) {
        try {
            // 参数校验
            commentValidationService.validateCommentReplyParams(
                commentRequest.getType(),
                commentRequest.getTargetId(),
                commentRequest.getParentId(),
                commentRequest.getReplyUserId(),
                commentRequest.getContent()
            );
            
            Long userId = StpUtil.getLoginIdAsLong();
            // 需要创建CommentCreatedEvent对象
            CommentCreatedEvent event = CommentCreatedEvent.builder()
                    .targetType(commentRequest.getType())
                    .targetId(commentRequest.getTargetId())
                    .parentId(commentRequest.getParentId())
                    .userId(userId)
                    .replyUserId(commentRequest.getReplyUserId())
                    .content(commentRequest.getContent())
                    .createTime(LocalDateTime.now())
                    .imageUrls(Collections.emptyList()) // 如果需要图片URL，可以从commentRequest中获取
                    .build();
            commentService.saveComment(event);
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("回复成功")
                    .build();
        } catch (Exception e) {
            log.error("回复评论失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "回复评论失败");
        }
    }

    @DeleteMapping("/delete/{id}")
    @SaCheckLogin
    @Operation(summary = "删除评论")
    @ApiOperationLog(description = "删除评论")
    public ResponseEntity<String> deleteComment(@PathVariable("id") Long id) {
        try {
            // Long userId = StpUtil.getLoginIdAsLong();
            commentService.deleteComment(id);
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("删除成功")
                    .build();
        } catch (Exception e) {
            log.error("删除评论失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "删除评论失败");
        }
    }

    @GetMapping("/like/{id}")
    @SaCheckLogin
    @Operation(summary = "点赞评论")
    @ApiOperationLog(description = "点赞评论")
    public ResponseEntity<String> likeComment(@PathVariable("id") Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            likeService.like(userId, LikeType.COMMENT, id);
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("点赞成功")
                    .build();
        } catch (Exception e) {
            log.error("点赞失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "点赞失败");
        }
    }

    @GetMapping("/unlike/{id}")
    @SaCheckLogin
    @Operation(summary = "取消点赞评论")
    @ApiOperationLog(description = "取消点赞评论")
    public ResponseEntity<String> unlikeComment(@PathVariable("id") Long id) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            likeService.unlike(userId, LikeType.COMMENT, id);
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("取消点赞成功")
                    .build();
        } catch (Exception e) {
            log.error("取消点赞失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "取消点赞失败");
        }
    }
    
    @PostMapping("/report")
    @SaCheckLogin
    @Operation(summary = "举报评论")
    @ApiOperationLog(description = "举报评论")
    public ResponseEntity<Long> reportComment(@RequestBody ReportRequestDTO requestDTO) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 创建举报
            ReportEntity report = reportDomainService.createReport(
                    userId,
                    ReportEntity.ReportType.COMMENT.getCode(), // 评论类型
                    requestDTO.getTargetId(), // 评论ID
                    requestDTO.getReason(),
                    requestDTO.getDetail()
            );
            
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(report.getId())
                    .info("举报成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("举报评论失败: {}", e.getMessage());
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("举报评论异常", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("举报失败，请稍后重试")
                    .build();
        }
    }

    @PostMapping("/reply/list")
    @Operation(summary = "获取评论回复列表")
    @ApiOperationLog(description = "获取评论回复列表")
    public ResponseEntity<List<CommentResponse>> getReplyList(@RequestBody FindReplyRequest findReplyRequest) {
        try {
            // 参数校验
            commentValidationService.validatePageParams(findReplyRequest.getPageNo(), findReplyRequest.getPageSize());
            
            List<CommentEntity> commentEntities = commentService.findChildCommentList(findReplyRequest);
            // 将CommentEntity转换为CommentResponse
            List<CommentResponse> commentVOList = convertToCommentVOList(commentEntities);
            return ResponseEntity.<List<CommentResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(commentVOList)
                    .build();
        } catch (Exception e) {
            log.error("获取评论回复列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取评论回复列表失败");
        }
    }
}