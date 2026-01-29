package cn.xu.controller.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.favorite.CreateFolderRequest;
import cn.xu.model.dto.favorite.GetMyFavoritesRequest;
import cn.xu.model.dto.favorite.UpdateFolderRequest;
import cn.xu.model.entity.FavoriteFolder;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.User;
import cn.xu.model.enums.favorite.TargetType;
import cn.xu.model.vo.favorite.FavoriteFolderVO;
import cn.xu.model.vo.post.PostItemVO;
import cn.xu.model.vo.post.PostListVO;
import cn.xu.service.favorite.FavoriteService;
import cn.xu.service.favorite.FavoriteFolderService;
import cn.xu.service.post.PostQueryService;
import cn.xu.service.user.UserService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;
import cn.xu.service.user.UserService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 收藏夹控制器
 */
@Slf4j
@Tag(name = "收藏夹接口", description = "收藏夹管理API")
@RestController
@RequestMapping("/api/favorite-folders")
@RequiredArgsConstructor
public class FavoriteFolderController {

    private final FavoriteFolderService favoriteFolderService;
    private final FavoriteService favoriteService;
    private final PostQueryService postQueryService;
    private final UserService userService;

    @Operation(summary = "创建收藏夹")
    @PostMapping
    @ApiOperationLog(description = "创建收藏夹")
    public ResponseEntity<FavoriteFolderVO> createFolder(@Valid @RequestBody CreateFolderRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            FavoriteFolder folder = favoriteFolderService.createFolder(
                    userId, request.getName(), request.getDescription(), 
                    Boolean.TRUE.equals(request.getIsPublic()));
            return ResponseEntity.<FavoriteFolderVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(FavoriteFolderVO.fromEntity(folder))
                    .info("创建成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<FavoriteFolderVO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "获取我的收藏夹列表")
    @GetMapping("/my")
    public ResponseEntity<List<FavoriteFolderVO>> getMyFolders() {
        Long userId = StpUtil.getLoginIdAsLong();
        // 确保有默认收藏夹
        favoriteFolderService.getOrCreateDefaultFolder(userId);
        List<FavoriteFolder> folders = favoriteFolderService.getUserFolders(userId);
        List<FavoriteFolderVO> voList = folders.stream()
                .map(FavoriteFolderVO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.<List<FavoriteFolderVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(voList)
                .build();
    }

    @Operation(summary = "获取用户公开的收藏夹")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<FavoriteFolderVO>> getUserPublicFolders(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        List<FavoriteFolder> folders = favoriteFolderService.getPublicFolders(userId);
        List<FavoriteFolderVO> voList = folders.stream()
                .map(FavoriteFolderVO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.<List<FavoriteFolderVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(voList)
                .build();
    }

    @Operation(summary = "获取收藏夹中的收藏内容")
    @PostMapping("/{folderId}/favorites")
    public ResponseEntity<PageResponse<List<PostListVO>>> getFolderFavorites(
            @Parameter(description = "收藏夹ID") @PathVariable Long folderId,
            @RequestBody GetMyFavoritesRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        
        // 检查收藏夹权限
        if (!favoriteFolderService.canAccessFolder(userId, folderId)) {
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("无权访问此收藏夹")
                    .build();
        }
        
        try {
            int pageNo = request.getSafePageNo();
            int pageSize = request.getSafePageSize();
            String apiTargetType = TargetType.fromCode(request.getType()).getApiCode();

            // 按收藏夹查询
            List<Long> postIds = favoriteService.getFavoritedTargetIdsByFolderWithPage(
                    userId, apiTargetType, folderId, pageNo, pageSize);
            int total = favoriteService.countFavoritedItemsByFolder(userId, apiTargetType, folderId);

            if (postIds == null || postIds.isEmpty()) {
                return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .data(PageResponse.ofList(pageNo, pageSize, (long) total, Collections.emptyList()))
                        .build();
            }

            List<PostListVO> responses = buildPostListVOs(postIds);
            
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(PageResponse.ofList(pageNo, pageSize, (long) total, responses))
                    .build();
        } catch (Exception e) {
            log.error("获取收藏夹内容失败", e);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取收藏夹内容失败: " + e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "更新收藏夹")
    @PutMapping("/{folderId}")
    @ApiOperationLog(description = "更新收藏夹")
    public ResponseEntity<Void> updateFolder(
            @Parameter(description = "收藏夹ID") @PathVariable Long folderId,
            @Valid @RequestBody UpdateFolderRequest request) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            favoriteFolderService.updateFolder(userId, folderId, 
                    request.getName(), request.getDescription(), request.getIsPublic());
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("更新成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "删除收藏夹")
    @DeleteMapping("/{folderId}")
    @ApiOperationLog(description = "删除收藏夹")
    public ResponseEntity<Void> deleteFolder(
            @Parameter(description = "收藏夹ID") @PathVariable Long folderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            favoriteFolderService.deleteFolder(userId, folderId);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("删除成功")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }

    @Operation(summary = "获取收藏夹详情")
    @GetMapping("/{folderId}")
    public ResponseEntity<FavoriteFolderVO> getFolderDetail(
            @Parameter(description = "收藏夹ID") @PathVariable Long folderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        FavoriteFolder folder = favoriteFolderService.getFolderById(folderId);
        if (folder == null) {
            return ResponseEntity.<FavoriteFolderVO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("收藏夹不存在")
                    .build();
        }
        // 检查访问权限
        if (!folder.getUserId().equals(userId) && !folder.isPublicFolder()) {
            return ResponseEntity.<FavoriteFolderVO>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("无权访问此收藏夹")
                    .build();
        }
        return ResponseEntity.<FavoriteFolderVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(FavoriteFolderVO.fromEntity(folder))
                .build();
    }
    
    @Operation(summary = "迁移收藏夹内容")
    @PostMapping("/{sourceFolderId}/move-to/{targetFolderId}")
    @ApiOperationLog(description = "迁移收藏夹内容")
    public ResponseEntity<Integer> moveFolderContents(
            @Parameter(description = "源收藏夹ID") @PathVariable Long sourceFolderId,
            @Parameter(description = "目标收藏夹ID") @PathVariable Long targetFolderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            int movedCount = favoriteFolderService.moveFolderContents(userId, sourceFolderId, targetFolderId);
            return ResponseEntity.<Integer>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(movedCount)
                    .info("已迁移 " + movedCount + " 个内容")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Integer>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }
    
    @Operation(summary = "合并收藏夹")
    @PostMapping("/{sourceFolderId}/merge-to/{targetFolderId}")
    @ApiOperationLog(description = "合并收藏夹")
    public ResponseEntity<Integer> mergeFolders(
            @Parameter(description = "源收藏夹ID") @PathVariable Long sourceFolderId,
            @Parameter(description = "目标收藏夹ID") @PathVariable Long targetFolderId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            int movedCount = favoriteFolderService.mergeFolders(userId, sourceFolderId, targetFolderId);
            return ResponseEntity.<Integer>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(movedCount)
                    .info("已合并 " + movedCount + " 个内容")
                    .build();
        } catch (BusinessException e) {
            return ResponseEntity.<Integer>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        }
    }
    
    // ==================== 私有辅助方法 ====================
    
    private List<PostListVO> buildPostListVOs(List<Long> postIds) {
        List<Post> posts = postQueryService.getByIds(postIds);
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
            return userService.batchGetUserInfo(new ArrayList<>(userIds));
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
