package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.dto.favorite.FavoriteCountResponse;
import cn.xu.api.web.dto.favorite.FavoriteRequest;
import cn.xu.application.service.FavoriteApplicationService;
import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.favorite.model.entity.FavoriteFolderEntity;
import cn.xu.domain.favorite.service.IFavoriteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏接口
 * 提供通用的收藏相关API接口
 */
@Slf4j
@Tag(name = "收藏接口")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteApplicationService favoriteApplicationService;
    private final IFavoriteService favoriteService;

    /**
     * 收藏内容
     */
    @Operation(summary = "添加收藏")
    @PostMapping
    public ResponseEntity<Void> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            // 根据目标类型执行不同的收藏逻辑
            if ("post".equals(request.getTargetType())) {
                favoriteApplicationService.favoritePost(userId, request.getTargetId());
            } else {
                // 其他类型的收藏直接调用服务层
                favoriteService.favorite(userId, request.getTargetId(), request.getTargetType());
            }
            
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("收藏成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("收藏失败: {}", e.getMessage());
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("收藏异常", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("收藏失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 取消收藏
     */
    @Operation(summary = "取消收藏")
    @DeleteMapping
    public ResponseEntity<Void> removeFavorite(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型") @RequestParam String targetType) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            // 根据目标类型执行不同的取消收藏逻辑
            if ("post".equals(targetType)) {
                boolean result = favoriteApplicationService.unfavoritePost(userId, targetId);
                if (!result) {
                    return ResponseEntity.<Void>builder()
                            .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                            .info("尚未收藏")
                            .build();
                }
            } else {
                // 其他类型的取消收藏直接调用服务层
                favoriteService.unfavorite(userId, targetId, targetType);
            }
            
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("取消收藏成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("取消收藏失败: {}", e.getMessage());
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("取消收藏异常", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("取消收藏失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 检查是否已收藏
     */
    @Operation(summary = "检查收藏状态")
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkStatus(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型") @RequestParam String targetType) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            boolean status = favoriteService.isFavorited(userId, targetId, targetType);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(status)
                    .build();
        } catch (Exception e) {
            log.error("检查收藏状态异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败，请稍后重试")
                    .data(false)
                    .build();
        }
    }

    /**
     * 批量获取收藏状态
     */
    @Operation(summary = "批量检查收藏状态")
    @PostMapping("/check/batch")
    public ResponseEntity<List<FavoriteCountResponse>> batchCheckStatus(
            @RequestBody List<FavoriteRequest> requests) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            List<FavoriteCountResponse> responses = requests.stream()
                    .map(request -> {
                        boolean favorited = favoriteService.isFavorited(
                                userId, 
                                request.getTargetId(), 
                                request.getTargetType()
                        );
                        return new FavoriteCountResponse(
                                request.getTargetId(), 
                                request.getTargetType(), 
                                null, // 不返回收藏数，只返回状态
                                favorited
                        );
                    })
                    .collect(Collectors.toList());
                    
            return ResponseEntity.<List<FavoriteCountResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(responses)
                    .build();
        } catch (Exception e) {
            log.error("批量检查收藏状态异常", e);
            return ResponseEntity.<List<FavoriteCountResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 获取收藏夹列表
     */
    @Operation(summary = "获取收藏夹列表")
    @GetMapping("/folders")
    public ResponseEntity<List<FavoriteFolderEntity>> getFolders() {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            List<FavoriteFolderEntity> folders = favoriteService.getFoldersByUserId(userId);
            return ResponseEntity.<List<FavoriteFolderEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info(ResponseCode.SUCCESS.getMessage())
                    .data(folders)
                    .build();
        } catch (Exception e) {
            log.error("获取收藏夹列表异常", e);
            return ResponseEntity.<List<FavoriteFolderEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 将内容添加到收藏夹
     */
    @Operation(summary = "将内容添加到收藏夹")
    @PostMapping("/to-folder")
    public ResponseEntity<Void> addToFolder(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型") @RequestParam String targetType,
            @Parameter(description = "收藏夹ID") @RequestParam Long folderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            // 先确保内容已被收藏
            if (!favoriteService.isFavorited(userId, targetId, targetType)) {
                favoriteService.favorite(userId, targetId, targetType);
            }
            
            // 添加到收藏夹
            favoriteService.addTargetToFolder(userId, targetId, targetType, folderId);
            
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("添加到收藏夹成功")
                    .build();
        } catch (Exception e) {
            log.error("添加到收藏夹异常", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("操作失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 从收藏夹移除内容
     */
    @Operation(summary = "从收藏夹移除内容")
    @DeleteMapping("/from-folder")
    public ResponseEntity<Void> removeFromFolder(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型") @RequestParam String targetType,
            @Parameter(description = "收藏夹ID") @RequestParam Long folderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            favoriteService.removeTargetFromFolder(userId, targetId, targetType, folderId);
            
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("从收藏夹移除成功")
                    .build();
        } catch (Exception e) {
            log.error("从收藏夹移除异常", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("操作失败，请稍后重试")
                    .build();
        }
    }
}