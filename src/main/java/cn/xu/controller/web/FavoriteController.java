package cn.xu.controller.web;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.favorite.FavoriteRequest;
import cn.xu.model.vo.favorite.FavoriteCountVO;
import cn.xu.model.dto.favorite.GetMyFavoritesRequest;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.User;
import cn.xu.model.enums.favorite.TargetType;
import cn.xu.model.vo.post.PostItemVO;
import cn.xu.model.vo.post.PostListVO;
import cn.xu.service.favorite.FavoriteService;
import cn.xu.service.post.PostQueryService;
import cn.xu.service.user.UserService;
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
 * <p>提供帖子收藏、取消收藏、收藏夹管理等功能接口</p>
 * <p>支持多收藏夹分类管理，默认收藏夹无需创建</p>
 
 */
@Slf4j
@Tag(name = "收藏接口", description = "收藏相关API")
@RestController
@RequestMapping("/api/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;
    private final PostQueryService postQueryService;
    private final UserService userService;

    /**
     * 添加收藏
     * 
     * <p>将指定目标添加到默认收藏夹
     * <p>需要登录后才能访问
     * 
     * @param request 收藏请求，包含目标ID和类型(post/essay)
     * @return 操作结果
     * @throws BusinessException 当重复收藏或目标不存在时抛出
     */
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

    /**
     * 取消收藏
     * 
     * <p>取消对指定目标的收藏
     * <p>需要登录后才能访问
     * 
     * @param request 取消收藏请求，包含目标ID和类型
     * @return 操作结果
     * @throws BusinessException 当未收藏或目标不存在时抛出
     */
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

    /**
     * 检查收藏状态
     * 
     * <p>检查当前登录用户是否已收藏指定目标
     * <p>需要登录后才能访问
     * 
     * @param targetId 目标ID
     * @param targetType 目标类型：post(帖子)、essay(随笔)
     * @return true-已收藏，false-未收藏
     */
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

    /**
     * 批量检查收藏状态
     * 
     * <p>批量检查多个目标的收藏状态，适用于列表页面展示
     * <p>需要登录后才能访问
     * 
     * @param requests 收藏请求列表，每个请求包含目标ID和类型
     * @return 每个目标的收藏状态
     */
    @Operation(summary = "批量检查收藏状态")
    @PostMapping("/check/batch")
    public ResponseEntity<List<FavoriteCountVO>> batchCheckStatus(@RequestBody List<FavoriteRequest> requests) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            List<FavoriteCountVO> responses = requests.stream()
                    .map(request -> {
                        TargetType targetTypeEnum = TargetType.fromCode(request.getTargetType());
                        boolean favorited = favoriteService.isFavorited(userId, request.getTargetId(), targetTypeEnum.getApiCode());
                        return FavoriteCountVO.builder()
                                .targetId(request.getTargetId())
                                .targetType(targetTypeEnum.getApiCode())
                                .isFavorited(favorited)
                                .build();
                    })
                    .collect(Collectors.toList());
            return ResponseEntity.<List<FavoriteCountVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(responses)
                    .build();
        } catch (Exception e) {
            log.error("批量检查收藏状态异常", e);
            return ResponseEntity.<List<FavoriteCountVO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("查询失败")
                    .build();
        }
    }


    /**
     * 获取我的收藏列表
     * 
     * <p>分页获取当前登录用户的收藏内容，支持按收藏夹筛选
     * <p>需要登录后才能访问
     * 
     * @param request 查询请求，包含页码、每页数量、类型、收藏夹ID(可选)
     * @return 分页的收藏帖子列表
     */
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
            List<Long> postIds = favoriteService.getFavoritedTargetIdsWithPage(userId, apiTargetType, offset, pageSize);
            int total = favoriteService.countFavoritedItems(userId, apiTargetType);

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