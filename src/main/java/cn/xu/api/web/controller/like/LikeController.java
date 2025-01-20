package cn.xu.api.web.controller.like;

import cn.xu.api.web.controller.like.dto.LikeCountResponse;
import cn.xu.api.web.controller.like.request.LikeRequest;
import cn.xu.api.web.model.dto.common.ResponseEntity;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.like.command.LikeCommand;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.LikeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Slf4j
@Tag(name = "点赞接口")
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @Operation(summary = "点赞")
    @PostMapping("/like")
    public ResponseEntity<Void> like(@Valid @RequestBody LikeRequest request) {
        try {
            LikeCommand command = LikeCommand.builder()
                    .userId(request.getUserId())
                    .targetId(request.getTargetId())
                    .type(LikeType.valueOf(request.getType().toUpperCase()))
                    .build();

            likeService.like(command);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("点赞成功")
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("非法的点赞类型")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("点赞失败：" + e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/unlike")
    public ResponseEntity<Void> unlike(@Valid @RequestBody LikeRequest request) {
        try {
            LikeCommand command = LikeCommand.builder()
                    .userId(request.getUserId())
                    .targetId(request.getTargetId())
                    .type(LikeType.valueOf(request.getType().toUpperCase()))
                    .build();

            likeService.unlike(command);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("取消点赞成功")
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("非法的点赞类型")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("取消点赞失败：" + e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "获取点赞数")
    @GetMapping("/count")
    public ResponseEntity<LikeCountResponse> getLikeCount(@RequestParam String type,
                                                          @RequestParam Long targetId) {
        try {
            Long count = likeService.getLikeCount(targetId, type);
            LikeCountResponse response = new LikeCountResponse(count, type, targetId);
            log.info("获取点赞数成功：" + response);
            return ResponseEntity.<LikeCountResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(response)
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.<LikeCountResponse>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("非法的点赞类型")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.<LikeCountResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取点赞数失败：" + e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "检查是否已点赞")
    @PostMapping("/check")
    public ResponseEntity<Boolean> checkLike(@Valid @RequestBody LikeRequest request) {
        try {
            boolean liked = likeService.isLiked(
                    request.getUserId(),
                    request.getTargetId(),
                    request.getType()
            );

            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(liked)
                    .build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("非法的点赞类型")
                    .build();
        } catch (Exception e) {
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("检查点赞状态失败：" + e.getMessage())
                    .build();
        }
    }
} 