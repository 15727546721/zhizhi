package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.converter.UserVOConverter;
import cn.xu.api.web.model.dto.comment.CommentCreateRequest;
import cn.xu.api.web.model.dto.comment.FindCommentRequest;
import cn.xu.api.web.model.dto.comment.FindReplyRequest;
import cn.xu.api.web.model.dto.report.ReportRequestDTO;
import cn.xu.api.web.model.vo.comment.CommentResponse;
import cn.xu.application.service.LikeApplicationService;
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
import cn.xu.domain.post.service.IPostService;
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
    private LikeApplicationService likeApplicationService;
    @Resource
    private IPostService postService;
    @Resource
    private ReportDomainService reportDomainService;
    @Resource
    private CommentValidationService commentValidationService;
    @Resource
    private UserVOConverter userVOConverter;
    @Resource
    private cn.xu.domain.comment.repository.ICommentRepository commentRepository;
    @Resource
    private cn.xu.domain.comment.service.CommentAggregateDomainService commentAggregateDomainService;

    @PostMapping("/list")
    @Operation(summary = "获取评论列表")
    @ApiOperationLog(description = "获取评论列表")
    public ResponseEntity<List<CommentResponse>> getCommentList(@RequestBody FindCommentRequest findCommentReq) {
        // 参数校验
        commentValidationService.validatePageParams(findCommentReq.getPageNo(), findCommentReq.getPageSize());
        
        List<CommentEntity> commentEntities = commentService.findCommentListWithPreview(findCommentReq);
        // 将CommentEntity转换为CommentResponse（包含点赞状态）
        List<CommentResponse> commentVOList = convertToCommentVOList(commentEntities, findCommentReq.getTargetId());
        return ResponseEntity.<List<CommentResponse>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(commentVOList)
                .build();
    }

    /**
     * 将CommentEntity列表转换为CommentResponse列表（包含点赞状态）
     * @param commentEntities 评论实体列表
     * @param postId 帖子ID（用于查询帖子作者）
     * @return 评论Response列表
     */
    private List<CommentResponse> convertToCommentVOList(List<CommentEntity> commentEntities, Long postId) {
        if (commentEntities == null || commentEntities.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 获取当前登录用户ID（可能为null，未登录时）
        Long currentUserId = null;
        try {
            currentUserId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 用户未登录，currentUserId保持为null
            log.debug("用户未登录，无法获取用户ID");
        }
        
        // 获取帖子作者ID（如果postId不为null且targetType为POST）
        Long authorId = null;
        if (postId != null) {
            try {
                authorId = postService.findPostEntityById(postId)
                        .map(post -> post.getUserId())
                        .orElse(null);
            } catch (Exception e) {
                log.warn("查询帖子作者失败，postId: {}", postId, e);
            }
        }
        
        // 收集所有评论ID（包括子评论）
        List<Long> allCommentIds = collectAllCommentIds(commentEntities);
        
        // 批量查询当前用户的点赞状态
        java.util.Map<Long, Boolean> currentUserLikeMap = new java.util.HashMap<>();
        if (currentUserId != null && !allCommentIds.isEmpty()) {
            currentUserLikeMap = likeApplicationService.batchCheckLikeStatus(currentUserId, allCommentIds, LikeType.COMMENT);
        }
        
        // 批量查询作者的点赞状态
        java.util.Map<Long, Boolean> authorLikeMap = new java.util.HashMap<>();
        if (authorId != null && !allCommentIds.isEmpty() && !authorId.equals(currentUserId)) {
            authorLikeMap = likeApplicationService.batchCheckLikeStatus(authorId, allCommentIds, LikeType.COMMENT);
        } else if (authorId != null && authorId.equals(currentUserId)) {
            // 如果作者就是当前用户，复用currentUserLikeMap
            authorLikeMap = currentUserLikeMap;
        }
        
        // 转换为VO列表
        List<CommentResponse> commentVOList = new ArrayList<>();
        for (CommentEntity entity : commentEntities) {
            CommentResponse vo = convertToCommentVO(entity, currentUserLikeMap, authorLikeMap);
            commentVOList.add(vo);
        }
        return commentVOList;
    }
    
    /**
     * 收集所有评论ID（包括子评论）
     * @param commentEntities 评论实体列表
     * @return 评论ID列表
     */
    private List<Long> collectAllCommentIds(List<CommentEntity> commentEntities) {
        List<Long> commentIds = new ArrayList<>();
        if (commentEntities == null || commentEntities.isEmpty()) {
            return commentIds;
        }
        
        for (CommentEntity entity : commentEntities) {
            if (entity.getId() != null) {
                commentIds.add(entity.getId());
            }
            // 递归收集子评论ID
            if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
                commentIds.addAll(collectAllCommentIds(entity.getChildren()));
            }
        }
        return commentIds;
    }
    
    /**
     * 将CommentEntity转换为CommentResponse（包含点赞状态）
     * @param entity 评论实体
     * @param currentUserLikeMap 当前用户点赞状态Map
     * @param authorLikeMap 作者点赞状态Map
     * @return 评论Response
     */
    private CommentResponse convertToCommentVO(CommentEntity entity, 
                                               java.util.Map<Long, Boolean> currentUserLikeMap,
                                               java.util.Map<Long, Boolean> authorLikeMap) {
        if (entity == null) {
            return null;
        }
        
        // 获取当前用户是否点赞
        Boolean isLiked = currentUserLikeMap.getOrDefault(entity.getId(), false);
        
        // 获取作者是否点赞
        Boolean isAuthorLiked = authorLikeMap.getOrDefault(entity.getId(), false);
        
        // 递归转换子评论
        List<CommentResponse> children = new ArrayList<>();
        if (entity.getChildren() != null && !entity.getChildren().isEmpty()) {
            for (CommentEntity childEntity : entity.getChildren()) {
                CommentResponse childVO = convertToCommentVO(childEntity, currentUserLikeMap, authorLikeMap);
                if (childVO != null) {
                    children.add(childVO);
                }
            }
        }
        
        return CommentResponse.builder()
                .id(entity.getId())
                .type(entity.getTargetType())
                .targetId(entity.getTargetId())
                .parentId(entity.getParentId())
                // 使用UserVOConverter转换用户信息
                .user(userVOConverter.convertToCommentUserResponse(entity.getUser()))
                .replyUser(userVOConverter.convertToCommentUserResponse(entity.getReplyUser()))
                .content(entity.getContentValue())
                .imageUrls(entity.getImageUrls())
                .likeCount(entity.getLikeCount() != null ? entity.getLikeCount().intValue() : 0)
                .isLiked(isLiked)
                .isAuthorLiked(isAuthorLiked)
                .replyCount(entity.getReplyCount() != null ? entity.getReplyCount().intValue() : 0)
                .children(children)
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
            // 将CommentEntity转换为CommentResponse（包含点赞状态，但回复列表不需要查询帖子作者）
            // 注意：回复列表没有postId，所以isAuthorLiked会为false
            List<CommentResponse> commentVOList = convertToCommentVOList(commentEntities, null);
            return ResponseEntity.<List<CommentResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(commentVOList)
                    .build();
        } catch (Exception e) {
            log.error("获取评论回复列表失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取评论回复列表失败");
        }
    }
    
    /**
     * 获取用户评论列表
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 用户评论列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户评论列表")
    @ApiOperationLog(description = "获取用户评论列表")
    public ResponseEntity<cn.xu.common.response.PageResponse<java.util.List<cn.xu.api.web.model.vo.user.UserCommentItemVO>>> getUserComments(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            // 参数校验
            commentValidationService.validatePageParams(pageNo, pageSize);
            
            // 计算偏移量
            int offset = (pageNo - 1) * pageSize;
            
            // 查询评论列表
            List<CommentEntity> commentEntities = commentRepository.findByUserId(userId, offset, pageSize);
            
            // 统计总数
            Long total = commentRepository.countByUserId(userId);
            
            // 转换为VO列表
            List<cn.xu.api.web.model.vo.user.UserCommentItemVO> commentItems = convertToUserCommentItemVOList(commentEntities);
            
            // 构建分页响应
            cn.xu.common.response.PageResponse<java.util.List<cn.xu.api.web.model.vo.user.UserCommentItemVO>> pageResponse = 
                    cn.xu.common.response.PageResponse.ofList(
                            pageNo, pageSize, total != null ? total : 0L, commentItems);
            
            return ResponseEntity.<cn.xu.common.response.PageResponse<java.util.List<cn.xu.api.web.model.vo.user.UserCommentItemVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取用户评论列表失败，userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取用户评论列表失败");
        }
    }
    
    /**
     * 将评论实体列表转换为用户评论项VO列表
     * @param commentEntities 评论实体列表
     * @return 用户评论项VO列表
     */
    private List<cn.xu.api.web.model.vo.user.UserCommentItemVO> convertToUserCommentItemVOList(List<CommentEntity> commentEntities) {
        if (commentEntities == null || commentEntities.isEmpty()) {
            return new ArrayList<>();
        }
        
        // 收集所有目标ID
        java.util.Set<Long> targetIds = new java.util.HashSet<>();
        for (CommentEntity entity : commentEntities) {
            if (entity.getTargetId() != null && entity.getTargetType() != null && entity.getTargetType() == 1) {
                // 只处理帖子类型的评论
                targetIds.add(entity.getTargetId());
            }
        }
        
        // 批量查询帖子信息
        java.util.Map<Long, String> targetTitleMap = new java.util.HashMap<>();
        if (!targetIds.isEmpty()) {
            try {
                java.util.List<Long> targetIdList = new java.util.ArrayList<>(targetIds);
                for (Long targetId : targetIdList) {
                    try {
                        java.util.Optional<cn.xu.domain.post.model.entity.PostEntity> postOpt = 
                                postService.findPostEntityById(targetId);
                        if (postOpt.isPresent()) {
                            cn.xu.domain.post.model.entity.PostEntity post = postOpt.get();
                            String title = post.getTitle() != null ? post.getTitle().getValue() : "无标题";
                            targetTitleMap.put(targetId, title);
                        }
                    } catch (Exception e) {
                        log.warn("查询帖子信息失败，targetId: {}", targetId, e);
                    }
                }
            } catch (Exception e) {
                log.warn("批量查询帖子信息失败", e);
            }
        }
        
        // 获取当前登录用户ID（可能为null）
        Long currentUserId = null;
        try {
            currentUserId = StpUtil.getLoginIdAsLong();
        } catch (Exception e) {
            // 用户未登录
        }
        
        // 收集所有评论ID
        List<Long> commentIds = new ArrayList<>();
        for (CommentEntity entity : commentEntities) {
            if (entity.getId() != null) {
                commentIds.add(entity.getId());
            }
        }
        
        // 批量查询点赞状态
        java.util.Map<Long, Boolean> likeMap = new java.util.HashMap<>();
        if (currentUserId != null && !commentIds.isEmpty()) {
            likeMap = likeApplicationService.batchCheckLikeStatus(currentUserId, commentIds, LikeType.COMMENT);
        }
        
        // 填充用户信息
        commentAggregateDomainService.fillUserInfo(commentEntities);
        
        // 转换为VO列表
        List<cn.xu.api.web.model.vo.user.UserCommentItemVO> result = new ArrayList<>();
        for (CommentEntity entity : commentEntities) {
            // 转换评论为CommentResponse
            CommentResponse commentResponse = convertToCommentVO(entity, likeMap, new java.util.HashMap<>());
            
            // 获取目标信息
            String targetTitle = targetTitleMap.getOrDefault(entity.getTargetId(), "无标题");
            String targetUrl = "/post/" + entity.getTargetId();
            
            cn.xu.api.web.model.vo.user.UserCommentItemVO item = 
                    cn.xu.api.web.model.vo.user.UserCommentItemVO.builder()
                            .comment(commentResponse)
                            .targetId(entity.getTargetId())
                            .targetTitle(targetTitle)
                            .targetType(entity.getTargetType())
                            .targetUrl(targetUrl)
                            .build();
            result.add(item);
        }
        
        return result;
    }
}