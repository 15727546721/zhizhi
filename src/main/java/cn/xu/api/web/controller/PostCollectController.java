package cn.xu.api.web.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.post.CollectPostToFolderRequest;
import cn.xu.api.web.model.dto.post.CreateCollectFolderRequest;
import cn.xu.api.web.model.dto.post.UpdateCollectFolderRequest;
import cn.xu.api.web.model.vo.post.CollectFolderResponse;
import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.application.service.PostCollectApplicationService;
import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.collect.model.entity.CollectFolderEntity;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.service.IPostCollectService;
import cn.xu.domain.post.service.IPostService;
import cn.xu.domain.post.service.PostCollectServiceAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

/**
 * 帖子收藏接口
 * 提供帖子收藏相关的API接口
 */
@Slf4j
@RestController
@RequestMapping("/api/post/collect")
public class PostCollectController {

    @Autowired
    private PostCollectServiceAdapter postCollectServiceAdapter;

    @Autowired
    private IPostCollectService postCollectService;

    @Autowired
    private PostCollectApplicationService postCollectApplicationService;

    @Autowired
    private IPostService postService;

    /**
     * 收藏帖子
     *
     * @param postId 帖子ID
     * @return 响应结果
     */
    @PostMapping("/post/{postId}")
    public ResponseEntity<Boolean> collectPost(@PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = postCollectApplicationService.collectPost(userId, postId);
            
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
    public ResponseEntity<Boolean> uncollectPost(@PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = postCollectApplicationService.uncollectPost(userId, postId);
            
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
    public ResponseEntity<Boolean> isPostCollected(@PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = postCollectApplicationService.isPostCollected(userId, postId);
            
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
    public ResponseEntity<Integer> getCollectCount() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            int count = postCollectApplicationService.getCollectCount(userId);
            
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
    public ResponseEntity<List<PostListResponse>> getCollectRanking(
            @RequestParam(defaultValue = "10") int limit) {
        try {
            // 限制最大返回数量
            int safeLimit = Math.max(1, Math.min(limit, 100));
            
            // 获取收藏数量排行榜
            // 这里简化实现，实际应该从数据库或缓存中获取真实的收藏排行榜数据
            // 可以通过查询post表中collect_count字段进行排序来实现
            
            // 先获取一些帖子作为示例
            List<PostEntity> posts = postService.getPostPageList(1, safeLimit);
            
            // 按收藏数排序
            posts.sort((p1, p2) -> {
                long collectCount1 = p1.getCollectCount() != null ? p1.getCollectCount() : 0L;
                long collectCount2 = p2.getCollectCount() != null ? p2.getCollectCount() : 0L;
                return Long.compare(collectCount2, collectCount1); // 降序排列
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
     * 创建收藏夹
     *
     * @param request 创建收藏夹请求
     * @return 响应结果
     */
    @PostMapping("/folder")
    public ResponseEntity<CollectFolderResponse> createFolder(@Valid @RequestBody CreateCollectFolderRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            CollectFolderEntity folder = postCollectApplicationService.createFolder(
                    userId,
                    request.getName(),
                    request.getDescription(),
                    request.getIsPublic() != null && request.getIsPublic() == 1
            );
            
            CollectFolderResponse response = convertToResponse(folder);
            
            return ResponseEntity.<CollectFolderResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(response)
                    .info("创建成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("创建收藏夹失败: {}", e.getMessage());
            return ResponseEntity.<CollectFolderResponse>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("创建收藏夹异常", e);
            return ResponseEntity.<CollectFolderResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("创建失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 更新收藏夹
     *
     * @param folderId 收藏夹ID
     * @param request  更新收藏夹请求
     * @return 响应结果
     */
    @PutMapping("/folder/{folderId}")
    public ResponseEntity<Boolean> updateFolder(@PathVariable Long folderId, @Valid @RequestBody UpdateCollectFolderRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = postCollectApplicationService.updateFolder(
                    userId,
                    folderId,
                    request.getName(),
                    request.getDescription(),
                    request.getIsPublic() != null && request.getIsPublic() == 1
            );
            
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .info(result ? "更新成功" : "更新失败")
                    .build();
        } catch (BusinessException e) {
            log.warn("更新收藏夹失败: {}", e.getMessage());
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("更新收藏夹异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("更新失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 删除收藏夹
     *
     * @param folderId 收藏夹ID
     * @return 响应结果
     */
    @DeleteMapping("/folder/{folderId}")
    public ResponseEntity<Boolean> deleteFolder(@PathVariable Long folderId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = postCollectApplicationService.deleteFolder(userId, folderId);
            
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .info(result ? "删除成功" : "删除失败")
                    .build();
        } catch (BusinessException e) {
            log.warn("删除收藏夹失败: {}", e.getMessage());
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("删除收藏夹异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("删除失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 获取用户的收藏夹列表
     *
     * @return 响应结果
     */
    @GetMapping("/folders")
    public ResponseEntity<List<CollectFolderResponse>> getUserFolders() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            List<CollectFolderEntity> folders = postCollectApplicationService.getUserFolders(userId);
            
            List<CollectFolderResponse> responses = folders.stream()
                    .map(this::convertToResponse)
                    .collect(Collectors.toList());
            
            return ResponseEntity.<List<CollectFolderResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(responses)
                    .build();
        } catch (Exception e) {
            log.error("获取收藏夹列表异常", e);
            return ResponseEntity.<List<CollectFolderResponse>>builder()
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
    public ResponseEntity<Boolean> collectPostToFolder(@Valid @RequestBody CollectPostToFolderRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = postCollectApplicationService.collectPostToFolder(userId, request.getFolderId(), request.getPostId());
            
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

    /**
     * 从收藏夹中移除帖子
     *
     * @param folderId 收藏夹ID
     * @param postId   帖子ID
     * @return 响应结果
     */
    @DeleteMapping("/folder/{folderId}/post/{postId}")
    public ResponseEntity<Boolean> uncollectPostFromFolder(@PathVariable Long folderId, @PathVariable Long postId) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            boolean result = postCollectApplicationService.uncollectPostFromFolder(userId, folderId, postId);
            
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(result)
                    .info(result ? "移除成功" : "移除失败")
                    .build();
        } catch (BusinessException e) {
            log.warn("从收藏夹移除帖子失败: {}", e.getMessage());
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("从收藏夹移除帖子异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("移除失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 将收藏夹实体转换为响应对象
     *
     * @param entity 收藏夹实体
     * @return 收藏夹响应对象
     */
    private CollectFolderResponse convertToResponse(CollectFolderEntity entity) {
        if (entity == null) {
            return null;
        }

        CollectFolderResponse response = new CollectFolderResponse();
        response.setId(entity.getId());
        response.setUserId(entity.getUserId());
        response.setName(entity.getName());
        response.setDescription(entity.getDescription());
        response.setIsDefault(entity.getIsDefault() != null && entity.getIsDefault() == 1 ? 1 : 0);
        response.setPostCount(entity.getContentCount());
        response.setIsPublic(entity.getIsPublic() != null && entity.getIsPublic() == 1 ? 1 : 0);
        response.setSort(entity.getSort());
        response.setCreateTime(entity.getCreateTime());
        response.setUpdateTime(entity.getUpdateTime());

        return response;
    }
}