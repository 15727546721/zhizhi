package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.post.DraftRequest;
import cn.xu.api.web.model.dto.post.FindPostPageByCategoryRequest;
import cn.xu.api.web.model.dto.post.PostPageQueryRequest;
import cn.xu.api.web.model.dto.post.PublishOrDraftPostRequest;
import cn.xu.api.web.model.dto.report.ReportRequestDTO;
import cn.xu.api.web.model.vo.post.PostDetailResponse;
import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.api.web.model.vo.post.PostPageListResponse;
import cn.xu.api.web.model.vo.post.PostSearchResponse;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.domain.like.model.LikeType;
import cn.xu.domain.like.service.ILikeService;
import cn.xu.domain.post.model.aggregate.PostAggregate;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostContent;
import cn.xu.domain.post.model.valobj.PostStatus;
import cn.xu.domain.post.model.valobj.PostTitle;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.service.*;
import cn.xu.domain.report.model.entity.ReportEntity;
import cn.xu.domain.report.service.ReportDomainService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@RequestMapping("api/post")
@RestController
@Tag(name = "帖子接口", description = "帖子相关接口")
@Slf4j
public class PostController {

    @Resource
    private IPostService postService;
    @Resource
    private IPostTagService postTagService;
    @Resource
    private IPostTopicService postTopicService;
    @Resource
    private ITagService tagService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource
    private IUserService userService;
    @Resource
    private ILikeService likeService;
    @Resource
    private cn.xu.application.service.SearchApplicationService searchApplicationService;
    @Resource
    private IFollowService followService;
    @Resource
    private ReportDomainService reportDomainService;
    @Resource
    private PostValidationService postValidationService;

    /**
     * 将帖子实体列表转换为PostListResponse列表
     */
    private List<PostListResponse> convertToPostListResponses(List<PostEntity> posts) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 收集所有帖子的作者ID
        Set<Long> userIds = new HashSet<>();
        posts.forEach(post -> {
            if (post.getUserId() != null) {
                userIds.add(post.getUserId());
            }
        });
        
        // 批量获取用户信息
        List<UserEntity> users = Collections.emptyList();
        try {
            users = userService.batchGetUserInfo(new ArrayList<>(userIds));
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
        }
        
