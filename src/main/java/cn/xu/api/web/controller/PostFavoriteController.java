package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.dto.favorite.FavoriteFolderResponse;
import cn.xu.api.web.dto.favorite.FavoritePostToFolderRequest;
import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.application.service.FavoriteApplicationService;
import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.favorite.model.entity.FavoriteFolderEntity;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.service.IPostService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 帖子收藏接口
 * 提供帖子收藏相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/post/favorite")
public class PostFavoriteController {

    @Autowired
    private FavoriteApplicationService favoriteApplicationService;

    @Autowired
    private IPostService postService;

    /**
     * 收藏帖子
     *
     * @param postId 帖子ID
     * @return 响应结果
     */
    @PostMapping("/post/{postId}")
    public ResponseEntity<Boolean> favoritePost(@PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = favoriteApplicationService.favoritePost(userId, postId);
            
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .info(result ? "收藏成功" : "收藏失败")
                    .build();
        } catch (BusinessException e) {
            log.warn("收藏帖子失败: {}", e.getMessage());
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("收藏帖子异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("收藏失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 取消收藏帖子
     *
     * @param postId 帖子ID
     * @return 响应结果
     */
    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Boolean> unfavoritePost(@PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = favoriteApplicationService.unfavoritePost(userId, postId);
            
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .info(result ? "取消收藏成功" : "取消收藏失败")
                    .build();
        } catch (BusinessException e) {
            log.warn("取消收藏帖子失败: {}", e.getMessage());
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("取消收藏帖子异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("取消收藏失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 检查是否已收藏帖子
     *
     * @param postId 帖子ID
     * @return 响应结果
     */
    @GetMapping("/post/{postId}/check")
    public ResponseEntity<Boolean> isPostFavorited(@PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = favoriteApplicationService.isPostFavorited(userId, postId);
            
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .build();
        } catch (Exception e) {
            log.error("检查帖子收藏状态异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("检查失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 获取用户收藏的帖子数量
     *
     * @return 响应结果
     */
    @GetMapping("/count")
    public ResponseEntity<Integer> getFavoriteCount() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            int count = favoriteApplicationService.getFavoriteCount(userId);
            
            return ResponseEntity.<Integer>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(count)
                    .build();
        } catch (Exception e) {
            log.error("获取收藏帖子数量异常", e);
            return ResponseEntity.<Integer>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 获取收藏帖子排行榜
     *
     * @param limit 排行榜数量，默认为10
     * @return 响应结果
     */
    @GetMapping("/ranking")
    public ResponseEntity<List<PostListResponse>> getFavoriteRanking(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // 限制最大返回数量
            int safeLimit = Math.max(1, Math.min(limit, 100));
            
            // 获取收藏数量排行榜
            // 这里简化实现，实际应该从数据库或缓存中获取真实的收藏排行榜数据
            // 可以通过查询post表中favorite_count字段进行排序来实现
            
            // 先获取一些帖子作为示例
            List<PostEntity> posts = postService.getPostPageList(1, safeLimit);
            
            // 按收藏数排序
            posts.sort((p1, p2) -> {
                long favoriteCount1 = p1.getFavoriteCount() != null ? p1.getFavoriteCount() : 0L;
                long favoriteCount2 = p2.getFavoriteCount() != null ? p2.getFavoriteCount() : 0L;
                return Long.compare(favoriteCount2, favoriteCount1); // 降序排列
            });
            
            // 转换为PostListResponse
            List<PostListResponse> responses = posts.stream()
                    .map(post -> {
                        PostListResponse response = new PostListResponse();
                        response.setPost(post);
                        // 这里可以设置其他需要的字段
                        return response;
                    })
                    .collect(Collectors.toList());
            
            return ResponseEntity.<List<PostListResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(responses)
                    .build();
        } catch (Exception e) {
            log.error("获取收藏帖子排行榜异常", e);
            return ResponseEntity.<List<PostListResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 获取用户的收藏夹列表
     *
     * @return 响应结果
     */
    @GetMapping("/folders")
    public ResponseEntity<List<FavoriteFolderResponse>> getUserFolders() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<FavoriteFolderEntity> folders = favoriteApplicationService.getUserFolders(userId);
            
            List<FavoriteFolderResponse> responses = folders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.<List<FavoriteFolderResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(responses)
                    .build();
        } catch (Exception e) {
            log.error("获取收藏夹列表异常", e);
            return ResponseEntity.<List<FavoriteFolderResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 收藏帖子到指定收藏夹
     *
     * @param request 收藏请求
     * @return 响应结果
     */
    @PostMapping("/folder/post")
    public ResponseEntity<Boolean> favoritePostToFolder(@Valid @RequestBody FavoritePostToFolderRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = favoriteApplicationService.favoritePostToFolder(userId, request.getFolderId(), request.getPostId());
            
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .info(result ? "收藏成功" : "收藏失败")
                    .build();
        } catch (BusinessException e) {
            log.warn("收藏帖子到收藏夹失败: {}", e.getMessage());
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("收藏帖子到收藏夹异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("收藏失败，请稍后重试")
                    .build();
        }
    }
    
    private FavoriteFolderResponse convertToResponse(FavoriteFolderEntity folder) {
        FavoriteFolderResponse response = new FavoriteFolderResponse();
        response.setId(folder.getId());
        response.setName(folder.getName());
        response.setDescription(folder.getDescription());
        response.setIsPublic(folder.getIsPublic());
        response.setContentCount(folder.getContentCount());
        response.setCreateTime(folder.getCreateTime());
        return response;
    }
}