package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.controller.admin.model.dto.post.CreatePostRequest;
import cn.xu.controller.admin.model.dto.post.PublishPostRequest;
import cn.xu.controller.admin.model.dto.post.SysPostQueryRequest;
import cn.xu.controller.admin.model.vo.SysPostDetailResponse;
import cn.xu.controller.admin.model.vo.SysPostListVO;
import cn.xu.event.post.PostEvent;
import cn.xu.event.post.PostEventPublisher;
import cn.xu.model.entity.Post;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.service.post.PostService;
import cn.xu.service.post.TagService;
import cn.xu.service.search.PostSearchService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 帖子管理控制器
 * 
 * @author xu
 * @since 2025-11-30
 */
@Slf4j
@RestController
@RequestMapping("/api/system/post")
@Tag(name = "帖子管理", description = "帖子管理相关接口")
public class SysPostController {

    @Resource(name = "postService")
    private PostService postService;
    @Resource(name = "tagService")
    private TagService tagService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private PostMapper postMapper;
    @Autowired(required = false) // 设置为非必需，允许Elasticsearch不可用
    private PostSearchService postSearchService;
    @Autowired
    private PostEventPublisher postEventPublisher;

    @PostMapping("/uploadCover")
    @Operation(summary = "上传帖子封面")
    @SaCheckLogin
    @ApiOperationLog(description = "上传帖子封面")
    public ResponseEntity<String> uploadPostCover(@Parameter(description = "封面文件") @RequestPart("files") MultipartFile file) {
        String coverUrl = postService.uploadCover(file);
        return ResponseEntity.<String>builder()
                .data(coverUrl)
                .code(ResponseCode.SUCCESS.getCode())
                .info("上传封面成功")
                .build();
    }

    @PostMapping("/add")
    @Operation(summary = "创建帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:add")
    @ApiOperationLog(description = "创建帖子")
    public ResponseEntity savePost(@RequestBody @Valid CreatePostRequest createPostRequest) {
        log.info("帖子创建参数: {}", createPostRequest);
        transactionTemplate.execute(status -> {
            try {
                // 根据请求中的状态字段确定帖子状态，默认为已发布
                String postStatus = "PUBLISHED";
                if ("DRAFT".equals(createPostRequest.getStatus())) {
                    postStatus = "DRAFT";
                }

                //1. 保存帖子
                Long userId = StpUtil.getLoginIdAsLong();
                Long postId;
                if ("DRAFT".equals(postStatus)) {
                    postId = postService.createDraft(
                            userId,
                            createPostRequest.getTitle(),
                            createPostRequest.getContent(),
                            createPostRequest.getDescription(),
                            createPostRequest.getCoverUrl(),
                            createPostRequest.getTagIds()
                    );
                } else {
                    postId = postService.publishPost(
                            null,
                            userId,
                            createPostRequest.getTitle(),
                            createPostRequest.getContent(),
                            createPostRequest.getDescription(),
                            createPostRequest.getCoverUrl(),
                            createPostRequest.getTagIds()
                    );
                }

                // 标签已在publishPost/createDraft中处理
                if (createPostRequest.getTagIds() != null && createPostRequest.getTagIds().size() > 5) {
                    throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过5个");
                }

                //4. 发布帖子创建事件
                PostEvent event = PostEvent.builder()
                        .eventType(PostEvent.PostEventType.CREATED)
                        .postId(postId)
                        .userId(userId)
                        .title(createPostRequest.getTitle())
                        .description(createPostRequest.getDescription())
                        .build();
                postEventPublisher.publishEvent(event);

                return 1;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("帖子创建失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子创建失败");
            }
        });
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子创建成功")
                .build();
    }

    @PostMapping("/update")
    @Operation(summary = "更新帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:update")
    @ApiOperationLog(description = "更新帖子")
    public ResponseEntity updatePost(@RequestBody @Valid CreatePostRequest updatePostRequest) {
        log.info("帖子更新参数: {}", updatePostRequest);
        transactionTemplate.execute(status -> {
            try {
                //1. 更新帖子
                Long userId = StpUtil.getLoginIdAsLong();
                String postStatus = "DRAFT".equals(updatePostRequest.getStatus()) ? "DRAFT" : "PUBLISHED";

                if ("DRAFT".equals(postStatus)) {
                    postService.updateDraft(
                            updatePostRequest.getId(),
                            userId,
                            updatePostRequest.getTitle(),
                            updatePostRequest.getContent(),
                            updatePostRequest.getDescription(),
                            updatePostRequest.getCoverUrl(),
                            updatePostRequest.getTagIds()
                    );
                } else {
                    postService.publishPost(
                            updatePostRequest.getId(),
                            userId,
                            updatePostRequest.getTitle(),
                            updatePostRequest.getContent(),
                            updatePostRequest.getDescription(),
                            updatePostRequest.getCoverUrl(),
                            updatePostRequest.getTagIds()
                    );
                }

                // 标签已在publishPost/updateDraft中处理
                if (updatePostRequest.getTagIds() != null && updatePostRequest.getTagIds().size() > 5) {
                    throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过5个");
                }

                //4. 发布帖子更新事件
                PostEvent event = PostEvent.builder()
                        .eventType(PostEvent.PostEventType.UPDATED)
                        .postId(updatePostRequest.getId())
                        .userId(userId)
                        .title(updatePostRequest.getTitle())
                        .description(updatePostRequest.getDescription())
                        .build();
                postEventPublisher.publishEvent(event);

                return 1;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("帖子更新失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子更新失败");
            }
        });
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子更新成功")
                .build();
    }

