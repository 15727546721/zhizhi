package cn.xu.api.system.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.system.model.dto.post.CreatePostRequest;
import cn.xu.api.system.model.dto.post.PublishPostRequest;
import cn.xu.api.system.model.dto.post.SysPostQueryRequest;
import cn.xu.api.system.model.vo.SysPostDetailResponse;
import cn.xu.api.web.model.vo.post.PostPageResponse;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.post.event.PostEventPublisher;
import cn.xu.domain.post.model.aggregate.PostAndAuthorAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostContent;
import cn.xu.domain.post.model.valobj.PostStatus;
import cn.xu.domain.post.model.valobj.PostTitle;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.service.*;
import cn.xu.infrastructure.persistent.read.elastic.service.PostElasticService;
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

@Tag(name = "帖子管理", description = "帖子管理相关接口")
@Slf4j
@RequestMapping("system/post")
@RestController
public class SysPostController {

    @Resource
    private IPostService postService;
    @Resource
    private IPostTagService postTagService;
    @Resource
    private IPostTopicService postTopicService;
    @Resource
    private ITagService tagService;
    @Resource
    private ICategoryService categoryService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Autowired(required = false) // 设置为非必需，允许Elasticsearch不可用
    private PostElasticService postElasticService;
    @Resource
    private PostEventPublisher eventPublisher;

    @PostMapping("/uploadCover")
    @Operation(summary = "上传帖子封面")
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
                
                //1. 保存帖子和分类id
                PostEntity post = PostEntity.builder()
                        .categoryId(createPostRequest.getCategoryId())
                        .title(new PostTitle(createPostRequest.getTitle()))
                        .coverUrl(createPostRequest.getCoverUrl())
                        .content(new PostContent(createPostRequest.getContent()))
                        .description(createPostRequest.getDescription())
                        .userId(StpUtil.getLoginIdAsLong())
                        .type(PostType.fromCode(createPostRequest.getType()))
                        .status(PostStatus.PUBLISHED)
                        .build();
                Long postId = postService.createPost(post);
                post.setId(postId);
                
                //2. 保存帖子标签
                if (createPostRequest.getTagIds() != null && !createPostRequest.getTagIds().isEmpty()) {
                    if (createPostRequest.getTagIds().size() > 3) {
                        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                    }
                    postTagService.savePostTags(postId, createPostRequest.getTagIds());
                }

