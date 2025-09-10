package cn.xu.api.web.controller;

import cn.xu.api.web.model.dto.like.LikeCountResponse;
import cn.xu.api.web.model.dto.like.LikeRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.application.service.LikeApplicationService;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.LikeStatisticsService;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
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
    public ResponseEntity<Void> like(@Valid @RequestBody LikeRequest request) {
        // 将String类型的type转换为Integer
        LikeType likeType = LikeType.valueOf(Integer.parseInt(request.getType()));
        likeApplicationService.doLike(
                request.getUserId(),
                request.getTargetId(),
                likeType.getCode());
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("点赞成功")
                .build();
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/unlike")
    public ResponseEntity<Void> unlike(@Valid @RequestBody LikeRequest request) {
        // 将String类型的type转换为Integer
        LikeType likeType = LikeType.valueOf(Integer.parseInt(request.getType()));
        likeApplicationService.cancelLike(
                request.getUserId(),
                request.getTargetId(),
                likeType.getCode());
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("取消点赞成功")
                .build();
    }
    
    @Operation(summary = "检查点赞状态")
    @GetMapping("/status")
    public ResponseEntity<Boolean> checkStatus(
            @RequestParam Long userId,
            @RequestParam Long targetId,
            @RequestParam String type) {
        LikeType likeType = LikeType.valueOf(Integer.parseInt(type));
        boolean status = likeApplicationService.checkLikeStatus(userId, targetId, likeType.getCode());
        return ResponseEntity.<Boolean>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(status)
                .build();
    }
    
    @Operation(summary = "获取点赞数")
    @GetMapping("/count")
    public ResponseEntity<LikeCountResponse> getLikeCount(
            @RequestParam Long targetId,
            @RequestParam String type,
            @RequestParam(required = false) Long userId) {
        LikeType likeType = LikeType.valueOf(Integer.parseInt(type));
        Long count = likeApplicationService.getLikeCount(targetId, likeType.getCode());
        
        // 如果提供了userId，检查是否已点赞
        Boolean liked = null;
        if (userId != null) {
            liked = likeApplicationService.checkLikeStatus(userId, targetId, likeType.getCode());
        }
        
        LikeCountResponse response = new LikeCountResponse(targetId, type, count, liked);
        return ResponseEntity.<LikeCountResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(response)
                .build();
    }
    
    @Operation(summary = "批量获取点赞数")
    @PostMapping("/counts")
    public ResponseEntity<List<LikeCountResponse>> getLikeCounts(
            @RequestBody List<LikeRequest> requests) {
        List<LikeCountResponse> responses = requests.stream()
                .map(request -> {
                    LikeType likeType = LikeType.valueOf(Integer.parseInt(request.getType()));
                    Long count = likeApplicationService.getLikeCount(
                            request.getTargetId(), 
                            likeType.getCode()
                    );
                    
                    Boolean liked = null;
                    if (request.getUserId() != null) {
                        liked = likeApplicationService.checkLikeStatus(
                                request.getUserId(), 
                                request.getTargetId(), 
                                likeType.getCode()
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
    }
    
    @Operation(summary = "获取用户点赞统计")
    @GetMapping("/user/statistics")
    public ResponseEntity<LikeStatisticsService.UserLikeStatistics> getUserStatistics(
            @RequestParam Long userId) {
        LikeStatisticsService.UserLikeStatistics statistics = 
                likeApplicationService.getUserLikeStatistics(userId);
        return ResponseEntity.<LikeStatisticsService.UserLikeStatistics>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(statistics)
                .build();
    }
}