    @PostMapping("/delete")
    @Operation(summary = "删除帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:delete")
    @ApiOperationLog(description = "删除帖子")
    public ResponseEntity deletePosts(@Parameter(description = "帖子ID列表") @RequestBody List<Long> postIds) {
        try {
            // 批量删除帖子
            postService.batchDeletePosts(postIds);

            // 为每个删除的帖子发布事件
            for (Long postId : postIds) {
                PostEvent event = PostEvent.builder()
                        .eventType(PostEvent.PostEventType.DELETED)
                        .postId(postId)
                        .build();
                postEventPublisher.publishEvent(event);
            }
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("帖子批量删除成功")
                    .build();
        } catch (Exception e) {
            log.error("帖子删除失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("帖子删除失败")
                    .build();
        }
    }

    @GetMapping("/list")
    @Operation(summary = "帖子列表")
    @SaCheckLogin
    @SaCheckPermission("system:post:list")
    @ApiOperationLog(description = "获取帖子列表")
    public ResponseEntity<PageResponse<List<SysPostListVO>>> listPost(@Valid SysPostQueryRequest postRequest) {
        log.info("帖子列表获取参数: {}", postRequest);

        // 参数校验和默认值设置
        if (postRequest.getPageNo() == null || postRequest.getPageNo() < 1) {
            postRequest.setPageNo(1);
        }
        if (postRequest.getPageSize() == null || postRequest.getPageSize() < 1) {
            postRequest.setPageSize(10);
        }

        // 计算偏移量
        int offset = (postRequest.getPageNo() - 1) * postRequest.getPageSize();
        
        // 使用优化查询（不查询content字段）
        List<SysPostListVO> posts = postMapper.findPostListForAdmin(
                postRequest.getTitle(),
                postRequest.getStatus(),
                postRequest.getUserId(),
                postRequest.getTagId(),
                offset,
                postRequest.getPageSize()
        );
        
        // 统计总数
        Long total = postMapper.countPostsForAdmin(
                postRequest.getTitle(),
                postRequest.getStatus(),
                postRequest.getUserId(),
                postRequest.getTagId()
        );
        
        PageResponse<List<SysPostListVO>> postList = PageResponse.ofList(
                postRequest.getPageNo(),
                postRequest.getPageSize(),
                total != null ? total : 0L,
                posts
        );

        return ResponseEntity.<PageResponse<List<SysPostListVO>>>builder()
                .data(postList)
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子列表获取成功")
                .build();
    }

    /**
     * 获取帖子详情
     *
     * @param id
     * @return
     */
    @GetMapping("info/{id}")
    @Operation(summary = "获取帖子详情")
    @SaCheckLogin
    @SaCheckPermission("system:post:list")
    @ApiOperationLog(description = "获取帖子详情")
    public ResponseEntity<SysPostDetailResponse> getPost(@Parameter(description = "帖子ID") @PathVariable("id") Long id) {
        log.info("帖子详情获取参数: id={}", id);
        if (id == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子ID不能为空");
        }
        Post post = postService.getPostById(id).orElse(null);
        if (post == null) {
            throw new BusinessException(ResponseCode.NOT_FOUND.getCode(), "帖子不存在");
        }
        
        // 获取帖子标签
        List<cn.xu.model.entity.Tag> tags = tagService.getTagsByPostId(id);
        List<String> tagNames = tags.stream()
                .map(cn.xu.model.entity.Tag::getName)
                .collect(java.util.stream.Collectors.toList());
        List<Long> tagIds = tags.stream()
                .map(cn.xu.model.entity.Tag::getId)
                .collect(java.util.stream.Collectors.toList());

        return ResponseEntity.<SysPostDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子获取成功")
                .data(SysPostDetailResponse.builder()
                        .post(post)
                        .tagNames(tagNames)
                        .tagIds(tagIds)
                        .build())
                .build();
    }

    @PostMapping("/search")
    @Operation(summary = "搜索帖子")
    @ApiOperationLog(description = "搜索帖子")
    public ResponseEntity<List<Post>> searchPosts(@Parameter(description = "搜索关键词") @RequestParam String title) {
        try {
            if (StringUtils.isBlank(title)) {
                return ResponseEntity.<List<Post>>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("搜索关键词不能为空")
                        .build();
            }

            // 检查搜索服务是否可用
            if (postSearchService == null) {
                return ResponseEntity.<List<Post>>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("搜索服务不可用")
                        .build();
            }

            // 使用PostSearchService进行搜索
            org.springframework.data.domain.Page<Post> searchResult = postSearchService.search(
                    title, null, org.springframework.data.domain.PageRequest.of(0, 20));
            List<Post> posts = searchResult.getContent();
            return ResponseEntity.<List<Post>>builder()
                    .data(posts)
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("搜索成功")
                    .build();
        } catch (Exception e) {
            log.error("帖子搜索失败: {}", e.getMessage(), e);
            return ResponseEntity.<List<Post>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索失败")
                    .build();
        }
    }

    @PostMapping("/publish")
    @Operation(summary = "发布帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:publish")
    @ApiOperationLog(description = "发布帖子")
    public ResponseEntity pushPost(@RequestBody PublishPostRequest publishPostRequest) {
        log.info("发布帖子，帖子内容：{}", publishPostRequest);
        Long userId = StpUtil.getLoginIdAsLong();

        final Long[] postId = new Long[1];

        transactionTemplate.execute(status -> {
            try {
                // 根据请求中的状态字段确定帖子状态，默认为已发布
                String postStatus = "PUBLISHED";
                if ("DRAFT".equals(publishPostRequest.getStatus())) {
                    postStatus = "DRAFT";
                }

                //1. 发布帖子
                if ("DRAFT".equals(postStatus)) {
                    postId[0] = postService.createDraft(
                            userId,
                            publishPostRequest.getTitle(),
                            publishPostRequest.getContent(),
                            publishPostRequest.getDescription(),
                            publishPostRequest.getCoverUrl(),
                            publishPostRequest.getTagIds()
                    );
                } else {
                    postId[0] = postService.publishPost(
                            null,
                            userId,
                            publishPostRequest.getTitle(),
                            publishPostRequest.getContent(),
                            publishPostRequest.getDescription(),
                            publishPostRequest.getCoverUrl(),
                            publishPostRequest.getTagIds()
                    );
                }

                //2. 验证标签
                if (publishPostRequest.getTagIds() == null || publishPostRequest.getTagIds().isEmpty()) {
                    throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "标签不能为空");
                }
                if (publishPostRequest.getTagIds().size() > 5) {
                    throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过5个");
                }

                // 4. 发布帖子创建事件
                PostEvent event = PostEvent.builder()
                        .eventType(PostEvent.PostEventType.CREATED)
                        .postId(postId[0])
                        .userId(userId)
                        .title(publishPostRequest.getTitle())
                        .description(publishPostRequest.getDescription())
                        .build();
                postEventPublisher.publishEvent(event);

                return 1;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.error("帖子发布失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子发布失败");
            }
        });
        
        log.info("帖子发布成功");
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子发布成功")
                .build();
    }

