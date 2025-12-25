package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.post.DraftRequest;
import cn.xu.model.dto.post.PostPageQueryRequest;
import cn.xu.model.dto.post.PostTagRelation;
import cn.xu.model.dto.post.PublishOrDraftPostRequest;
import cn.xu.model.dto.search.SearchFilter;
import cn.xu.model.entity.Like;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.User;
import cn.xu.model.vo.post.PostDetailVO;
import cn.xu.model.vo.post.PostItemVO;
import cn.xu.model.vo.post.PostListVO;
import cn.xu.model.vo.post.PostSearchResponseVO;
import cn.xu.model.vo.user.UserVO;
import cn.xu.service.favorite.FavoriteService;
import cn.xu.service.follow.FollowService;
import cn.xu.service.like.LikeService;
import cn.xu.service.post.PostService;
import cn.xu.service.post.PostValidationService;
import cn.xu.service.post.TagService;
import cn.xu.service.user.UserService;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.LoginUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 帖子控制器
 * <p>
 * 职责：帖子内容管理、交互操作
 * <ul>
 *   <li>内容：创建、发布、编辑、删除、草稿</li>
 *   <li>查询：列表、详情、搜索、排行榜</li>
 *   <li>交互：点赞、收藏、分享、浏览</li>
 * </ul>
 *
 */
@RequestMapping("/api/post")
@RestController
@Tag(name = "帖子接口", description = "帖子相关API")
@Slf4j
public class PostController {

    @Resource(name = "postService")
    private PostService postService;
    @Resource(name = "tagService")
    private TagService tagService;
    @Resource
    private TransactionTemplate transactionTemplate;
    @Resource(name = "userService")
    private UserService userService;
    @Resource
    private LikeService likeService;
    @Resource
    private cn.xu.service.search.PostSearchService postSearchService;
    @Resource
    private FollowService followService;
    @Resource
    private PostValidationService postValidationService;
    @Resource
    private FavoriteService favoriteService;

