package cn.xu.api.controller.web.like;

import cn.xu.api.controller.web.like.dto.LikeCountResponse;
import cn.xu.api.controller.web.like.dto.LikeRequest;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "点赞接口")
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "点赞")
    @PostMapping("/like")
    public ResponseEntity<Void> like(@RequestBody LikeRequest request) {
        try {
            likeService.like(request.getUserId(), request.getTargetId(), 
                    LikeType.valueOf(request.getType().toUpperCase()));
            return ResponseEntity.<Void>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("点赞成功")
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.<Void>builder()
                    .code(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("非法的点赞类型")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.<Void>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("点赞失败：" + e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/unlike")
    public ResponseEntity<Void> unlike(@RequestBody LikeRequest request) {
        try {
            likeService.unlike(request.getUserId(), request.getTargetId(), 
                    LikeType.valueOf(request.getType().toUpperCase()));
            return ResponseEntity.<Void>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("取消点赞成功")
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.<Void>builder()
                    .code(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("非法的点赞类型")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.<Void>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("取消点赞失败：" + e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "获取点赞数")
    @GetMapping("/count")
    public ResponseEntity<LikeCountResponse> getLikeCount(@RequestParam String type,
                                                        @RequestParam Long targetId) {
        try {
            LikeType likeType = LikeType.valueOf(type.toUpperCase());
            Long count = likeService.getLikeCount(targetId, likeType);
            LikeCountResponse response = new LikeCountResponse(count, type, targetId);
            
            return ResponseEntity.<LikeCountResponse>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.<LikeCountResponse>builder()
                    .code(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("非法的点赞类型")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.<LikeCountResponse>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("获取点赞数失败：" + e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "检查是否已点赞")
    @PostMapping("/check")
    public ResponseEntity<Boolean> checkLike(@RequestBody LikeRequest likeRequest) {
        Long userId = likeRequest.getUserId();
        Long targetId = likeRequest.getTargetId();
        String type = likeRequest.getType();
        try {
            Boolean liked = likeService.isLiked(userId, targetId, 
                    LikeType.valueOf(type.toUpperCase()));
            
            return ResponseEntity.<Boolean>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info(Constants.ResponseCode.SUCCESS.getInfo())
                    .data(liked)
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.<Boolean>builder()
                    .code(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("非法的点赞类型")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.<Boolean>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("检查点赞状态失败：" + e.getMessage())
                    .build();
        }
    }
} 