                //3. 保存帖子话题
                if (createPostRequest.getTopicIds() != null && !createPostRequest.getTopicIds().isEmpty()) {
                    if (createPostRequest.getTopicIds().size() > 10) {
                        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "话题不能超过10个");
                    }
                    postTopicService.savePostTopics(postId, createPostRequest.getTopicIds());
                }

                //4. 发布帖子创建事件
                eventPublisher.publishCreated(post);

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
    @ApiOperationLog(description = "更新帖子")
    public ResponseEntity updatePost(@RequestBody @Valid CreatePostRequest updatePostRequest) {
        log.info("帖子更新参数: {}", updatePostRequest);
        transactionTemplate.execute(status -> {
            try {
                // 根据请求中的状态字段确定帖子状态，默认为已发布
                PostStatus postStatus = PostStatus.PUBLISHED;
                if ("DRAFT".equals(updatePostRequest.getStatus())) {
                    postStatus = PostStatus.DRAFT;
                }
                
                //1. 更新帖子和分类id
                PostEntity post = PostEntity.builder()
                        .id(updatePostRequest.getId())
                        .categoryId(updatePostRequest.getCategoryId())
                        .title(new PostTitle(updatePostRequest.getTitle()))
                        .coverUrl(updatePostRequest.getCoverUrl())
                        .content(new PostContent(updatePostRequest.getContent()))
                        .description(updatePostRequest.getDescription())
                        .userId(StpUtil.getLoginIdAsLong())
                        .type(PostType.fromCode(updatePostRequest.getType()))
                        .status(postStatus)
                        .build();
                postService.updatePost(post);

                //2. 更新帖子标签
                if (updatePostRequest.getTagIds() != null && !updatePostRequest.getTagIds().isEmpty()) {
                    if (updatePostRequest.getTagIds().size() > 3) {
                        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                    }
                    postTagService.savePostTags(updatePostRequest.getId(), updatePostRequest.getTagIds());
                }

                //3. 更新帖子话题
                if (updatePostRequest.getTopicIds() != null && !updatePostRequest.getTopicIds().isEmpty()) {
                    if (updatePostRequest.getTopicIds().size() > 10) {
                        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "话题不能超过10个");
                    }
                    postTopicService.savePostTopics(updatePostRequest.getId(), updatePostRequest.getTopicIds());
                }

                //4. 发布帖子更新事件
                eventPublisher.publishUpdated(post);

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
    @ApiOperationLog(description = "删除帖子")
    public ResponseEntity deletePosts(@Parameter(description = "帖子ID列表") @RequestBody List<Long> postIds) {
        try {
            for (Long postId : postIds) {
                postService.deletePosts(postIds);
                // 发布帖子删除事件
                PostEntity post = PostEntity.builder()
                        .id(postId)
                        .build();
                eventPublisher.publishDeleted(postId);
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
    @ApiOperationLog(description = "获取帖子列表")
    public ResponseEntity<PageResponse<List<PostPageResponse>>> listPost(@Valid SysPostQueryRequest postRequest) {
        log.info("帖子列表获取参数: {}", postRequest);

        // 参数校验和默认值设置
        if (postRequest.getPageNo() == null || postRequest.getPageNo() < 1) {
            postRequest.setPageNo(1);
        }
        if (postRequest.getPageSize() == null || postRequest.getPageSize() < 1) {
            postRequest.setPageSize(10);
        }

        // 查询帖子列表
        PageResponse<List<PostPageResponse>> postList = postService.listPost(postRequest);
        log.info("帖子列表获取结果: {}", postList);

        return ResponseEntity.<PageResponse<List<PostPageResponse>>>builder()
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
    @ApiOperationLog(description = "获取帖子详情")
    public ResponseEntity<SysPostDetailResponse> getPost(@Parameter(description = "帖子ID") @PathVariable("id") Long id) {
        log.info("帖子详情获取参数: id={}", id);
        if (id == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子ID不能为空");
        }
        // 这里需要调整，因为PostService可能没有完全对应的方法
        PostAndAuthorAggregate post = null; // postService.getPostDetailById(id);
        // CategoryEntity category = categoryService.getCategoryByPostId(id);
        // List<TagEntity> tag = tagService.getTagsByPostId(id);

        return ResponseEntity.<SysPostDetailResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子获取成功")
                .data(SysPostDetailResponse.builder()
                        .postAndAuthorAggregate(post)
                        // .categoryName(category.getName())
                        // .tags(tag.stream().map(TagEntity::getName).collect(Collectors.toList()))
                        .build())
                .build();

    }

    @PostMapping("/search")
    @Operation(summary = "搜索帖子")
    @ApiOperationLog(description = "搜索帖子")
    public ResponseEntity<List<PostEntity>> searchPosts(@Parameter(description = "搜索关键词") @RequestParam String title) {
        try {
            if (StringUtils.isBlank(title)) {
                return ResponseEntity.<List<PostEntity>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info("搜索关键词不能为空")
                    .build();
            }

            // 检查Elasticsearch是否可用
            if (postElasticService == null) {
                return ResponseEntity.<List<PostEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索服务不可用")
                    .build();
            }

            List<PostEntity> posts = null; // postElasticService.searchPosts(title);
            return ResponseEntity.<List<PostEntity>>builder()
                .data(posts)
                .code(ResponseCode.SUCCESS.getCode())
                .info("搜索成功")
                .build();
        } catch (Exception e) {
            log.error("帖子搜索失败: {}", e.getMessage(), e);
            return ResponseEntity.<List<PostEntity>>builder()
                .code(ResponseCode.UN_ERROR.getCode())
                .info("搜索失败")
                .build();
        }
    }

    @PostMapping("/publish")
    @Operation(summary = "发布帖子")
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
                
                //1. 保存帖子和分类id
                PostEntity post = PostEntity.builder()
                        .categoryId(publishPostRequest.getCategoryId())
                        .title(new PostTitle(publishPostRequest.getTitle()))
                        .coverUrl(publishPostRequest.getCoverUrl())
                        .content(new PostContent(publishPostRequest.getContent()))
                        .description(publishPostRequest.getDescription())
                        .userId(userId)
                        .type(PostType.fromCode(publishPostRequest.getType()))
                        .status(PostStatus.PUBLISHED)
                        .build();
                
                postId[0] = postService.createPost(post);
                post.setId(postId[0]);
                
                //2. 保存帖子标签
                if (publishPostRequest.getTagIds() == null || publishPostRequest.getTagIds().isEmpty()) {
                    throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "标签不能为空");
                }
                if (publishPostRequest.getTagIds().size() > 3) {
                    throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                }
                postTagService.savePostTags(postId[0], publishPostRequest.getTagIds());
                
                //3. 保存帖子话题
                if (publishPostRequest.getTopicIds() != null && !publishPostRequest.getTopicIds().isEmpty()) {
                    if (publishPostRequest.getTopicIds().size() > 10) {
                        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "话题不能超过10个");
                    }
                    postTopicService.savePostTopics(postId[0], publishPostRequest.getTopicIds());
                }
                
                // 4. 发布帖子创建事件
                eventPublisher.publishCreated(post);
                
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
}