    /**
     * Post列表转换为PostListVO（批量获取用户和标签信息）
     */
    private List<PostListVO> convertToPostListVOs(List<Post> posts) {
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
        // 【优雅降级】获取用户信息失败时，不影响帖子列表展示，只是缺少用户头像/昵称
        Map<Long, User> tempUserMap;
        try {
            tempUserMap = userService.batchGetUserInfo(new ArrayList<>(userIds));
        } catch (Exception e) {
            log.error("【降级】批量获取用户信息失败，帖子将不显示作者信息 - userIds: {}", userIds, e);
            tempUserMap = new HashMap<>();
        }
        final Map<Long, User> userMap = tempUserMap;

        // 批量获取帖子的标签信息
        List<Long> postIds = posts.stream()
                .map(Post::getId)
                .collect(Collectors.toList());

        java.util.Map<Long, List<String>> postTagMap = new java.util.HashMap<>();
        if (!postIds.isEmpty()) {
            try {
                // 批量获取帖子标签关系
                List<PostTagRelation> tagRelations = tagService.batchGetTagIdsByPostIds(postIds);

                // 收集所有标签ID
                Set<Long> allTagIds = new java.util.HashSet<>();
                tagRelations.forEach(relation -> {
                    if (relation.getTagIds() != null) {
                        allTagIds.addAll(relation.getTagIds());
                    }
                });

                // 批量获取标签信息（优化：使用批量查询代替全表扫描）
                java.util.Map<Long, String> tagMap = new java.util.HashMap<>();
                if (!allTagIds.isEmpty()) {
                    java.util.Map<Long, cn.xu.model.entity.Tag> tagObjMap = tagService.batchGetTags(allTagIds);
                    tagObjMap.forEach((id, tag) -> tagMap.put(id, tag.getName()));
                }

                // 构建帖子ID到标签名称列表的映射
                tagRelations.forEach(relation -> {
                    if (relation.getTagIds() != null) {
                        List<String> tagNames = relation.getTagIds().stream()
                                .map(tagId -> tagMap.getOrDefault(tagId, ""))
                                .filter(name -> name != null && !name.isEmpty())
                                .collect(Collectors.toList());
                        if (!tagNames.isEmpty()) {
                            postTagMap.put(relation.getPostId(), tagNames);
                        }
                    }
                });
            } catch (Exception e) {
                // 【优雅降级】获取标签失败时，帖子仍可展示，只是缺少标签信息
                log.error("【降级】批量获取帖子标签失败，帖子将不显示标签 - postIds: {}", postIds, e);
            }
        }

        // 构建最终的标签名称映射
        final java.util.Map<Long, String[]> finalTagMap = postTagMap.entrySet().stream()
                .collect(Collectors.toMap(
                        java.util.Map.Entry::getKey,
                        entry -> entry.getValue().toArray(new String[0])
                ));

        return posts.stream()
                .map(post -> {
                    // 获取用户信息
                    User user = post.getUserId() != null ? userMap.get(post.getUserId()) : null;

                    // 获取标签名称列表
                    String[] tagNameList = finalTagMap.getOrDefault(post.getId(), new String[]{});

                    // 构建扁平化的PostItemVO
                    PostItemVO postItem = PostItemVO.builder()
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
                            .tagNameList(tagNameList)
                            .build();

                    // 构建PostListVO
                    return PostListVO.builder()
                            .postItem(postItem)
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * 分页获取帖子列表（支持多种排序，公开接口）
     */
    @PostMapping("/page")
    @ApiOperationLog(description = "分页获取帖子列表（支持排序）")
    @Operation(summary = "分页获取帖子列表（支持排序）")
    public ResponseEntity<PageResponse<List<PostListVO>>> getPostByPage(@RequestBody PostPageQueryRequest request) {
        try {
            // 参数校验
            postValidationService.validatePageParams(request.getPageNo(), request.getPageSize());

            // 调用应用服务获取帖子列表
            List<Post> posts = postService.getAllPosts(request.getPageNo(), request.getPageSize());

            // 获取总记录数
            long total = postService.countAllPosts();

            // 转换为PostListVO
            List<PostListVO> result = convertToPostListVOs(posts);

            // 创建分页响应对象
            PageResponse<List<PostListVO>> pageResponse = PageResponse.ofList(
                    request.getPageNo(),
                    request.getPageSize(),
                    total,
                    result
            );

            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("分页获取帖子列表失败", e);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子列表失败")
                    .build();
        }
    }

    /**
     * 搜索帖子（支持时间范围、排序方式筛选）
     */
    @GetMapping("/search")
    @Operation(summary = "搜索帖子")
    @ApiOperationLog(description = "搜索帖子")
    public ResponseEntity<PageResponse<List<PostSearchResponseVO>>> searchPosts(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "帖子类型筛选（多个类型，用逗号分隔或重复参数）") @RequestParam(required = false) String[] types,
            @Parameter(description = "发布时间范围筛选（all/day/week/month/year）") @RequestParam(required = false, defaultValue = "all") String timeRange,
            @Parameter(description = "排序方式（time/hot/comment/like）") @RequestParam(required = false, defaultValue = "time") String sortOption,
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小，默认为10") @RequestParam(defaultValue = "10") int size,
            HttpServletRequest request) {
        try {
            // 构建筛选条件
            SearchFilter.SearchFilterBuilder filterBuilder =
                    SearchFilter.builder();

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
                    // "all" 或其它值，不设置时间范围
                    break;
            }
            if (startTime != null) {
                filterBuilder.startTime(startTime);
                filterBuilder.endTime(now);
            }

            // 处理排序方式
            SearchFilter.SortOption sort =
                    SearchFilter.SortOption.TIME; // 默认
            if (sortOption != null) {
                switch (sortOption.toLowerCase()) {
                    case "hot":
                        sort = SearchFilter.SortOption.HOT;
                        break;
                    case "comment":
                        sort = SearchFilter.SortOption.COMMENT;
                        break;
                    case "like":
                        sort = SearchFilter.SortOption.LIKE;
                        break;
                    default:
                        sort = SearchFilter.SortOption.TIME;
                        break;
                }
            }
            filterBuilder.sortOption(sort);

            SearchFilter filter = filterBuilder.build();

            // 调用搜索应用服务
            cn.xu.service.search.PostSearchService.SearchResult searchResult =
                    postSearchService.executeSearch(keyword, filter, page, size);

            // 创建PageResponse对象
            PageResponse<List<PostSearchResponseVO>> pageResponse = PageResponse.ofList(
                    searchResult.getPage(),
                    searchResult.getSize(),
                    searchResult.getTotal(),
                    searchResult.getPosts()
            );

            return ResponseEntity.<PageResponse<List<PostSearchResponseVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (IllegalArgumentException e) {
            log.warn("搜索参数错误: keyword={}, error={}", keyword, e.getMessage());
            return ResponseEntity.<PageResponse<List<PostSearchResponseVO>>>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("搜索帖子失败: keyword={}", keyword, e);
            // 生产环境中不返回详细错误信息，避免泄露系统内部信息
            // 详细错误信息已被记录在日志中
            return ResponseEntity.<PageResponse<List<PostSearchResponseVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 获取客户端IP
     */
    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 如果IP包含多个值（通过代理），只取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    /**
     * 获取帖子详情（含作者、标签、统计、交互状态）
     */
    @GetMapping("/{id}")
    @Operation(summary = "获取帖子详情", description = "返回帖子完整信息，包括内容、作者、标签、统计数据和用户交互状态")
    @ApiOperationLog(description = "获取帖子详情")
    public ResponseEntity<PostDetailVO> getPostDetail(
            @Parameter(description = "帖子ID") @PathVariable("id") Long id,
            HttpServletRequest request) {

        log.debug("获取帖子详情: postId={}", id);

        try {
            // 1. 获取当前用户ID（可能未登录）
            Long currentUserId = LoginUserUtil.getLoginUserIdOptional().orElse(null);

            // 2. 增加浏览量（带防刷机制）
            String clientIp = getClientIp(request);
            postService.viewPost(id, currentUserId, clientIp);

            // 3. 获取帖子基本信息
            Optional<Post> postOpt = postService.getPostById(id);
            if (!postOpt.isPresent()) {
                return ResponseEntity.<PostDetailVO>builder()
                        .code(ResponseCode.UN_ERROR.getCode())
                        .info("帖子不存在")
                        .build();
            }

            Post post = postOpt.get();

            // 4. 权限校验：只有已发布的帖子或自己的草稿才可以查看
            if (post.getStatus() != Post.STATUS_PUBLISHED) {
                if (currentUserId == null || !currentUserId.equals(post.getUserId())) {
                    return ResponseEntity.<PostDetailVO>builder()
                            .code(ResponseCode.UN_ERROR.getCode())
                            .info("无权限查看该帖子")
                            .build();
                }
            }

            // 5. 构建详情响应
            PostDetailVO detail = buildPostDetailVO(post, currentUserId);

            return ResponseEntity.<PostDetailVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(detail)
                    .build();

        } catch (Exception e) {
            log.error("获取帖子详情失败: postId={}", id, e);
            return ResponseEntity.<PostDetailVO>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子详情失败，请稍后重试")
                    .build();
        }
    }

    /**
     * 创建帖子（支持直接发布或保存为草稿）
     */
    @PostMapping("/create")
    @Operation(summary = "创建帖子")
    @ApiOperationLog(description = "创建帖子")
    public ResponseEntity<Long> createPost(@RequestBody PublishOrDraftPostRequest request) {
        try {
            // 参数校验
            postValidationService.validatePostPublishParams(
                    request.getTitle(),
                    request.getContent()
            );
            postValidationService.validateTagIds(request.getTagIds());

            log.info("创建帖子，标题：{}，状态：{}",
                    request.getTitle(), request.getStatus());

            Long userId = LoginUserUtil.getLoginUserId();
            Long postId;

            // 根据状态决定是创建草稿还是直接发布
            if ("DRAFT".equals(request.getStatus())) {
                // 创建草稿
                postId = postService.createDraft(
                        userId,
                        request.getTitle(),
                        request.getContent(),
                        request.getDescription(),
                        request.getCoverUrl(),
                        request.getTagIds()
                );
                log.info("草稿创建成功，ID:{}", postId);
                return ResponseEntity.<Long>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .data(postId)
                        .info("草稿保存成功")
                        .build();
            } else {
                // 直接发布
                postId = postService.publishPost(
                        null, // postId为null表示创建新帖子
                        userId,
                        request.getTitle(),
                        request.getContent(),
                        request.getDescription(),
                        request.getCoverUrl(),
                        request.getTagIds()
                );
                log.info("帖子创建并发布成功，ID:{}", postId);
                return ResponseEntity.<Long>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .data(postId)
                        .info("帖子发布成功")
                        .build();
            }
        } catch (BusinessException e) {
            log.error("创建帖子失败: {}", e.getMessage());
            return ResponseEntity.<Long>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("创建帖子异常", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("帖子创建失败")
                    .build();
        }
    }

    /**
     * 发布/更新帖子（草稿发布或已发布帖子更新）
     */
    @PostMapping("/publish")
    @Operation(summary = "发布帖子（更新帖子）")
    @SaCheckLogin
    @ApiOperationLog(description = "发布帖子（更新帖子）")
    public ResponseEntity<Long> publishPost(@RequestBody PublishOrDraftPostRequest request) {
        try {
            log.info("发布帖子，帖子ID:{}，标题：{}，状态：{}",
                    request.getId(), request.getTitle(), request.getStatus());

            // 参数校验
            postValidationService.validatePostPublishParams(
                    request.getTitle(),
                    request.getContent()
            );
            postValidationService.validateTagIds(request.getTagIds());

            Long userId = LoginUserUtil.getLoginUserId();

            // 根据状态决定是更新草稿还是发布
            if ("DRAFT".equals(request.getStatus())) {
                // 更新草稿
                if (request.getId() == null) {
                    throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "帖子ID不能为空");
                }
                postService.updateDraft(
                        request.getId(),
                        userId,
                        request.getTitle(),
                        request.getContent(),
                        request.getDescription(),
                        request.getCoverUrl(),
                        request.getTagIds()
                );
                log.info("草稿更新成功，ID:{}", request.getId());
                return ResponseEntity.<Long>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .data(request.getId())
                        .info("草稿保存成功")
                        .build();
            } else {
                // 发布帖子（可能是新建也可能是从草稿发布）
                Long postId = postService.publishPost(
                        request.getId(), // 如果ID为null则创建新帖子
                        userId,
                        request.getTitle(),
                        request.getContent(),
                        request.getDescription(),
                        request.getCoverUrl(),
                        request.getTagIds()
                );
                log.info("帖子发布成功，ID:{}", postId);
                return ResponseEntity.<Long>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .data(postId)
                        .info("帖子发布成功")
                        .build();
            }
        } catch (BusinessException e) {
            log.error("发布帖子失败: {}", e.getMessage());
            return ResponseEntity.<Long>builder()
                    .code(e.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("发布帖子异常", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("帖子发布失败")
                    .build();
        }
    }

    /**
     * 保存帖子草稿（支持新建和更新）
     */
    @PostMapping("/saveDraft")
    @Operation(summary = "保存帖子草稿")
    @SaCheckLogin
    @ApiOperationLog(description = "保存帖子草稿")
    public ResponseEntity<Long> saveDraft(@RequestBody DraftRequest draftRequest) {
        Long userId = LoginUserUtil.getLoginUserId();
        try {
            Long postId;
            if (draftRequest.getId() != null) {
                postService.updateDraft(draftRequest.getId(), userId, draftRequest.getTitle(),
                        draftRequest.getContent(), draftRequest.getDescription(),
                        draftRequest.getCoverUrl(), draftRequest.getTagIds());
                postId = draftRequest.getId();
            } else {
                postId = postService.createDraft(userId, draftRequest.getTitle(),
                        draftRequest.getContent(), draftRequest.getDescription(),
                        draftRequest.getCoverUrl(), draftRequest.getTagIds());
            }
            return ResponseEntity.<Long>builder().code(ResponseCode.SUCCESS.getCode())
                    .data(postId).info("草稿保存成功").build();
        } catch (Exception e) {
            log.error("保存草稿失败", e);
            return ResponseEntity.<Long>builder().code(ResponseCode.UN_ERROR.getCode())
                    .info("保存草稿失败").build();
        }
    }

    /**
     * 获取我的草稿列表
     */
    @PostMapping("/drafts")
    @Operation(summary = "获取我的草稿列表")
    @SaCheckLogin
    @ApiOperationLog(description = "获取我的草稿列表")
    public ResponseEntity<PageResponse<List<PostListVO>>> getMyDrafts(@RequestBody Map<String, Object> request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            Integer pageNo = request.get("pageNo") != null ? Integer.valueOf(request.get("pageNo").toString()) : 1;
            Integer pageSize = request.get("pageSize") != null ? Integer.valueOf(request.get("pageSize").toString()) : 10;
            postValidationService.validatePageParams(pageNo, pageSize);
            List<Post> drafts = postService.getUserPostsByStatus(userId, Post.STATUS_DRAFT, pageNo, pageSize);
            long total = postService.countUserDrafts(userId);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(PageResponse.ofList(pageNo, pageSize, total, convertToPostListVOs(drafts)))
                    .build();
        } catch (Exception e) {
            log.error("获取草稿列表失败", e);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode()).info("获取草稿列表失败").build();
        }
    }

    /**
     * 删除草稿（仅能删除自己的草稿）
     */
    @DeleteMapping("/draft/{id}")
    @Operation(summary = "删除草稿")
    @SaCheckLogin
    @ApiOperationLog(description = "删除草稿")
    public ResponseEntity<?> deleteDraft(@PathVariable Long id) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            Optional<Post> postOpt = postService.getPostById(id);
            if (!postOpt.isPresent()) {
                return ResponseEntity.builder().code(ResponseCode.UN_ERROR.getCode()).info("草稿不存在").build();
            }
            Post post = postOpt.get();
            if (!userId.equals(post.getUserId())) {
                return ResponseEntity.builder().code(ResponseCode.UN_ERROR.getCode()).info("无权删除此草稿").build();
            }
            if (post.getStatus() != Post.STATUS_DRAFT) {
                return ResponseEntity.builder().code(ResponseCode.UN_ERROR.getCode()).info("只能删除草稿状态的帖子").build();
            }
            postService.deletePost(id, userId, false);
            return ResponseEntity.builder().code(ResponseCode.SUCCESS.getCode()).info("草稿删除成功").build();
        } catch (Exception e) {
            log.error("删除草稿失败，id={}", id, e);
            return ResponseEntity.builder().code(ResponseCode.UN_ERROR.getCode()).info("删除草稿失败").build();
        }
    }

    /**
     * 分享帖子（增加分享数）
     */
    @GetMapping("/share/{id}")
    @Operation(summary = "增加分享数")
    @ApiOperationLog(description = "增加帖子分享数")
    public ResponseEntity sharePost(@PathVariable Long id) {
        try {
            Optional<Post> postOpt = postService.getPostById(id);
            if (!postOpt.isPresent()) {
                return ResponseEntity.builder().code(ResponseCode.UN_ERROR.getCode()).info("帖子不存在").build();
            }
            postService.increaseShareCount(id);
            return ResponseEntity.builder().code(ResponseCode.SUCCESS.getCode()).info("分享成功").build();
        } catch (Exception e) {
            log.error("分享帖子失败，postId={}", id, e);
            return ResponseEntity.builder().code(ResponseCode.UN_ERROR.getCode()).info("分享失败").build();
        }
    }

    /**
     * 增加帖子浏览数
     */
    @GetMapping("/view")
    @Operation(summary = "增加帖子浏览数")
    @ApiOperationLog(description = "增加帖子浏览数")
    public ResponseEntity<?> viewPost(@Parameter(description = "帖子ID") @RequestParam Long postId) {
        try {
            postService.viewPost(postId, null, null);
            return ResponseEntity.builder().code(ResponseCode.SUCCESS.getCode()).info("帖子阅读数+1成功").build();
        } catch (Exception e) {
            log.error("增加帖子浏览量失败", e);
            return ResponseEntity.builder().code(ResponseCode.UN_ERROR.getCode()).info("增加帖子浏览量失败").build();
        }
    }

    @GetMapping("/like/{id}")
    @Operation(summary = "帖子点赞")
    @ApiOperationLog(description = "帖子点赞")
    public ResponseEntity<?> likePost(@PathVariable("id") Long id) {
        Long userId = LoginUserUtil.getLoginUserId();
        likeService.like(userId, Like.LikeType.POST.getCode(), id);
        return ResponseEntity.builder().code(ResponseCode.SUCCESS.getCode()).info("帖子点赞成功").build();
    }

    @GetMapping("/unlike/{id}")
    @Operation(summary = "取消帖子点赞")
    @ApiOperationLog(description = "取消帖子点赞")
    public ResponseEntity<?> unlikePost(@PathVariable("id") Long postId) {
        Long userId = LoginUserUtil.getLoginUserId();
        likeService.unlike(userId, Like.LikeType.POST.getCode(), postId);
        return ResponseEntity.builder().code(ResponseCode.SUCCESS.getCode()).info("帖子取消点赞成功").build();
    }

    /**
     * 获取我的帖子列表
     * 
     * @param request 请求参数：pageNo, pageSize, status, keyword(可选)
     */
    @PostMapping("/my")
    @Operation(summary = "获取我的帖子列表")
    @SaCheckLogin
    @ApiOperationLog(description = "获取我的帖子列表")
    public ResponseEntity<PageResponse<List<PostListVO>>> getMyPosts(@RequestBody Map<String, Object> request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            Integer pageNo = request.get("pageNo") != null ? Integer.valueOf(request.get("pageNo").toString()) : 1;
            Integer pageSize = request.get("pageSize") != null ? Integer.valueOf(request.get("pageSize").toString()) : 10;
            String status = request.get("status") != null ? request.get("status").toString() : null;
            String keyword = request.get("keyword") != null ? request.get("keyword").toString().trim() : null;
            
            postValidationService.validatePageParams(pageNo, pageSize);
            
            // 根据状态确定查询条件：null=全部, PUBLISHED=已发布(1), DRAFT=草稿(0)
            Integer statusCode = null;
            if ("PUBLISHED".equals(status)) {
                statusCode = 1;
            } else if ("DRAFT".equals(status)) {
                statusCode = 0;
            }
            
            List<Post> posts = postService.getUserPostsWithKeyword(userId, statusCode, keyword, pageNo, pageSize);
            long total = postService.countUserPostsWithKeyword(userId, statusCode, keyword);
            
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(PageResponse.ofList(pageNo, pageSize, total, convertToPostListVOs(posts)))
                    .build();
        } catch (Exception e) {
            log.error("获取我的帖子列表失败", e);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode()).info("获取帖子列表失败").build();
        }
    }