        // 构建用户ID到用户实体的映射
        java.util.Map<Long, UserEntity> userMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user, (existing, replacement) -> existing));
        
        return posts.stream()
                .map(post -> {
                    PostListResponse vo = new PostListResponse();
                    vo.setPost(post);
                    // 设置作者信息
                    if (post.getUserId() != null) {
                        vo.setUser(userMap.get(post.getUserId()));
                    }
                    return vo;
                })
                .collect(Collectors.toList());
    }


    @PostMapping("/page/category")
    @ApiOperationLog(description = "通过分类获取帖子列表")
    @Operation(summary = "通过分类获取帖子列表")
    public ResponseEntity<PageResponse<List<PostPageListResponse>>> getPostByCategoryId(@RequestBody FindPostPageByCategoryRequest request) {
        try {
            // 参数校验
            postValidationService.validatePageParams(request.getPageNo(), request.getPageSize());
            
            List<PostEntity> postList;
            long total;
            if (request.getCategoryId() == null) {
                postList = postService.getPostPageList(request.getPageNo(), request.getPageSize());
                total = postService.countAllPosts();
            } else {
                postList = postService.getPostPageListByCategoryId(request.getCategoryId(), request.getPageNo(), request.getPageSize());
                // TODO: 需要实现countPostsByCategoryId方法来获取准确的总数
                total = postList.size(); // 临时实现，实际应该获取该分类下的帖子总数
            }
            
            // 获取用户信息
            List<Long> userIds = postList.stream()
                    .map(PostEntity::getUserId)
                    .filter(Objects::nonNull)
                    .distinct()
                    .collect(Collectors.toList());
            
            List<UserEntity> userList = userService.batchGetUserInfo(userIds);
            java.util.Map<Long, UserEntity> userMap = userList.stream()
                    .collect(Collectors.toMap(UserEntity::getId, user -> user));
            
            // 转换为PostPageListResponse列表
            List<PostPageListResponse> result = postList.stream()
                    .map(post -> PostPageListResponse.builder()
                            .post(post)
                            .user(userMap.get(post.getUserId()))
                            .build())
                    .collect(Collectors.toList());
            
            // 创建分页响应对象
            PageResponse<List<PostPageListResponse>> pageResponse = PageResponse.ofList(
                request.getPageNo(),
                request.getPageSize(),
                total,
                result
            );

            return ResponseEntity.<PageResponse<List<PostPageListResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("通过分类获取帖子列表失败", e);
            return ResponseEntity.<PageResponse<List<PostPageListResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子列表失败")
                    .build();
        }
    }
    
    @PostMapping("/page")
    @ApiOperationLog(description = "分页获取帖子列表（支持排序）")
    @Operation(summary = "分页获取帖子列表（支持排序）")
    public ResponseEntity<PageResponse<List<PostPageListResponse>>> getPostByPage(@RequestBody PostPageQueryRequest request) {
        try {
            // 参数校验
            postValidationService.validatePageParams(request.getPageNo(), request.getPageSize());
            
            // 调用应用服务获取帖子列表
            List<PostPageListResponse> result = postService.getPostPageListWithSort(request);
            
            // 获取总记录数
            long total = postService.countAllPosts();
            
            // 创建分页响应对象
            PageResponse<List<PostPageListResponse>> pageResponse = PageResponse.ofList(
                request.getPageNo(),
                request.getPageSize(),
                total,
                result
            );

            return ResponseEntity.<PageResponse<List<PostPageListResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("分页获取帖子列表失败", e);
            return ResponseEntity.<PageResponse<List<PostPageListResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子列表失败")
                    .build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索帖子")
    @ApiOperationLog(description = "搜索帖子")
    public ResponseEntity<PageResponse<List<PostSearchResponse>>> searchPosts(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "帖子类型筛选（多个类型，用逗号分隔或重复参数）") @RequestParam(required = false) String[] types,
            @Parameter(description = "发布时间范围筛选（all/day/week/month/year）") @RequestParam(required = false, defaultValue = "all") String timeRange,
            @Parameter(description = "排序方式（time/hot/comment/like）") @RequestParam(required = false, defaultValue = "time") String sortOption,
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小，默认为10") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            // 构建筛选条件
            cn.xu.domain.search.model.valobj.SearchFilter.SearchFilterBuilder filterBuilder = 
                    cn.xu.domain.search.model.valobj.SearchFilter.builder();
            
            // 处理类型筛选
            if (types != null && types.length > 0) {
                List<cn.xu.domain.post.model.valobj.PostType> postTypes = java.util.Arrays.stream(types)
                        .filter(type -> type != null && !type.trim().isEmpty())
                        .flatMap(type -> {
                            // 支持逗号分隔的字符串，如 "ARTICLE,POST"
                            return java.util.Arrays.stream(type.split(","))
                                    .map(t -> t.trim().toUpperCase())
                                    .filter(t -> !t.isEmpty());
                        })
                        .map(type -> cn.xu.domain.post.model.valobj.PostType.fromCode(type))
                        .filter(type -> type != null)
                        .distinct()
                        .collect(java.util.stream.Collectors.toList());
                if (!postTypes.isEmpty()) {
                    filterBuilder.types(postTypes);
                }
            }
            
            // 处理时间范围筛选
            java.time.LocalDateTime now = java.time.LocalDateTime.now();
            java.time.LocalDateTime startTime = null;
            switch (timeRange != null ? timeRange.toLowerCase() : "all") {
                case "day":
                    startTime = now.minusDays(1);
                    break;
                case "week":
                    startTime = now.minusWeeks(1);
                    break;
                case "month":
                    startTime = now.minusMonths(1);
                    break;
                case "year":
                    startTime = now.minusYears(1);
                    break;
                default:
                    // "all" 或其他值，不设置时间范围
                    break;
            }
            if (startTime != null) {
                filterBuilder.startTime(startTime);
                filterBuilder.endTime(now);
            }
            
            // 处理排序方式
            cn.xu.domain.search.model.valobj.SearchFilter.SortOption sort = 
                    cn.xu.domain.search.model.valobj.SearchFilter.SortOption.TIME; // 默认
            if (sortOption != null) {
                switch (sortOption.toLowerCase()) {
                    case "hot":
                        sort = cn.xu.domain.search.model.valobj.SearchFilter.SortOption.HOT;
                        break;
                    case "comment":
                        sort = cn.xu.domain.search.model.valobj.SearchFilter.SortOption.COMMENT;
                        break;
                    case "like":
                        sort = cn.xu.domain.search.model.valobj.SearchFilter.SortOption.LIKE;
                        break;
                    default:
                        sort = cn.xu.domain.search.model.valobj.SearchFilter.SortOption.TIME;
                        break;
                }
            }
            filterBuilder.sortOption(sort);
            
            cn.xu.domain.search.model.valobj.SearchFilter filter = filterBuilder.build();
            
            // 调用搜索应用服务
            cn.xu.application.service.SearchApplicationService.SearchResult searchResult = 
                    searchApplicationService.executeSearch(keyword, filter, page, size);
            
            // 创建PageResponse对象
            PageResponse<List<PostSearchResponse>> pageResponse = PageResponse.ofList(
                searchResult.getPage(), 
                searchResult.getSize(), 
                searchResult.getTotal(), 
                searchResult.getPosts()
            );
            
            return ResponseEntity.<PageResponse<List<PostSearchResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("搜索参数错误: keyword={}, error={}", keyword, e.getMessage());
            return ResponseEntity.<PageResponse<List<PostSearchResponse>>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("搜索帖子失败: keyword={}", keyword, e);
            // 生产环境不返回详细错误信息，避免泄露系统内部信息
            // 详细错误信息已记录在日志中
            return ResponseEntity.<PageResponse<List<PostSearchResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 获取客户端真实IP地址
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果IP包含多个值（通过代理），取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    @GetMapping("/{id}")
    @Operation(summary = "获取帖子详情")
    @ApiOperationLog(description = "获取帖子详情")
    public ResponseEntity<?> getPostDetail(@Parameter(description = "帖子ID") @PathVariable("id") Long id,
                                           HttpServletRequest request) {
        log.info("获取帖子详情，帖子ID：{}", id);
        Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;

        // 增加浏览量（带防刷机制）
        // 获取客户端IP地址
        String clientIp = getClientIp(request);
        
        // 调用服务层增加浏览量，传入IP和用户ID用于防刷
        postService.viewPost(id, clientIp, currentUserId);

        // 调用服务层获取帖子详情
        PostDetailResponse postDetail = postService.getPostDetail(id, currentUserId);

        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子获取成功")
                .data(postDetail)
                .build();
    }

    @PostMapping("/create")
    @Operation(summary = "创建帖子")
    @ApiOperationLog(description = "创建帖子")
    public ResponseEntity<Long> createPost(@RequestBody PublishOrDraftPostRequest publishPostRequest) {
        // 参数校验
        postValidationService.validatePostPublishParams(
            publishPostRequest.getTitle(),
            publishPostRequest.getContent(),
            publishPostRequest.getCategoryId(),
            publishPostRequest.getType()
        );
        
        log.info("创建帖子，标题：{}，分类ID：{}，类型：{}，状态：{}", 
                publishPostRequest.getTitle(), publishPostRequest.getCategoryId(), 
                publishPostRequest.getType(), publishPostRequest.getStatus());

        Long userId = StpUtil.getLoginIdAsLong();
        AtomicLong postId = new AtomicLong();
        transactionTemplate.execute(status -> {
            // 事务开始
            try {
                // 根据请求中的状态字段确定帖子状态，默认为已发布
                String postStatus = "PUBLISHED";
                if ("DRAFT".equals(publishPostRequest.getStatus())) {
                    postStatus = "DRAFT";
                }
                
                // 创建帖子实体
                PostEntity.PostEntityBuilder postBuilder = PostEntity.builder()
                        .categoryId(publishPostRequest.getCategoryId())
                        .title(new PostTitle(publishPostRequest.getTitle()))
                        .coverUrl(publishPostRequest.getCoverUrl())
                        .content(new PostContent(publishPostRequest.getContent()))
                        .description(publishPostRequest.getDescription())
                        .userId(userId); // 当前登录用户ID
            
                // 设置帖子类型
                PostType postType = PostType.fromCode(publishPostRequest.getType());
                postBuilder.type(postType);
            
                // 如果是回答类型，检查是否有关联的问题ID
                if (PostType.ANSWER == postType && publishPostRequest.getAcceptedAnswerId() != null) {
                    postBuilder.acceptedAnswerId(Long.valueOf(publishPostRequest.getAcceptedAnswerId()));
                }
            
                // 设置状态
                postBuilder.status(PostStatus.PUBLISHED);
            
                //1. 保存帖子和分类id
                long id = postService.createPost(postBuilder.build());
                postId.set(id);
                
                //2. 保存帖子标签
                if (publishPostRequest.getTagIds() != null && !publishPostRequest.getTagIds().isEmpty()) {
                    if (publishPostRequest.getTagIds().size() > 3) {
                        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                    }
                    postTagService.savePostTags(postId.get(), publishPostRequest.getTagIds());
                }
                
                //3. 保存帖子话题
                if (publishPostRequest.getTopicIds() != null && !publishPostRequest.getTopicIds().isEmpty()) {
                    if (publishPostRequest.getTopicIds().size() > 10) {
                        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "话题不能超过10个");
                    }
                    postTopicService.savePostTopics(postId.get(), publishPostRequest.getTopicIds());
                }
                
                // 事务提交
                return 1;
            } catch (Exception e) {
                // 事务回滚
                status.setRollbackOnly();
                log.error("帖子发布失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子发布失败");
            }
        });
        
        log.info("帖子创建成功，ID：{}", postId.longValue());
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(postId.longValue())
                .info("帖子创建成功")
                .build();
    }

    @PostMapping("/publish")
    @Operation(summary = "发布帖子")
    @SaCheckLogin
    @ApiOperationLog(description = "发布帖子")
    public ResponseEntity pushPost(@RequestBody PublishOrDraftPostRequest publishPostRequest) {
        log.info("发布帖子，帖子ID：{}，标题：{}，分类ID：{}，类型：{}，状态：{}", 
                publishPostRequest.getId(), publishPostRequest.getTitle(), 
                publishPostRequest.getCategoryId(), publishPostRequest.getType(), 
                publishPostRequest.getStatus());
        
        if (publishPostRequest == null || publishPostRequest.getId() == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子ID不能为空");
        }
        
        // 参数校验
        postValidationService.validatePostPublishParams(
            publishPostRequest.getTitle(),
            publishPostRequest.getContent(),
            publishPostRequest.getCategoryId(),
            publishPostRequest.getType()
        );
        
        Long userId = StpUtil.getLoginIdAsLong();
        transactionTemplate.execute(status -> {
            // 事务开始
            try {
                Long postId = publishPostRequest.getId();
                // 根据请求中的状态字段确定帖子状态，默认为已发布
                PostStatus postStatus = PostStatus.PUBLISHED;
                if ("DRAFT".equals(publishPostRequest.getStatus())) {
                    postStatus = PostStatus.DRAFT;
                }
                
                //1. 保存帖子和分类id
                postService.publishPost(PostEntity.builder()
                        .id(publishPostRequest.getId())
                        .categoryId(publishPostRequest.getCategoryId())
                        .title(new PostTitle(publishPostRequest.getTitle()))
                        .coverUrl(publishPostRequest.getCoverUrl())
                        .content(new PostContent(publishPostRequest.getContent()))
                        .description(publishPostRequest.getDescription())
                        .userId(userId) // 当前登录用户ID
                        .type(PostType.fromCode(publishPostRequest.getType()))
                        .status(postStatus)
                        .build(), userId);
                //2. 保存帖子标签
                if (publishPostRequest.getTagIds() != null && !publishPostRequest.getTagIds().isEmpty()) {
                    if (publishPostRequest.getTagIds().size() > 3) {
                        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "标签不能超过3个");
                    }
                    postTagService.savePostTags(postId, publishPostRequest.getTagIds());
                }
                //3. 保存帖子话题
                if (publishPostRequest.getTopicIds() != null && !publishPostRequest.getTopicIds().isEmpty()) {
                    if (publishPostRequest.getTopicIds().size() > 10) {
                        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "话题不能超过10个");
                    }
                    postTopicService.savePostTopics(postId, publishPostRequest.getTopicIds());
                }
                // 事务提交
                return 1;
            } catch (Exception e) {
                // 事务回滚
                status.setRollbackOnly();
                log.error("帖子发布失败", e);
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "帖子发布失败");
            }
        });
        log.info("帖子发布成功，ID：{}", publishPostRequest.getId());
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("帖子发布成功")
                .build();
    }

    @PostMapping("/saveDraft")
    @Operation(summary = "保存帖子草稿")
    @SaCheckLogin
    @ApiOperationLog(description = "保存帖子草稿")
    public ResponseEntity<Long> saveDraft(@RequestBody DraftRequest draftRequest) {
        log.info("保存帖子草稿，标题：{}，分类ID：{}，类型：{}", 
                draftRequest.getTitle(), draftRequest.getCategoryId(), draftRequest.getType());
        Long userId = StpUtil.getLoginIdAsLong();

        try {
            PostEntity postEntity = PostEntity.createDraft(
                    userId,
                    draftRequest.getTitle(),
                    draftRequest.getContent(),
                    draftRequest.getDescription(),
                    draftRequest.getCategoryId(),
                    PostType.fromCode(draftRequest.getType())
            );

            Long postId = postService.createOrUpdatePostDraft(postEntity);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(postId)
                    .info("草稿保存成功")
                    .build();
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("保存草稿失败")
                    .build();
        }
    }

    @GetMapping("/share/{id}")
    @Operation(summary = "增加分享数")
    @ApiOperationLog(description = "增加帖子分享数")
    public ResponseEntity sharePost(@PathVariable Long id) {
        try {
            // 验证帖子是否存在
            Optional<PostEntity> postOpt = postService.findPostEntityById(id);
            if (!postOpt.isPresent()) {
                return ResponseEntity.builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("帖子不存在")
                        .build();
            }
            
            // 增加分享数
            postService.increasePostShareCount(id);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("分享成功")
                    .build();
        } catch (Exception e) {
            log.error("分享帖子失败，postId={}", id, e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("分享失败")
                    .build();
        }
    }
    
    @GetMapping("/view")
    @Operation(summary = "增加帖子浏览量")
    @ApiOperationLog(description = "增加帖子浏览量")
    public ResponseEntity<?> viewPost(@Parameter(description = "帖子ID") @RequestParam Long postId) {
        try {
            postService.viewPost(postId, null, null);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("帖子阅读数+1成功")
                    .build();
        } catch (Exception e) {
            log.error("增加帖子浏览量失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("增加帖子浏览量失败")
                    .build();
        }
    }

    @GetMapping("/like/{id}")
    @Operation(summary = "帖子点赞")
    @ApiOperationLog(description = "帖子点赞")
    public ResponseEntity<?> likePost(@Parameter(description = "帖子ID") @PathVariable("id") Long id) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            likeService.like(userId, LikeType.POST, id);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("帖子点赞成功")
                    .build();
        } catch (BusinessException e) {
            // 对于业务异常，检查是否是数据不一致导致的问题
            if (e.getMessage().contains("请勿重复操作")) {
                // 触发数据一致性检查和修复
                likeService.checkAndRepairLikeConsistency(userId, id, LikeType.POST);
            }
            throw e;
        }
    }

    @GetMapping("/unlike/{id}")
    @Operation(summary = "取消帖子点赞")
    @ApiOperationLog(description = "取消帖子点赞")
    public ResponseEntity<?> unlikePost(@Parameter(description = "帖子ID") @PathVariable("id") Long postId) {
        Long userId = StpUtil.getLoginIdAsLong();
        try {
            likeService.unlike(userId, LikeType.POST, postId);
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("帖子取消点赞成功")
                    .build();
        } catch (BusinessException e) {
            // 对于业务异常，检查是否是数据不一致导致的问题
            if (e.getMessage().contains("尚未点赞")) {
                // 触发数据一致性检查和修复
                likeService.checkAndRepairLikeConsistency(userId, postId, LikeType.POST);
            }
            throw e;
        }
    }
    
    @PostMapping("/report")
    @SaCheckLogin
    @Operation(summary = "举报帖子")
    @ApiOperationLog(description = "举报帖子")
    public ResponseEntity<Long> reportPost(@RequestBody ReportRequestDTO requestDTO) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 创建举报
            ReportEntity report = reportDomainService.createReport(
                    userId,
                    ReportEntity.ReportType.ARTICLE.getCode(), // 帖子类型
                    requestDTO.getTargetId(), // 帖子ID
                    requestDTO.getReason(),
                    requestDTO.getDetail()
            );
            
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(report.getId())
                    .info("举报成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("举报帖子失败: {}", e.getMessage());
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("举报帖子异常", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("举报失败，请稍后重试")
                    .build();
        }
    }
    

    
    @PostMapping("/accept-answer")
    @Operation(summary = "采纳回答（仅限问答帖）")
    @SaCheckLogin
    @ApiOperationLog(description = "采纳回答（仅限问答帖）")
    public ResponseEntity<?> acceptAnswer(@RequestBody Map<String, Object> request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            Long postId = Long.valueOf(request.get("postId").toString());
            Long answerId = Long.valueOf(request.get("answerId").toString());
            
            // 调用服务层采纳回答
            postService.acceptAnswer(postId, answerId, userId);
            
            return ResponseEntity.builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("已采纳最佳答案")
                    .build();
        } catch (Exception e) {
            log.error("采纳回答失败", e);
            return ResponseEntity.builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("采纳回答失败，请稍后重试")
                    .build();
        }
    }

    @GetMapping("/answers/{questionId}")
    @Operation(summary = "获取问答帖的所有回答")
    @ApiOperationLog(description = "获取问答帖的所有回答")
    public ResponseEntity<List<PostEntity>> getAnswersByQuestionId(
            @Parameter(description = "问题帖子ID") @PathVariable("questionId") Long questionId,
            @Parameter(description = "页码，默认为0") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "每页数量，默认为10") @RequestParam(defaultValue = "10") int size) {
        try {
            // 验证问题帖子是否存在且为问答帖
            Optional<PostAggregate> questionAggregateOpt = postService.findById(questionId);
            if (!questionAggregateOpt.isPresent()) {
                return ResponseEntity.<List<PostEntity>>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("问题帖子不存在")
                        .build();
            }
            
            PostAggregate questionAggregate = questionAggregateOpt.get();
            if (!questionAggregate.isQuestion()) {
                return ResponseEntity.<List<PostEntity>>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("指定的帖子不是问答帖")
                        .build();
            }
            
            // 查询所有回答帖（类型为POST且acceptedAnswerId等于问题ID）
            Pageable pageable = PageRequest.of(page, size);
            List<PostEntity> answers = postService.findAnswersByQuestionId(questionId, page, size);
            
            return ResponseEntity.<List<PostEntity>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(answers)
                    .info("获取回答列表成功")
                    .build();
        } catch (Exception e) {
            log.error("获取问答帖回答列表失败: {}", e.getMessage(), e);
            return ResponseEntity.<List<PostEntity>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取回答列表失败")
                    .build();
        }
    }
}