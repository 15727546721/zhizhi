package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.post.CreatePostRequest;
import cn.xu.model.dto.post.PublishPostRequest;
import cn.xu.model.dto.post.SysPostQueryRequest;
import cn.xu.model.vo.post.SysPostDetailVO;
import cn.xu.model.vo.post.SysPostListVO;
import cn.xu.event.publisher.ContentEventPublisher;
import cn.xu.model.entity.Post;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.service.post.PostCommandService;
import cn.xu.service.post.PostQueryService;
import cn.xu.service.post.TagService;
import cn.xu.service.search.PostSearchService;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.LoginUserUtil;
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
 * <p>提供后台帖子管理功能，包括创建、编辑、删除、置顶、加精等</p>
 * <p>需要登录并拥有相应权限</p>
 
 */
@Slf4j
@RestController
@RequestMapping("/api/system/post")
@Tag(name = "帖子管理", description = "帖子管理相关接口")
public class SysPostController {

    @Resource
    private PostCommandService postCommandService;
    @Resource
    private PostQueryService postQueryService;
    @Resource(name = "tagService")
    private TagService tagService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private PostMapper postMapper;
    @Autowired(required = false) // 设置为非必需，允许Elasticsearch不可用
    private PostSearchService postSearchService;
    @Autowired
    private ContentEventPublisher contentEventPublisher;

    /**
     * 上传帖子封面
     * 
     * <p>上传帖子封面图片，返回图片URL
     * <p>需要登录后才能访问
     * 
     * @param file 封面图片文件
     * @return 上传成功的图片URL
     */
    @PostMapping("/uploadCover")
    @Operation(summary = "上传帖子封面")
    @SaCheckLogin
    @ApiOperationLog(description = "上传帖子封面")
    public ResponseEntity<String> uploadPostCover(@Parameter(description = "封面文件") @RequestPart("files") MultipartFile file) {
        String coverUrl = postCommandService.uploadCover(file);
        return ResponseEntity.<String>builder()
                .data(coverUrl)
                .code(ResponseCode.SUCCESS.getCode())
                .info("上传封面成功")
                .build();
    }

    /**
     * 创建帖子
     * 
     * <p>后台创建新帖子，支持保存为草稿或直接发布
     * <p>需要system:post:add权限
     * 
     * @param createPostRequest 帖子创建请求，包含标题、内容、封面、标签等
     * @return 创建结果
     * @throws BusinessException 当标签超过5个或创建失败时抛出
     */
    @PostMapping("/add")
    @Operation(summary = "创建帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:add")
    @ApiOperationLog(description = "创建帖子")
    public ResponseEntity savePost(@RequestBody @Valid CreatePostRequest createPostRequest) {
        log.info("帖子创建参数: {}", createPostRequest);
        
        // 前置校验
        validateTagIds(createPostRequest.getTagIds(), false);
        
        Long userId = LoginUserUtil.getLoginUserId();
        boolean isDraft = "DRAFT".equals(createPostRequest.getStatus());
        
        Long postId = executePostOperation(userId, null, createPostRequest, isDraft);
        
        // 发布帖子创建事件
        contentEventPublisher.publishPostCreated(userId, postId, createPostRequest.getTitle());
        
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子创建成功")
                .build();
    }

    /**
     * 更新帖子
     * 
     * <p>后台更新帖子内容，支持修改标题、内容、封面、标签等
     * <p>需要system:post:update权限
     * 
     * @param updatePostRequest 帖子更新请求，必须包含帖子ID
     * @return 更新结果
     * @throws BusinessException 当标签超过5个或更新失败时抛出
     */
    @PostMapping("/update")
    @Operation(summary = "更新帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:update")
    @ApiOperationLog(description = "更新帖子")
    public ResponseEntity updatePost(@RequestBody @Valid CreatePostRequest updatePostRequest) {
        log.info("帖子更新参数: {}", updatePostRequest);
        
        // 前置校验
        validateTagIds(updatePostRequest.getTagIds(), false);
        
        Long userId = LoginUserUtil.getLoginUserId();
        boolean isDraft = "DRAFT".equals(updatePostRequest.getStatus());
        
        executePostOperation(userId, updatePostRequest.getId(), updatePostRequest, isDraft);
        
        // 发布帖子更新事件
        contentEventPublisher.publishPostUpdated(userId, updatePostRequest.getId(), updatePostRequest.getTitle());
        
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子更新成功")
                .build();
    }
    
