package cn.xu.controller.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.like.LikeCountResponse;
import cn.xu.model.dto.like.LikeRequest;
import cn.xu.model.entity.Comment;
import cn.xu.model.entity.Like;
import cn.xu.model.entity.Post;
import cn.xu.model.vo.user.UserLikeItemVO;
import cn.xu.repository.ICommentRepository;
import cn.xu.service.like.LikeService;
import cn.xu.service.like.LikeStatisticsService;
import cn.xu.service.post.PostService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 点赞控制器
 * 
 * <p>提供点赞、取消点赞、查询点赞状态、获取点赞数等功能接口
 * 
 * @author xu
 * @since 2025-11-25
 */
@Slf4j
@Tag(name = "点赞接口", description = "点赞相关API")
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;
    private final LikeStatisticsService likeStatisticsService;
    private final PostService postService;
    private final ICommentRepository commentRepository;

    @Operation(summary = "点赞")
    @PostMapping("/like")
    @ApiOperationLog(description = "点赞")
    public ResponseEntity<Void> like(@Valid @RequestBody LikeRequest request) {
        try {
            // 从登录上下文获取用户ID，确保安全性
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 将String类型的type转换为Like.LikeType枚举
            Like.LikeType likeType = parseLikeType(request.getType());
            likeService.like(
                    userId,
                    likeType.getCode(),
                    request.getTargetId());
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("点赞成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("点赞失败: {}", e.getMessage());
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("点赞异常", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("点赞失败，请稍后重试")
                    .build();
        }
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/unlike")
    @ApiOperationLog(description = "取消点赞")
    public ResponseEntity<Void> unlike(@Valid @RequestBody LikeRequest request) {
        try {
            // 从登录上下文获取用户ID，确保安全性
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 将String类型的type转换为Like.LikeType枚举
            Like.LikeType likeType = parseLikeType(request.getType());
            likeService.unlike(
                    userId,
                    likeType.getCode(),
                    request.getTargetId());
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("取消点赞成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("取消点赞失败: {}", e.getMessage());
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("取消点赞异常", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("取消点赞失败，请稍后重试")
                    .build();
        }
    }
    
    @Operation(summary = "检查点赞状态")
    @GetMapping("/status")
    @ApiOperationLog(description = "检查点赞状态")
    public ResponseEntity<Boolean> checkStatus(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "点赞类型") @RequestParam String type) {
        try {
            // 从登录上下文获取用户ID，确保安全性
            Long userId = StpUtil.getLoginIdAsLong();
            
            Like.LikeType likeType = parseLikeType(type);
            boolean status = likeService.checkStatus(userId, likeType.getCode(), targetId);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(status)
                    .build();
        } catch (Exception e) {
            log.error("检查点赞状态异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败，请稍后重试")
                    .data(false)
                    .build();
        }
    }
    
    @Operation(summary = "获取点赞数")
    @GetMapping("/count")
    @ApiOperationLog(description = "获取点赞数")
    public ResponseEntity<LikeCountResponse> getLikeCount(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "点赞类型") @RequestParam String type) {
        try {
            Like.LikeType likeType = parseLikeType(type);
            Long count = likeService.getLikeCount(targetId, likeType.getCode());
            
            // 从登录上下文获取用户ID，检查是否已点赞
            Boolean liked = null;
            try {
                Long userId = StpUtil.getLoginIdAsLong();
                liked = likeService.checkStatus(userId, likeType.getCode(), targetId);
            } catch (Exception e) {
                // 用户未登录，liked保持为null
                log.debug("用户未登录，不返回点赞状态");
            }
            
            LikeCountResponse response = new LikeCountResponse(targetId, type, count, liked);
            return ResponseEntity.<LikeCountResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(response)
                    .build();
        } catch (Exception e) {
            log.error("获取点赞数异常", e);
            return ResponseEntity.<LikeCountResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败，请稍后重试")
                    .build();
        }
    }
    
    @Operation(summary = "批量获取点赞数")
    @PostMapping("/counts")
    @ApiOperationLog(description = "批量获取点赞数")
    public ResponseEntity<List<LikeCountResponse>> getLikeCounts(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "点赞请求列表") @RequestBody List<LikeRequest> requests) {
        try {
            // 从登录上下文获取用户ID，确保安全性
            Long userId = null;
            try {
                userId = StpUtil.getLoginIdAsLong();
            } catch (Exception e) {
                log.debug("用户未登录，批量查询不返回点赞状态");
            }
            
            final Long finalUserId = userId; // 用于lambda表达式
            List<LikeCountResponse> responses = requests.stream()
                    .map(request -> {
                        Like.LikeType likeType = parseLikeType(request.getType());
                        Long count = likeService.getLikeCount(
                                request.getTargetId(), 
                                likeType.getCode()
                        );
                        
                        Boolean liked = null;
                        if (finalUserId != null) {
                            liked = likeService.checkStatus(
                                    finalUserId,
                                    likeType.getCode(),
                                    request.getTargetId()
                            );
                        }
                        
                        return new LikeCountResponse(
                                request.getTargetId(), 
                                request.getType(), 
                                count, 
                                liked
                        );
                    })
                    .collect(Collectors.toList());
                    
            return ResponseEntity.<List<LikeCountResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(responses)
                    .build();
        } catch (Exception e) {
            log.error("批量获取点赞数异常", e);
            return ResponseEntity.<List<LikeCountResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败，请稍后重试")
                    .build();
        }
    }
    
    /**
     * 解析点赞类型字符串（简化版）
     * @param type 点赞类型字符串，可以是数字或枚举名称
     * @return Like.LikeType枚举
     */
    private Like.LikeType parseLikeType(String type) {
        // 尝试按名称解析
        Like.LikeType likeType = Like.LikeType.fromName(type);
        if (likeType != null) {
            return likeType;
        }
        
        // 尝试按数字解析
        try {
            int code = Integer.parseInt(type);
            return Like.LikeType.fromCode(code);
        } catch (Exception e) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "无效的点赞类型: " + type);
        }
    }
    
    @Operation(summary = "获取用户点赞统计")
    @GetMapping("/user/statistics")
    @ApiOperationLog(description = "获取用户点赞统计")
    public ResponseEntity<LikeStatisticsService.UserLikeStatistics> getUserStatistics(
            @Parameter(description = "用户ID") @RequestParam Long userId) {
        LikeStatisticsService.UserLikeStatistics statistics = 
                likeStatisticsService.getUserLikeStatistics(userId);
        return ResponseEntity.<LikeStatisticsService.UserLikeStatistics>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(statistics)
                .build();
    }
    
    /**
     * 获取用户点赞列表
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @return 用户点赞列表
     */
    @GetMapping("/user/{userId}")
    @Operation(summary = "获取用户点赞列表")
    @ApiOperationLog(description = "获取用户点赞列表")
    public ResponseEntity<PageResponse<List<UserLikeItemVO>>> getUserLikes(
            @PathVariable("userId") Long userId,
            @RequestParam(value = "pageNo", defaultValue = "1") Integer pageNo,
            @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        try {
            // 获取用户点赞列表
            List<Like> likes = likeService.getUserLikes(userId, pageNo, pageSize);
            
            // 获取总数
            long total = likeService.countUserLikes(userId);
            
            // 转换为VO
            List<UserLikeItemVO> voList = likes.stream()
                    .map(this::convertToVO)
                    .collect(Collectors.toList());
            
            PageResponse<List<UserLikeItemVO>> pageResponse = 
                    PageResponse.ofList(pageNo, pageSize, total, voList);
            
            return ResponseEntity.<PageResponse<List<UserLikeItemVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("查询成功")
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("[点赞控制器] 获取用户点赞列表失败 - userId: {}", userId, e);
            return ResponseEntity.<PageResponse<List<UserLikeItemVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败，请稍后重试")
                    .build();
        }
    }
    
    /**
     * 转换Like为UserLikeItemVO
     */
    private UserLikeItemVO convertToVO(Like like) {
        Like.LikeType likeType = Like.LikeType.fromCode(like.getType());
        
        UserLikeItemVO vo = UserLikeItemVO.builder()
                .likeId(like.getId())
                .targetId(like.getTargetId())
                .targetType(like.getType())
                .targetTypeName(likeType.getDesc())
                .likeTime(like.getCreateTime())
                .build();
        
        // 根据类型获取目标详情（标题和链接）
        try {
            switch (likeType) {
                case POST:
                    // 获取帖子信息
                    Optional<Post> postOpt = postService.getPostById(like.getTargetId());
                    if (postOpt.isPresent()) {
                        Post post = postOpt.get();
                        vo.setTargetTitle(post.getTitle());
                        vo.setTargetUrl("/post/" + like.getTargetId());
                    }
                    break;
                case COMMENT:
                    // 获取评论信息
                    Comment comment = commentRepository.findById(like.getTargetId());
                    if (comment != null) {
                        String contentValue = comment.getContent();
                        vo.setTargetTitle(contentValue != null && contentValue.length() > 50 
                                ? contentValue.substring(0, 50) + "..." 
                                : contentValue);
                        vo.setTargetUrl("/post/" + comment.getTargetId() + "#comment-" + like.getTargetId());
                    }
                    break;
                case ESSAY:
                    // 随笔暂不支持
                    vo.setTargetTitle("随笔-" + like.getTargetId());
                    vo.setTargetUrl("/essay/" + like.getTargetId());
                    break;
                default:
                    vo.setTargetTitle("未知类型");
                    break;
            }
        } catch (Exception e) {
            log.warn("[点赞控制器] 获取目标详情失败 - targetId: {}, type: {}", 
                    like.getTargetId(), like.getType(), e);
            vo.setTargetTitle("目标已删除");
        }
        
        return vo;
    }
}