    /**
     * 获取指定用户的帖子列表（公开接口）
     */
    @PostMapping("/user/{userId}")
    @Operation(summary = "获取指定用户的帖子列表")
    @ApiOperationLog(description = "获取指定用户的帖子列表")
    public ResponseEntity<PageResponse<List<PostListVO>>> getUserPosts(@PathVariable Long userId, @RequestBody Map<String, Object> request) {
        try {
            Integer pageNo = request.get("pageNo") != null ? Integer.valueOf(request.get("pageNo").toString()) : 1;
            Integer pageSize = request.get("pageSize") != null ? Integer.valueOf(request.get("pageSize").toString()) : 10;
            String status = request.get("status") != null ? request.get("status").toString() : "PUBLISHED";
            if (userId == null || userId <= 0) {
                return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode()).info("用户ID不能为空").build();
            }
            postValidationService.validatePageParams(pageNo, pageSize);
            List<Post> posts = postService.getUserPostsByStatus(userId, "PUBLISHED".equals(status) ? 1 : 0, pageNo, pageSize);
            long total = "PUBLISHED".equals(status) ? postService.countUserPublishedPosts(userId) : postService.countUserDrafts(userId);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(PageResponse.ofList(pageNo, pageSize, total, convertToPostListVOs(posts)))
                    .build();
        } catch (Exception e) {
            log.error("获取用户帖子列表失败，userId: {}", userId, e);
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode()).info("获取帖子列表失败").build();
        }
    }

    /**
     * 获取收藏排行榜
     */
    @GetMapping("/favorite/ranking")
    @Operation(summary = "获取收藏排行榜")
    @ApiOperationLog(description = "获取收藏排行榜")
    public ResponseEntity<List<PostListVO>> getFavoriteRanking(@RequestParam(defaultValue = "10") Integer limit) {
        try {
            int safeLimit = Math.max(1, Math.min(limit, 100));
            List<Post> posts = postService.getPostsByFavoriteCount(safeLimit);
            return ResponseEntity.<List<PostListVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode()).data(convertToPostListVOs(posts)).build();
        } catch (Exception e) {
            log.error("获取收藏排行榜失败", e);
            return ResponseEntity.<List<PostListVO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode()).info("获取收藏排行榜失败").build();
        }
    }

    /**
     * 构建PostDetailVO（含作者、标签、交互状态）
     */
    private PostDetailVO buildPostDetailVO(Post post, Long currentUserId) {
        UserVO author = null;
        if (post.getUserId() != null) {
            try {
                User user = userService.getUserById(post.getUserId());
                author = convertToUserDetailVO(user);
            } catch (Exception e) {
                log.warn("获取作者信息失败: userId={}", post.getUserId(), e);
            }
        }
        List<cn.xu.model.vo.tag.TagVO> tags = Collections.emptyList();
        try {
            List<cn.xu.model.entity.Tag> entityTags = tagService.getTagsByPostId(post.getId());
            tags = entityTags.stream()
                    .map(tag -> cn.xu.model.vo.tag.TagVO.builder().id(tag.getId()).name(tag.getName()).build())
                    .collect(java.util.stream.Collectors.toList());
        } catch (Exception e) {
            log.warn("获取帖子标签失败: postId={}", post.getId(), e);
        }
        boolean isLiked = false, isFavorited = false, isFollowed = false, isAuthor = false;
        if (currentUserId != null) {
            isAuthor = currentUserId.equals(post.getUserId());
            try { isLiked = likeService.checkStatus(currentUserId, Like.LikeType.POST.getCode(), post.getId()); } catch (Exception e) {}
            try { isFavorited = favoriteService.isFavorited(currentUserId, post.getId(), "POST"); } catch (Exception e) {}
            try { if (post.getUserId() != null && !isAuthor) isFollowed = followService.isFollowed(currentUserId, post.getUserId()); } catch (Exception e) {}
        }
        return PostDetailVO.builder()
                .id(post.getId()).title(post.getTitle()).description(post.getDescription())
                .content(post.getContent()).coverUrl(post.getCoverUrl()).author(author).tags(tags)
                .viewCount(post.getViewCount()).likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount()).favoriteCount(post.getFavoriteCount())
                .shareCount(post.getShareCount()).status(post.getStatus())
                .isFeatured(post.getIsFeatured() == 1).isLiked(isLiked).isFavorited(isFavorited)
                .isFollowed(isFollowed).createTime(post.getCreateTime()).updateTime(post.getUpdateTime())
                .build();
    }

    /**
     * User转换为UserVO
     */
    private UserVO convertToUserDetailVO(User user) {
        if (user == null) return null;
        return UserVO.builder()
                .id(user.getId()).username(user.getUsername()).nickname(user.getNickname())
                .avatar(user.getAvatar()).description(user.getDescription())
                .followCount(user.getFollowCount()).fansCount(user.getFansCount())
                .likeCount(user.getLikeCount()).createTime(user.getCreateTime())
                .build();
    }
}