    /**
     * 校验标签ID列表
     */
    private void validateTagIds(List<Long> tagIds, boolean required) {
        if (required && (tagIds == null || tagIds.isEmpty())) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "标签不能为空");
        }
        if (tagIds != null && tagIds.size() > 5) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过5个");
        }
    }
    
    /**
     * 执行帖子创建/更新操作
     */
    private Long executePostOperation(Long userId, Long postId, CreatePostRequest request, boolean isDraft) {
        final Long[] resultId = new Long[1];
        transactionTemplate.execute(status -> {
            try {
                if (postId == null) {
                    // 创建
                    if (isDraft) {
                        resultId[0] = postCommandService.createDraft(userId, request.getTitle(), 
                                request.getContent(), request.getDescription(), 
                                request.getCoverUrl(), request.getTagIds());
                    } else {
                        resultId[0] = postCommandService.publishPost(null, userId, request.getTitle(), 
                                request.getContent(), request.getDescription(), 
                                request.getCoverUrl(), request.getTagIds());
                    }
                } else {
                    // 更新
                    resultId[0] = postId;
                    if (isDraft) {
                        postCommandService.updateDraft(postId, userId, request.getTitle(), 
                                request.getContent(), request.getDescription(), 
                                request.getCoverUrl(), request.getTagIds());
                    } else {
                        postCommandService.publishPost(postId, userId, request.getTitle(), 
                                request.getContent(), request.getDescription(), 
                                request.getCoverUrl(), request.getTagIds());
                    }
                }
                return 1;
            } catch (BusinessException e) {
                status.setRollbackOnly();
                throw e;
            } catch (Exception e) {
                status.setRollbackOnly();
                log.warn("帖子操作失败: {}", e.getMessage());
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子操作失败");
            }
        });
        return resultId[0];
    }

    /**
     * 批量删除帖子
     * 
     * <p>后台批量删除帖子，软删除（修改状态）
     * <p>需要system:post:delete权限
     * 
     * @param postIds 帖子ID列表
     * @return 删除结果
     */
    @PostMapping("/delete")
    @Operation(summary = "删除帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:delete")
    @ApiOperationLog(description = "删除帖子")
    public ResponseEntity deletePosts(@Parameter(description = "帖子ID列表") @RequestBody List<Long> postIds) {
        try {
            // 批量删除帖子
            postCommandService.batchDelete(postIds);

            // 为每一个删除的帖子发布事件
            Long currentUserId = LoginUserUtil.getLoginUserId();
            for (Long postId : postIds) {
                contentEventPublisher.publishPostDeleted(currentUserId, postId);
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

    /**
     * 获取帖子列表
     * 
     * <p>分页获取帖子列表，支持按标题、状态、用户、标签筛选
     * <p>需要system:post:list权限
     * 
     * @param postRequest 查询请求，包含页码、筛选条件
     * @return 分页的帖子列表
     */
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
     * <p>获取帖子完整信息，包括内容和标签
     * <p>需要system:post:list权限
     *
     * @param id 帖子ID
     * @return 帖子详情，包含标签列表
     * @throws BusinessException 当帖子不存在时抛出
     */
    @GetMapping("info/{id}")
    @Operation(summary = "获取帖子详情")
    @SaCheckLogin
    @SaCheckPermission("system:post:list")
    @ApiOperationLog(description = "获取帖子详情")
    public ResponseEntity<SysPostDetailVO> getPost(@Parameter(description = "帖子ID") @PathVariable("id") Long id) {
        log.info("帖子详情获取参数: id={}", id);
        if (id == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子ID不能为空");
        }
        Post post = postQueryService.getById(id).orElse(null);
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

        return ResponseEntity.<SysPostDetailVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子获取成功")
                .data(SysPostDetailVO.builder()
                        .post(post)
                        .tagNames(tagNames)
                        .tagIds(tagIds)
                        .build())
                .build();
    }

    /**
     * 搜索帖子
     * 
     * <p>根据标题关键词搜索帖子，使用Elasticsearch
     * <p>公开接口，无需登录
     * 
     * @param title 搜索关键词
     * @return 搜索结果列表，最多返回20条
     */
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

    /**
     * 发布帖子
     * 
     * <p>后台发布新帖子，支持草稿或直接发布
     * <p>需要system:post:publish权限
     * 
     * @param publishPostRequest 发布请求，包含标题、内容、标签等
     * @return 发布结果
     * @throws BusinessException 当标签为空或超过5个时抛出
     */
    @PostMapping("/publish")
    @Operation(summary = "发布帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:publish")
    @ApiOperationLog(description = "发布帖子")
    public ResponseEntity pushPost(@RequestBody PublishPostRequest publishPostRequest) {
        log.info("发布帖子，帖子内容：{}", publishPostRequest);
        
        // 前置校验（发布时标签必填）
        validateTagIds(publishPostRequest.getTagIds(), true);
        
        Long userId = LoginUserUtil.getLoginUserId();
        boolean isDraft = "DRAFT".equals(publishPostRequest.getStatus());
        
        // 转换为 CreatePostRequest 复用逻辑
        CreatePostRequest request = new CreatePostRequest();
        request.setTitle(publishPostRequest.getTitle());
        request.setContent(publishPostRequest.getContent());
        request.setDescription(publishPostRequest.getDescription());
        request.setCoverUrl(publishPostRequest.getCoverUrl());
        request.setTagIds(publishPostRequest.getTagIds());
        request.setStatus(publishPostRequest.getStatus());
        
        Long postId = executePostOperation(userId, null, request, isDraft);
        
        // 发布帖子创建事件
        contentEventPublisher.publishPostCreated(userId, postId, publishPostRequest.getTitle());
        
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子发布成功")
                .build();
    }

    /**
     * 切换帖子加精状态
     * 
     * <p>加精或取消加精帖子
     * <p>需要system:post:update权限
     * 
     * @param postIds 帖子ID列表
     * @return 操作结果
     */
    @PostMapping("/featured")
    @Operation(summary = "切换加精状态")
    @SaCheckLogin
    @SaCheckPermission("system:post:update")
    @ApiOperationLog(description = "切换帖子加精状态")
    public ResponseEntity toggleFeatured(@RequestBody List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子ID不能为空");
        }
        for (Long postId : postIds) {
            postCommandService.toggleFeatured(postId);
        }
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("操作成功")
                .build();
    }

    /**
     * 切换帖子发布状态
     * 
     * <p>发布或下架帖子
     * <p>需要system:post:update权限
     * 
     * @param postIds 帖子ID列表
     * @return 操作结果
     */
    @PostMapping("/status")
    @Operation(summary = "切换发布状态")
    @SaCheckLogin
    @SaCheckPermission("system:post:update")
    @ApiOperationLog(description = "切换帖子发布状态")
    public ResponseEntity toggleStatus(@RequestBody List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子ID不能为空");
        }
        for (Long postId : postIds) {
            postCommandService.togglePublish(postId);
        }
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("操作成功")
                .build();
    }

    /**
     * 置顶/取消置顶帖子
     * 
     * <p>置顶或取消置顶帖子
     * <p>需要system:post:update权限
     * 
     * @param postIds 帖子ID列表
     * @return 操作结果
     */
    @PostMapping("/top")
    @Operation(summary = "置顶帖子")
    @SaCheckLogin
    @SaCheckPermission("system:post:update")
    @ApiOperationLog(description = "置顶帖子")
    public ResponseEntity toggleTop(@RequestBody List<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子ID不能为空");
        }
        // TODO: 实现置顶逻辑，需要在 PostCommandService 中添加 toggleTop 方法
        log.info("置顶帖子: {}", postIds);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("操作成功")
                .build();
    }

    /**
     * 获取随机封面图
     * 
     * <p>返回一个随机的默认封面图URL
     * <p>公开接口，无需登录
     * 
     * @return 随机封面图URL
     */
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