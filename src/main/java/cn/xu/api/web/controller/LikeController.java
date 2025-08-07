package cn.xu.api.web.controller;

import cn.xu.api.web.model.dto.like.LikeRequest;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.infrastructure.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@Slf4j
@Tag(name = "点赞接口")
@RestController
@RequestMapping("/api/likes")
@RequiredArgsConstructor
public class LikeController {

    private final ILikeService likeService;

    @Operation(summary = "点赞")
    @PostMapping("/like")
    public ResponseEntity<Void> like(@Valid @RequestBody LikeRequest request) {
        likeService.like(
                request.getUserId(),
                LikeType.valueOf(request.getType()).getCode(),
                request.getTargetId());
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("点赞成功")
                .build();
    }

    @Operation(summary = "取消点赞")
    @PostMapping("/unlike")
    public ResponseEntity<Void> unlike(@Valid @RequestBody LikeRequest request) {
        likeService.like(
                request.getUserId(),
                LikeType.valueOf(request.getType()).getCode(),
                request.getTargetId());
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("点赞成功")
                .build();
    }
} 