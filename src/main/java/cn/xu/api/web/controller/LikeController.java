package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.like.LikeCountResponse;
import cn.xu.api.web.model.dto.like.LikeRequest;
import cn.xu.application.service.LikeApplicationService;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.LikeStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Tag(name = "点赞接口")
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeApplicationService likeApplicationService;
    private final LikeStatisticsService likeStatisticsService;

    @Operation(summary = "点赞")
    @PostMapping("/like")
    @ApiOperationLog(description = "点赞")
    public ResponseEntity<Void> like(@Valid @RequestBody LikeRequest request) {
        try {
            // 从登录上下文获取用户ID，确保安全性
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 将String类型的type转换为LikeType枚举
            LikeType likeType = parseLikeType(request.getType());
            likeApplicationService.doLike(
                    userId, // 使用从登录上下文获取的userId，而不是请求中的userId
                    request.getTargetId(),
                    likeType); // 使用LikeType枚举而不是getCode()
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
            
            // 将String类型的type转换为LikeType枚举
            LikeType likeType = parseLikeType(request.getType());
            likeApplicationService.cancelLike(
                    userId, // 使用从登录上下文获取的userId，而不是请求中的userId
                    request.getTargetId(),
                    likeType); // 使用LikeType枚举而不是getCode()
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
            
            LikeType likeType = parseLikeType(type);
            boolean status = likeApplicationService.checkLikeStatus(userId, targetId, likeType); // 使用LikeType枚举
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
            LikeType likeType = parseLikeType(type);
            Long count = likeApplicationService.getLikeCount(targetId, likeType); // 使用LikeType枚举
            
            // 从登录上下文获取用户ID，检查是否已点赞
            Boolean liked = null;
            try {
                Long userId = StpUtil.getLoginIdAsLong();
                liked = likeApplicationService.checkLikeStatus(userId, targetId, likeType); // 使用LikeType枚举
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
                        LikeType likeType = parseLikeType(request.getType());
                        Long count = likeApplicationService.getLikeCount(
                                request.getTargetId(), 
                                likeType // 使用LikeType枚举
                        );
                        
                        Boolean liked = null;
                        if (finalUserId != null) {
                            liked = likeApplicationService.checkLikeStatus(
                                    finalUserId, // 使用从登录上下文获取的userId
                                    request.getTargetId(), 
                                    likeType // 使用LikeType枚举
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
     * 解析点赞类型字符串
     * @param type 点赞类型字符串，可以是数字或枚举名称
     * @return LikeType枚举
     */
    private LikeType parseLikeType(String type) {
        // 首先尝试按名称解析
        LikeType likeType = LikeType.fromName(type);
        if (likeType != null) {
            return likeType;
        }
        
        // 如果按名称解析失败，尝试按数字解析
        try {
            int code = Integer.parseInt(type);
            likeType = LikeType.valueOf(code);
            if (likeType != null) {
                return likeType;
            }
        } catch (NumberFormatException e) {
            // 解析失败，继续抛出异常
            log.error("不存在点赞类型: {}", type, e);
        }
        
        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "无效的点赞类型: " + type);
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
}