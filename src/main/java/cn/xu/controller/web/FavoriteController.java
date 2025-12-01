package cn.xu.controller.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.favorite.FavoriteCountResponse;
import cn.xu.model.dto.favorite.FavoriteRequest;
import cn.xu.model.dto.favorite.GetMyFavoritesRequest;
import cn.xu.model.entity.Favorite;
import cn.xu.model.entity.FavoriteFolder;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.User;
import cn.xu.model.enums.favorite.TargetType;
import cn.xu.model.vo.post.PostItemVO;
import cn.xu.model.vo.post.PostListVO;
import cn.xu.service.favorite.FavoriteService;
import cn.xu.service.post.PostService;
import cn.xu.service.user.IUserService;
import cn.xu.support.exception.BusinessException;
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
 * 收藏控制器
 * 
 * <p>提供帖子收藏、取消收藏、收藏夹管理等功能接口
 * 
 * @author xu
 * @since 2025-11-25
 */
@Slf4j
@Tag(name = "收藏接口", description = "收藏相关API")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final PostService postService;
    private final IUserService userService;

    @Operation(summary = "添加收藏")
    @PostMapping("/favorite")
    @ApiOperationLog(description = "添加收藏")
    public ResponseEntity<Void> addFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            TargetType targetType = TargetType.fromCode(request.getTargetType());
            favoriteService.favorite(userId, request.getTargetId(), targetType.getApiCode());
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

    @Operation(summary = "取消收藏")
    @PostMapping("/unfavorite")
    @ApiOperationLog(description = "取消收藏")
    public ResponseEntity<Void> removeFavorite(@Valid @RequestBody FavoriteRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            TargetType targetType = TargetType.fromCode(request.getTargetType());
            favoriteService.unfavorite(userId, request.getTargetId(), targetType.getApiCode());
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

    @Operation(summary = "检查收藏状态")
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkStatus(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型") @RequestParam String targetType) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            TargetType targetTypeEnum = TargetType.fromCode(targetType);
            boolean status = favoriteService.isFavorited(userId, targetId, targetTypeEnum.getApiCode());
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(status)
                    .build();
        } catch (Exception e) {
            log.error("检查收藏状态异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .data(false)
                    .build();
        }
    }

    @Operation(summary = "批量检查收藏状态")
    @PostMapping("/check/batch")
    public ResponseEntity<List<FavoriteCountResponse>> batchCheckStatus(@RequestBody List<FavoriteRequest> requests) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            List<FavoriteCountResponse> responses = requests.stream()
                    .map(request -> {
                        TargetType targetTypeEnum = TargetType.fromCode(request.getTargetType());
                        boolean favorited = favoriteService.isFavorited(userId, request.getTargetId(), targetTypeEnum.getApiCode());
                        return FavoriteCountResponse.builder()
                                .targetId(request.getTargetId())
                                .targetType(targetTypeEnum.getApiCode())
                                .isFavorited(favorited)
                                .build();
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.<List<FavoriteCountResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(responses)
                    .build();
        } catch (Exception e) {
            log.error("批量检查收藏状态异常", e);
            return ResponseEntity.<List<FavoriteCountResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }

    @Operation(summary = "创建收藏夹")
    @PostMapping("/folders")
    @ApiOperationLog(description = "创建收藏夹")
    public ResponseEntity<Long> createFolder(@RequestBody Map<String, String> request) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            String name = request.get("name");
            String description = request.get("description");
            
            if (name == null || name.trim().isEmpty()) {
                return ResponseEntity.<Long>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("收藏夹名称不能为空")
                        .build();
            }
            
            Long folderId = favoriteService.createFolder(userId, name.trim(), description);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(folderId)
                    .info("创建成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("创建收藏夹失败: {}", e.getMessage());
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("创建收藏夹异常", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("创建失败")
                    .build();
        }
    }

    @Operation(summary = "删除收藏夹")
    @DeleteMapping("/folders/{folderId}")
    @ApiOperationLog(description = "删除收藏夹")
    public ResponseEntity<Void> deleteFolder(@PathVariable Long folderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            favoriteService.deleteFolder(userId, folderId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("删除成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("删除收藏夹失败: {}", e.getMessage());
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("删除收藏夹异常", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("删除失败")
                    .build();
        }
    }

    @Operation(summary = "获取收藏夹列表")
    @GetMapping("/folders")
    public ResponseEntity<List<FavoriteFolder>> getFolders() {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            List<FavoriteFolder> folders = favoriteService.getFoldersByUserId(userId);
            return ResponseEntity.<List<FavoriteFolder>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(folders)
                    .build();
        } catch (Exception e) {
            log.error("获取收藏夹列表异常", e);
            return ResponseEntity.<List<FavoriteFolder>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }

    @Operation(summary = "将内容添加到收藏夹")
    @PostMapping("/to-folder")
    public ResponseEntity<Void> addToFolder(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型") @RequestParam String targetType,
            @Parameter(description = "收藏夹ID") @RequestParam Long folderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            TargetType targetTypeEnum = TargetType.fromCode(targetType);
            String apiTargetType = targetTypeEnum.getApiCode();
            if (!favoriteService.isFavorited(userId, targetId, apiTargetType)) {
                favoriteService.favorite(userId, targetId, apiTargetType);
            }
            favoriteService.moveToFolder(userId, targetId, apiTargetType, folderId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("添加到收藏夹成功")
                    .build();
        } catch (Exception e) {
            log.error("添加到收藏夹异常", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("操作失败")
                    .build();
        }
    }

    @Operation(summary = "从收藏夹移除内容")
    @DeleteMapping("/from-folder")
    public ResponseEntity<Void> removeFromFolder(
            @Parameter(description = "目标ID") @RequestParam Long targetId,
            @Parameter(description = "目标类型") @RequestParam String targetType,
            @Parameter(description = "收藏夹ID") @RequestParam Long folderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
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
                    .info("操作失败")
                    .build();
        }
    }

    @Operation(summary = "获取我的收藏列表")
    @PostMapping("/my")
    @ApiOperationLog(description = "获取我的收藏列表")
    public ResponseEntity<PageResponse<List<PostListVO>>> getMyFavorites(@RequestBody GetMyFavoritesRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            int pageNo = request.getSafePageNo();
            int pageSize = request.getSafePageSize();
            int offset = request.getOffset();
            String apiTargetType = TargetType.fromCode(request.getType()).getApiCode();

            // 1. 获取收藏的帖子ID列表和总数
            List<Long> postIds;
            int total;
            
            if (request.getFolderId() != null) {
                List<Favorite> favorites = favoriteService.findTargetsInFolder(userId, request.getFolderId());
                List<Long> allPostIds = favorites.stream()
                        .filter(f -> "post".equalsIgnoreCase(f.getTargetType()))
                        .map(Favorite::getTargetId)
                        .collect(Collectors.toList());
                total = allPostIds.size();
                int endIndex = Math.min(offset + pageSize, allPostIds.size());
                postIds = allPostIds.subList(Math.min(offset, allPostIds.size()), endIndex);
            } else {
                postIds = favoriteService.getFavoritedTargetIdsWithPage(userId, apiTargetType, null, offset, pageSize);
                total = favoriteService.countFavoritedItems(userId, apiTargetType);
            }

            // 2. 空列表快速返回
            if (postIds == null || postIds.isEmpty()) {
                return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .data(PageResponse.ofList(pageNo, pageSize, (long) total, Collections.emptyList()))
                        .build();
            }

            // 3. 构建响应
            List<PostListVO> responses = buildPostListVOs(postIds);
            
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(PageResponse.ofList(pageNo, pageSize, (long) total, responses))
                    .build();
        } catch (Exception e) {
            log.error("获取我的收藏列表失败", e);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取收藏列表失败: " + e.getMessage())
                    .build();
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    private List<PostListVO> buildPostListVOs(List<Long> postIds) {
        List<Post> posts = postService.getPostsByIds(postIds);
        Map<Long, Post> postMap = posts.stream()
                .collect(Collectors.toMap(Post::getId, p -> p, (a, b) -> a));
        Map<Long, User> userMap = getUserMap(posts);
        
        return postIds.stream()
                .map(postId -> {
                    Post post = postMap.get(postId);
                    if (post == null) return null;
                    User user = post.getUserId() != null ? userMap.get(post.getUserId()) : null;
                    return PostListVO.builder()
                            .postItem(buildPostItemVO(post, user))
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    private Map<Long, User> getUserMap(List<Post> posts) {
        Set<Long> userIds = posts.stream()
                .map(Post::getUserId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        
        if (userIds.isEmpty()) {
            return Collections.emptyMap();
        }
        
        try {
            List<User> users = userService.batchGetUserInfo(new ArrayList<>(userIds));
            return users.stream()
                    .collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
            return Collections.emptyMap();
        }
    }
    
    private PostItemVO buildPostItemVO(Post post, User user) {
        return PostItemVO.builder()
                .id(post.getId())
                .title(post.getTitle())
                .description(post.getDescription())
                .content(post.getContent())
                .coverUrl(post.getCoverUrl())
                .status(post.getStatus())
                .userId(post.getUserId())
                .nickname(user != null ? user.getNickname() : null)
                .avatar(user != null ? user.getAvatar() : null)
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .favoriteCount(post.getFavoriteCount())
                .createTime(post.getCreateTime())
                .updateTime(post.getUpdateTime())
                .build();
    }
}

