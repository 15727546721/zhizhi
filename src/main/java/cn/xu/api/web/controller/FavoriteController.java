package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.dto.favorite.FavoriteCountResponse;
import cn.xu.api.web.dto.favorite.FavoriteRequest;
import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.application.service.FavoriteApplicationService;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.favorite.model.entity.FavoriteEntity;
import cn.xu.domain.favorite.model.entity.FavoriteFolderEntity;
import cn.xu.domain.favorite.model.valobj.TargetType;
import cn.xu.domain.favorite.service.IFavoriteService;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.service.IPostService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.*;
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
    private final IPostService postService;
    private final IUserService userService;

    /**
     * 收藏内容
     */
    @Operation(summary = "添加收藏")
    @PostMapping("/favorite")
    @ApiOperationLog(description = "添加收藏")
    public ResponseEntity<Void> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            // 将字符串转换为枚举
            TargetType targetType = TargetType.fromCode(request.getTargetType());
            
            // 根据目标类型执行不同的收藏逻辑
            if (targetType.isPost()) {
                favoriteApplicationService.favoritePost(userId, request.getTargetId());
            } else {
                // 其他类型的收藏直接调用服务层（使用枚举的API代码）
                favoriteService.favorite(userId, request.getTargetId(), targetType.getApiCode());
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
    @PostMapping("/unfavorite")
    @ApiOperationLog(description = "取消收藏")
    public ResponseEntity<Void> removeFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        try {
            // 将字符串转换为枚举
            TargetType targetType = TargetType.fromCode(request.getTargetType());
            
            // 根据目标类型执行不同的取消收藏逻辑
            if (targetType.isPost()) {
                boolean result = favoriteApplicationService.unfavoritePost(userId, request.getTargetId());
                if (!result) {
                    return ResponseEntity.<Void>builder()
                            .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                            .info("尚未收藏")
                            .build();
                }
            } else {
                // 其他类型的取消收藏直接调用服务层（使用枚举的API代码）
                favoriteService.unfavorite(userId, request.getTargetId(), targetType.getApiCode());
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
            // 将字符串转换为枚举
            TargetType targetTypeEnum = TargetType.fromCode(targetType);
            boolean status = favoriteService.isFavorited(userId, targetId, targetTypeEnum.getApiCode());
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
                        // 将字符串转换为枚举
                        TargetType targetTypeEnum = TargetType.fromCode(request.getTargetType());
                        boolean favorited = favoriteService.isFavorited(
                                userId, 
                                request.getTargetId(), 
                                targetTypeEnum.getApiCode()
                        );
                        return new FavoriteCountResponse(
                                request.getTargetId(), 
                                targetTypeEnum.getApiCode(), 
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
            // 将字符串转换为枚举
            TargetType targetTypeEnum = TargetType.fromCode(targetType);
            String apiTargetType = targetTypeEnum.getApiCode();
            
            // 先确保内容已被收藏
            if (!favoriteService.isFavorited(userId, targetId, apiTargetType)) {
                favoriteService.favorite(userId, targetId, apiTargetType);
            }
            
            // 添加到收藏夹
            favoriteService.addTargetToFolder(userId, targetId, apiTargetType, folderId);
            
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
            // 将字符串转换为枚举
            TargetType targetTypeEnum = TargetType.fromCode(targetType);
            favoriteService.removeTargetFromFolder(userId, targetId, targetTypeEnum.getApiCode(), folderId);
            
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

    /**
     * 获取我的收藏列表
     */
    @Operation(summary = "获取我的收藏列表")
    @PostMapping("/my")
    @ApiOperationLog(description = "获取我的收藏列表")
    public ResponseEntity<PageResponse<List<PostListResponse>>> getMyFavorites(@RequestBody Map<String, Object> request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Integer pageNo = request.get("pageNo") != null ? Integer.valueOf(request.get("pageNo").toString()) : 1;
            Integer pageSize = request.get("pageSize") != null ? Integer.valueOf(request.get("pageSize").toString()) : 10;
            String targetType = request.get("type") != null ? request.get("type").toString() : "POST";
            Long folderId = request.get("folderId") != null ? Long.valueOf(request.get("folderId").toString()) : null;

            // 参数校验
            if (pageNo < 1) {
                pageNo = 1;
            }
            if (pageSize < 1 || pageSize > 100) {
                pageSize = 10;
            }

            // 计算分页参数
            int offset = (pageNo - 1) * pageSize;
            int safeOffset = Math.max(0, offset);
            int safeLimit = Math.max(1, Math.min(pageSize, 100));

            // 获取收藏的帖子ID列表（数据库层面分页）
            List<Long> favoritedPostIds;
            String apiTargetType = TargetType.fromCode(targetType).getApiCode();
            
            if (folderId != null) {
                // 获取收藏夹中的收藏记录（这里需要先获取所有，然后过滤，因为收藏夹查询不支持分页）
                List<FavoriteEntity> favorites = favoriteService.getTargetsInFolder(userId, folderId);
                List<Long> allPostIds = favorites.stream()
                        .filter(f -> "post".equalsIgnoreCase(f.getTargetType()) || "POST".equalsIgnoreCase(f.getTargetType()))
                        .map(FavoriteEntity::getTargetId)
                        .collect(Collectors.toList());
                
                // 内存分页
                int endIndex = Math.min(safeOffset + safeLimit, allPostIds.size());
                favoritedPostIds = allPostIds.subList(Math.min(safeOffset, allPostIds.size()), endIndex);
            } else {
                // 使用数据库分页查询
                favoritedPostIds = favoriteService.getFavoritedTargetIdsWithPage(userId, apiTargetType, null, safeOffset, safeLimit);
            }

            // 计算总数
            int total;
            if (folderId != null) {
                // 收藏夹中的总数
                List<FavoriteEntity> allFavorites = favoriteService.getTargetsInFolder(userId, folderId);
                total = (int) allFavorites.stream()
                        .filter(f -> "post".equalsIgnoreCase(f.getTargetType()) || "POST".equalsIgnoreCase(f.getTargetType()))
                        .count();
            } else {
                // 所有收藏的总数
                total = favoriteService.countFavoritedItems(userId, apiTargetType);
            }

            // 如果没有收藏，直接返回空列表
            if (favoritedPostIds == null || favoritedPostIds.isEmpty()) {
                PageResponse<List<PostListResponse>> pageResponse = PageResponse.ofList(
                    pageNo, 
                    pageSize, 
                    (long) total, 
                    Collections.emptyList()
                );
                return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .data(pageResponse)
                        .build();
            }

            List<Long> pagePostIds = favoritedPostIds;

            // 批量获取帖子详情
            List<PostEntity> posts = postService.findPostsByIds(pagePostIds);
            
            // 获取用户信息
            Set<Long> userIds = posts.stream()
                    .map(PostEntity::getUserId)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toSet());
            
            List<UserEntity> users = Collections.emptyList();
            if (!userIds.isEmpty()) {
                try {
                    users = userService.batchGetUserInfo(new ArrayList<>(userIds));
                } catch (Exception e) {
                    log.warn("批量获取用户信息失败", e);
                }
            }
            
            Map<Long, UserEntity> userMap = users.stream()
                    .collect(Collectors.toMap(UserEntity::getId, user -> user, (existing, replacement) -> existing));

            // 转换为PostListResponse，保持收藏顺序
            List<PostListResponse> postListResponses = pagePostIds.stream()
                    .map(postId -> {
                        PostEntity post = posts.stream()
                                .filter(p -> p.getId().equals(postId))
                                .findFirst()
                                .orElse(null);
                        if (post == null) {
                            return null;
                        }
                        PostListResponse response = new PostListResponse();
                        response.setPost(post);
                        if (post.getUserId() != null) {
                            response.setUser(userMap.get(post.getUserId()));
                        }
                        return response;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            // 创建PageResponse对象
            PageResponse<List<PostListResponse>> pageResponse = PageResponse.ofList(
                pageNo, 
                pageSize, 
                (long) total, 
                postListResponses
            );

            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取我的收藏列表失败", e);
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取收藏列表失败: " + e.getMessage())
                    .build();
        }
    }
}