    @PostMapping("/top")
    @Operation(summary = "置顶/取消置顶帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:top")
    @ApiOperationLog(description = "置顶/取消置顶帖子")
    public ResponseEntity toggleTop(@RequestBody java.util.Map<String, Long> request) {
        Long postId = request.get("id");
        if (postId == null) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info("帖子ID不能为空")
                    .build();
        }
        postService.toggleFeatured(postId);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("操作成功")
                .build();
    }

    @PostMapping("/toggleArticlePublication")
    @Operation(summary = "发布/下架帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:publish")
    @ApiOperationLog(description = "发布/下架帖子")
    public ResponseEntity togglePublication(@RequestBody java.util.Map<String, Long> request) {
        Long postId = request.get("id");
        if (postId == null) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info("帖子ID不能为空")
                    .build();
        }
        postService.togglePublish(postId);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("操作成功")
                .build();
    }

    @PostMapping("/featured")
    @Operation(summary = "加精/取消加精帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:update")
    @ApiOperationLog(description = "加精/取消加精帖子")
    public ResponseEntity toggleFeatured(@RequestBody java.util.Map<String, Long> request) {
        Long postId = request.get("id");
        if (postId == null) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info("帖子ID不能为空")
                    .build();
        }
        postService.toggleFeatured(postId);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("操作成功")
                .build();
    }

    @PostMapping("/status")
    @Operation(summary = "发布/下架帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:update")
    @ApiOperationLog(description = "发布/下架帖子")
    public ResponseEntity toggleStatus(@RequestBody java.util.Map<String, Long> request) {
        Long postId = request.get("id");
        if (postId == null) {
            return ResponseEntity.builder()
                    .code(ResponseCode.NULL_PARAMETER.getCode())
                    .info("帖子ID不能为空")
                    .build();
        }
        postService.togglePublish(postId);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("操作成功")
                .build();
    }

    @GetMapping("/randomImg")
    @Operation(summary = "获取随机封面图")
    @ApiOperationLog(description = "获取随机封面图")
    public ResponseEntity<String> randomCoverImage() {
        // 返回一个默认的随机封面图URL
        String[] defaultCovers = {
            "https://picsum.photos/800/400?random=1",
            "https://picsum.photos/800/400?random=2",
            "https://picsum.photos/800/400?random=3"
        };
        int index = (int) (Math.random() * defaultCovers.length);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(defaultCovers[index])
                .build();
    }
}
