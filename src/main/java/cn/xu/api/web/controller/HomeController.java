package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.vo.HomePageResponse;
import cn.xu.api.web.model.vo.post.PostListResponse;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.follow.model.entity.FollowRelationEntity;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.application.service.SearchApplicationService;
import cn.xu.api.web.model.vo.post.PostSearchResponse;
import cn.xu.domain.post.model.entity.PostEntity;
import cn.xu.domain.post.model.valobj.PostType;
import cn.xu.domain.post.service.IPostService;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页控制器
 * 提供首页数据展示接口
 */
@Slf4j
@RestController
@RequestMapping("/api/home")
@Tag(name = "首页接口", description = "首页相关接口")
public class HomeController {

    @Resource
    private IPostService postService;
    @Resource
    private IUserService userService;
    @Resource
    private IFollowService followService;
    @Resource
    private SearchApplicationService searchApplicationService;

    @GetMapping("/page")
    @Operation(summary = "获取首页数据")
    public ResponseEntity<HomePageResponse> getHomePage() {
        try {
            Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
            
            // 获取热门帖子（本周）
            List<PostEntity> hotPosts = new ArrayList<>();
            try {
                hotPosts = postService.findHotPosts(1, 10);
            } catch (Exception e) {
                log.warn("获取热门帖子失败，使用默认排序", e);
                hotPosts = postService.getPostPageList(1, 10);
            }
            
            // 获取最新帖子
            List<PostEntity> latestPosts = postService.getPostPageList(1, 10);
            
            HomePageResponse homePageVO = HomePageResponse.builder()
                    .hotPosts(convertToPostListResponses(hotPosts))
                    .latestPosts(convertToPostListResponses(latestPosts))
                    .build();
            
            return ResponseEntity.<HomePageResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(homePageVO)
                    .build();
        } catch (Exception e) {
            log.error("获取首页数据失败", e);
            return ResponseEntity.<HomePageResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取首页数据失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/following")
    @Operation(summary = "获取关注用户的帖子")
    @SaCheckLogin
    public ResponseEntity<PageResponse<List<PostListResponse>>> getFollowingPosts(
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小，默认为10") @RequestParam(defaultValue = "10") int size) {
        try {
            // 确保页码至少为1
            int safePage = Math.max(1, page);
            int safeSize = Math.max(1, Math.min(size, 100)); // 限制页面大小最大为100
            
            Long currentUserId = StpUtil.getLoginIdAsLong();
            
            // 获取关注的用户ID列表
            List<FollowRelationEntity> followingList = followService.getFollowingList(currentUserId);
            Set<Long> followingUserIds = followingList.stream()
                    .map(FollowRelationEntity::getFollowedId)
                    .collect(Collectors.toSet());
            
            List<PostEntity> followingAuthorsPosts = new ArrayList<>();
            long total = 0;
            if (!followingUserIds.isEmpty()) {
                // 获取关注用户的帖子，按时间倒序排列
                followingAuthorsPosts = postService.getPostsByUserIds(new ArrayList<>(followingUserIds), safePage, safeSize);
                // 获取总数
                total = postService.countPostsByUserIds(new ArrayList<>(followingUserIds));
            }
            
            // 转换为PostListResponse列表
            List<PostListResponse> postListResponses = convertToPostListResponses(followingAuthorsPosts);
            
            // 创建PageResponse对象
            PageResponse<List<PostListResponse>> pageResponse = PageResponse.ofList(
                safePage, 
                safeSize, 
                total, 
                postListResponses
            );
            
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取关注用户文章失败", e);
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取关注用户文章失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/search")
    @Operation(summary = "搜索帖子")
    @ApiOperationLog(description = "搜索帖子")
    public ResponseEntity<PageResponse<List<PostListResponse>>> searchPosts(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小，默认为10") @RequestParam(defaultValue = "10") int size) {
        try {
            // 确保页码至少为1
            int safePage = Math.max(1, page);
            int safeSize = Math.max(1, Math.min(size, 100)); // 限制页面大小最大为100
            
            // 调用搜索应用服务
            SearchApplicationService.SearchResult searchResult = 
                    searchApplicationService.executeSearch(keyword, null, safePage, safeSize);
            
            // 将PostSearchResponse转换为PostListResponse
            List<PostListResponse> postListResponses = new ArrayList<>();
            for (PostSearchResponse response : searchResult.getPosts()) {
                PostListResponse listResponse = new PostListResponse();
                // 从PostSearchResponse构建PostEntity
                PostEntity postEntity = PostEntity.builder()
                        .id(response.getId())
                        .title(response.getTitle() != null ? new cn.xu.domain.post.model.valobj.PostTitle(response.getTitle()) : null)
                        .description(response.getDescription())
                        .content(response.getContent() != null ? new cn.xu.domain.post.model.valobj.PostContent(response.getContent()) : null)
                        .coverUrl(response.getCoverUrl())
                        .userId(response.getUserId())
                        .categoryId(response.getCategoryId())
                        .type(response.getType() != null ? PostType.fromCode(response.getType()) : PostType.POST)
                        .viewCount(response.getViewCount())
                        .likeCount(response.getLikeCount())
                        .commentCount(response.getCommentCount())
                        .favoriteCount(response.getFavoriteCount())
                        .shareCount(response.getShareCount())
                        .isFeatured(response.getIsFeatured() != null ? response.getIsFeatured() : false)
                        .createTime(response.getCreateTime())
                        .updateTime(response.getUpdateTime())
                        .publishTime(response.getPublishTime())
                        .status(cn.xu.domain.post.model.valobj.PostStatus.PUBLISHED)
                        .build();
                listResponse.setPost(postEntity);
                
                // 设置用户信息（如果响应中包含）
                if (response.getUserId() != null) {
                    try {
                        UserEntity user = userService.getUserById(response.getUserId());
                        listResponse.setUser(user);
                    } catch (Exception e) {
                        log.warn("获取用户信息失败: userId={}", response.getUserId(), e);
                    }
                }
                
                postListResponses.add(listResponse);
            }
            
            // 创建PageResponse对象
            PageResponse<List<PostListResponse>> pageResponse = PageResponse.ofList(
                safePage, 
                safeSize, 
                searchResult.getTotal(), 
                postListResponses
            );
            
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("搜索文章失败", e);
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("搜索文章失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/posts")
    @Operation(summary = "获取帖子列表（支持筛选和排序）")
    public ResponseEntity<PageResponse<List<PostListResponse>>> getPosts(
            @Parameter(description = "帖子类型") @RequestParam(required = false) String type,
            @Parameter(description = "标签ID") @RequestParam(required = false) Long tagId,
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "latest") String sort,
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小，默认为10") @RequestParam(defaultValue = "10") int size) {
        try {
            // 确保页码至少为1
            int safePage = Math.max(1, page);
            int safeSize = Math.max(1, Math.min(size, 100)); // 限制页面大小最大为100
            
            List<PostEntity> posts = new ArrayList<>();
            long total = 0;
            
            // 根据类型筛选
            if (type != null && !type.isEmpty()) {
                PostType postType = PostType.fromCode(type);
                posts = postService.findPostsByType(postType, safePage, safeSize);
                total = postService.countPostsByType(postType);
            } 
            // 根据标签筛选
            else if (tagId != null) {
                posts = postService.findPostsByTagId(tagId, safePage, safeSize);
                total = postService.countPostsByTagId(tagId);
            } 
            // 默认获取最新帖子
            else {
                posts = postService.getPostPageList(safePage, safeSize);
                total = postService.countAllPosts();
            }
            
            // 转换为PostListResponse列表
            List<PostListResponse> postListResponses = convertToPostListResponses(posts);
            
            // 创建PageResponse对象
            PageResponse<List<PostListResponse>> pageResponse = PageResponse.ofList(
                safePage, 
                safeSize, 
                total, 
                postListResponses
            );
            
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取帖子列表失败", e);
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子列表失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/hot")
    @Operation(summary = "获取热门帖子")
    public ResponseEntity<PageResponse<List<PostListResponse>>> getHotPosts(
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小，默认为10") @RequestParam(defaultValue = "10") int size) {
        try {
            // 确保页码至少为1
            int safePage = Math.max(1, page);
            int safeSize = Math.max(1, Math.min(size, 100)); // 限制页面大小最大为100
            
            int offset = (safePage - 1) * safeSize;
            List<PostEntity> hotPosts = postService.findHotPosts(safePage, safeSize);
            long total = postService.countHotPosts();
            
            // 转换为PostListResponse列表
            List<PostListResponse> postListResponses = convertToPostListResponses(hotPosts);
            
            // 创建PageResponse对象
            PageResponse<List<PostListResponse>> pageResponse = PageResponse.ofList(
                safePage, 
                safeSize, 
                total, 
                postListResponses
            );
            
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取热门帖子失败", e);
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取热门帖子失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/recommend")
    @Operation(summary = "获取推荐帖子")
    public ResponseEntity<PageResponse<List<PostListResponse>>> getRecommendedPosts(
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小，默认为10") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "分类ID，可选") @RequestParam(required = false) Long categoryId) {
        try {
            // 确保页码至少为1
            int safePage = Math.max(1, page);
            int safeSize = Math.max(1, Math.min(size, 100)); // 限制页面大小最大为100
            
            Long currentUserId = StpUtil.isLogin() ? StpUtil.getLoginIdAsLong() : null;
            
            // 统一使用推荐服务，它会根据用户是否有行为数据自动选择推荐策略：
            // - 匿名用户（未登录）：热门+精选
            // - 新用户（已登录但无行为数据）：热门+精选
            // - 有行为数据的用户（已登录且有关注）：关注+热门+精选
            List<PostEntity> recommendedPosts;
            long total;
            if (currentUserId != null) {
                recommendedPosts = postService.findRecommendedPosts(currentUserId, safePage, safeSize, categoryId);
                total = postService.countRecommendedPosts(currentUserId, categoryId);
            } else {
                // 未登录用户也使用推荐服务，会返回热门+精选
                recommendedPosts = postService.findRecommendedPosts(null, safePage, safeSize, categoryId);
                total = postService.countRecommendedPosts(null, categoryId);
            }
            
            // 转换为PostListResponse列表
            List<PostListResponse> postListResponses = convertToPostListResponses(recommendedPosts);
            
            // 创建PageResponse对象
            PageResponse<List<PostListResponse>> pageResponse = PageResponse.ofList(
                safePage, 
                safeSize, 
                total, 
                postListResponses
            );
            
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取推荐帖子失败", e);
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取推荐帖子失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/related")
    @Operation(summary = "获取相关推荐帖子")
    public ResponseEntity<PageResponse<List<PostListResponse>>> getRelatedPosts(
            @Parameter(description = "帖子类型") @RequestParam String type,
            @Parameter(description = "当前帖子ID") @RequestParam Long excludeId,
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小，默认为5") @RequestParam(defaultValue = "5") int size) {
        try {
            // 确保页码至少为1
            int safePage = Math.max(1, page);
            int safeSize = Math.max(1, Math.min(size, 20)); // 限制页面大小最大为20
            
            // 转换帖子类型
            PostType postType = PostType.fromCode(type);
            
            // 获取相关推荐帖子
            List<PostEntity> relatedPosts = postService.findRelatedPostsByType(postType, excludeId, safeSize);
            
            // 转换为PostListResponse列表
            List<PostListResponse> postListResponses = convertToPostListResponses(relatedPosts);
            
            // 创建PageResponse对象
            PageResponse<List<PostListResponse>> pageResponse = PageResponse.ofList(
                safePage, 
                safeSize,
                    (long) postListResponses.size(),
                postListResponses
            );
            
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取相关推荐帖子失败: type={}, excludeId={}", type, excludeId, e);
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取相关推荐帖子失败: " + e.getMessage())
                    .build();
        }
    }

    @GetMapping("/featured")
    @Operation(summary = "获取精选帖子")
    public ResponseEntity<PageResponse<List<PostListResponse>>> getFeaturedPosts(
            @Parameter(description = "页码，默认为1") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "页面大小，默认为10") @RequestParam(defaultValue = "10") int size) {
        try {
            // 确保页码至少为1
            int safePage = Math.max(1, page);
            int safeSize = Math.max(1, Math.min(size, 100)); // 限制页面大小最大为100
            
            List<PostEntity> featuredPosts = postService.findFeaturedPosts(safePage, safeSize);
            long total = postService.countFeaturedPosts();
            
            // 转换为PostListResponse列表
            List<PostListResponse> postListResponses = convertToPostListResponses(featuredPosts);
            
            // 创建PageResponse对象
            PageResponse<List<PostListResponse>> pageResponse = PageResponse.ofList(
                safePage, 
                safeSize, 
                total, 
                postListResponses
            );
            
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(pageResponse)
                    .build();
        } catch (Exception e) {
            log.error("获取精选帖子失败", e);
            return ResponseEntity.<PageResponse<List<PostListResponse>>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取精选帖子失败: " + e.getMessage())
                    .build();
        }
    }

    private List<PostListResponse> convertToPostListResponses(List<PostEntity> posts) {
        if (posts == null || posts.isEmpty()) {
            return Collections.emptyList();
        }
        
        Set<Long> userIds = new HashSet<>();
        posts.forEach(post -> {
            if (post.getUserId() != null) {
                userIds.add(post.getUserId());
            }
        });
        
        List<UserEntity> users = Collections.emptyList();
        try {
            users = userService.batchGetUserInfo(new ArrayList<>(userIds));
        } catch (Exception e) {
            log.warn("批量获取用户信息失败", e);
        }
        
        java.util.Map<Long, UserEntity> userMap = users.stream()
                .collect(Collectors.toMap(UserEntity::getId, user -> user, (existing, replacement) -> existing));
        
        return posts.stream()
                .map(post -> {
                    UserEntity user = post.getUserId() != null ? userMap.get(post.getUserId()) : null;
                    
                    cn.xu.api.web.model.vo.post.PostListItemVO.PostListItemVOBuilder postItemBuilder = cn.xu.api.web.model.vo.post.PostListItemVO.builder()
                            .id(post.getId())
                            .title(post.getTitle() != null ? post.getTitle().getValue() : null)
                            .description(post.getDescription())
                            .content(post.getContent() != null ? post.getContent().getValue() : null)
                            .coverUrl(post.getCoverUrl())
                            .type(post.getType() != null ? post.getType().getCode() : null)
                            .status(post.getStatus() != null ? post.getStatus().getCode() : null)
                            .userId(post.getUserId())
                            .nickname(user != null ? user.getNickname() : null)
                            .avatar(user != null ? user.getAvatar() : null)
                            .viewCount(post.getViewCount())
                            .likeCount(post.getLikeCount())
                            .commentCount(post.getCommentCount())
                            .favoriteCount(post.getFavoriteCount())
                            .createTime(post.getCreateTime())
                            .updateTime(post.getUpdateTime())
                            .publishTime(post.getPublishTime())
                            .tagNameList(new String[]{});
                    
                    cn.xu.api.web.model.vo.post.PostListItemVO postItem = postItemBuilder.build();
                    
                    PostListResponse vo = PostListResponse.builder()
                            .postItem(postItem)
                            .post(post)
                            .user(user)
                            .build();
                    
                    return vo;
                })
                .collect(Collectors.toList());
    }
}