package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.User;
import cn.xu.model.vo.post.PostItemVO;
import cn.xu.model.vo.post.PostListVO;
import cn.xu.service.follow.FollowService;
import cn.xu.service.post.PostService;
import cn.xu.service.post.TagService;
import cn.xu.service.user.IUserService;
import cn.xu.support.util.LoginUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 首页接口
 */
@Slf4j
@RestController
@RequestMapping("/api/home")
@Tag(name = "首页接口", description = "首页相关API")
public class HomeController {

    @Resource(name = "postService")
    private PostService postService;
    @Resource(name = "tagService")
    private TagService tagService;
    @Resource(name = "userService")
    private IUserService userService;
    @Resource
    private FollowService followService;

    /**
     * 获取帖子列表
     * 
     * <p>支持按标签筛选和排序（最新、热门），返回分页的帖子列表
     * 
     * @param tagId 标签ID（可选），为空时返回所有帖子
     * @param sort 排序方式：latest(最新)、hot(热门)，默认为latest
     * @param page 页码，从1开始，默认为1
     * @param size 每页数量，默认为10
     * @return 分页的帖子列表
     */
    @GetMapping("/posts")
    @Operation(summary = "getPosts")
    @ApiOperationLog(description = "getPosts")
    public ResponseEntity<PageResponse<List<PostListVO>>> getPosts(
            @Parameter(description = "tagId") @RequestParam(required = false) Long tagId,
            @Parameter(description = "sort") @RequestParam(defaultValue = "latest") String sort,
            @Parameter(description = "page") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "size") @RequestParam(defaultValue = "10") Integer size) {
        List<Post> posts;
        long total;
        if (tagId != null) {
            if ("hot".equalsIgnoreCase(sort)) {
                posts = postService.getHotPostsByTag(tagId, page, size);
                total = postService.countHotPostsByTag(tagId);
            } else {
                posts = postService.getPostsByTag(tagId, page, size);
                total = postService.countPostsByTag(tagId);
            }
        } else {
            if ("hot".equalsIgnoreCase(sort)) {
                posts = postService.getHotPosts(page, size);
                total = postService.countHotPosts();
            } else {
                posts = postService.getLatestPosts(page, size);
                total = postService.countAllPosts();
            }
        }
        List<PostListVO> result = convert(posts);
        PageResponse<List<PostListVO>> pageResponse = PageResponse.ofList(page, size, total, result);
        return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode()).data(pageResponse).build();
    }

    /**
     * 获取关注用户的帖子列表
     * 
     * <p>返回当前登录用户关注的所有用户发布的帖子，按时间倒序排列
     * <p>需要登录后才能访问
     * 
     * @param page 页码，从1开始，默认为1
     * @param size 每页数量，默认为10
     * @return 分页的帖子列表，如果未关注任何用户则返回空列表
     */
    @GetMapping("/following")
    @SaCheckLogin
    @Operation(summary = "getFollowingPosts")
    @ApiOperationLog(description = "getFollowingPosts")
    public ResponseEntity<PageResponse<List<PostListVO>>> getFollowingPosts(
            @Parameter(description = "page") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "size") @RequestParam(defaultValue = "10") Integer size) {
        Long currentUserId = LoginUserUtil.getLoginUserId();
        List<Long> followingUserIds = followService.getFollowingUserIds(currentUserId, 500);
        if (followingUserIds.isEmpty()) {
            PageResponse<List<PostListVO>> empty = PageResponse.ofList(page, size, 0L, Collections.emptyList());
            return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                    .code(ResponseCode.SUCCESS.getCode()).data(empty).build();
        }
        List<Post> posts = postService.getPostsByUserIds(followingUserIds, page, size);
        long total = postService.countPostsByUserIds(followingUserIds);
        List<PostListVO> result = convert(posts);
        PageResponse<List<PostListVO>> pageResponse = PageResponse.ofList(page, size, total, result);
        return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode()).data(pageResponse).build();
    }

    /**
     * 获取精选帖子列表
     * 
     * <p>返回管理员标记为精选的帖子，支持按标签筛选
     * 
     * @param tagId 标签ID（可选），为空时返回所有精选帖子
     * @param page 页码，从1开始，默认为1
     * @param size 每页数量，默认为10
     * @return 分页的精选帖子列表
     */
    @GetMapping("/featured")
    @Operation(summary = "getFeaturedPosts")
    @ApiOperationLog(description = "getFeaturedPosts")
    public ResponseEntity<PageResponse<List<PostListVO>>> getFeaturedPosts(
            @Parameter(description = "tagId") @RequestParam(required = false) Long tagId,
            @Parameter(description = "page") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "size") @RequestParam(defaultValue = "10") Integer size) {
        List<Post> posts;
        long total;
        if (tagId != null) {
            posts = postService.getFeaturedPostsByTag(tagId, page, size);
            total = postService.countFeaturedPostsByTag(tagId);
        } else {
            posts = postService.getFeaturedPosts(page, size);
            total = postService.countFeaturedPosts();
        }
        List<PostListVO> result = convert(posts);
        PageResponse<List<PostListVO>> pageResponse = PageResponse.ofList(page, size, total, result);
        return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode()).data(pageResponse).build();
    }

    /**
     * 获取热门帖子列表
     * 
     * <p>根据热度评分排序返回热门帖子，支持按标签筛选
     * <p>热度评分综合考虑了浏览量、点赞数、评论数、收藏数等指标
     * 
     * @param tagId 标签ID（可选），为空时返回所有热门帖子
     * @param page 页码，从1开始，默认为1
     * @param size 每页数量，默认为10
     * @return 分页的热门帖子列表
     */
    @GetMapping("/hot")
    @Operation(summary = "getHotPosts")
    @ApiOperationLog(description = "getHotPosts")
    public ResponseEntity<PageResponse<List<PostListVO>>> getHotPosts(
            @Parameter(description = "tagId") @RequestParam(required = false) Long tagId,
            @Parameter(description = "page") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "size") @RequestParam(defaultValue = "10") Integer size) {
        List<Post> posts;
        long total;
        if (tagId != null) {
            posts = postService.getHotPostsByTag(tagId, page, size);
            total = postService.countHotPostsByTag(tagId);
        } else {
            posts = postService.getHotPosts(page, size);
            total = postService.countHotPosts();
        }
        List<PostListVO> result = convert(posts);
        PageResponse<List<PostListVO>> pageResponse = PageResponse.ofList(page, size, total, result);
        return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode()).data(pageResponse).build();
    }

    /**
     * 获取相关推荐帖子列表
     * 
     * <p>用于帖子详情页的侧边栏推荐，返回热门帖子并排除指定帖子
     * 
     * @param excludeId 要排除的帖子ID（可选），通常为当前正在查看的帖子
     * @param page 页码，从1开始，默认为1
     * @param size 每页数量，默认为5
     * @return 分页的推荐帖子列表
     */
    @GetMapping("/related")
    @Operation(summary = "getRelatedPosts")
    @ApiOperationLog(description = "getRelatedPosts")
    public ResponseEntity<PageResponse<List<PostListVO>>> getRelatedPosts(
            @Parameter(description = "excludeId") @RequestParam(required = false) Long excludeId,
            @Parameter(description = "page") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "size") @RequestParam(defaultValue = "5") Integer size) {
        List<Post> posts = postService.getHotPosts(page, size + 1);
        if (excludeId != null) {
            posts = posts.stream().filter(p -> !excludeId.equals(p.getId())).limit(size).collect(Collectors.toList());
        } else if (posts.size() > size) {
            posts = posts.subList(0, size);
        }
        long total = postService.countHotPosts();
        List<PostListVO> result = convert(posts);
        PageResponse<List<PostListVO>> pageResponse = PageResponse.ofList(page, size, total, result);
        return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode()).data(pageResponse).build();
    }

    /**
     * 将帖子实体列表转换为VO列表
     * 
     * <p>批量查询用户信息并组装到帖子VO中，避免N+1查询问题
     * 
     * @param posts 帖子实体列表
     * @return 帖子VO列表，包含用户信息和统计数据
     */
    private List<PostListVO> convert(List<Post> posts) {
        if (posts == null || posts.isEmpty()) return Collections.emptyList();
        Set<Long> userIds = new HashSet<>();
        posts.forEach(p -> { if (p.getUserId() != null) userIds.add(p.getUserId()); });
        List<User> users = Collections.emptyList();
        try { 
            Map<Long, User> tempMap = userService.batchGetUserInfo(new ArrayList<>(userIds));
            users = new ArrayList<>(tempMap.values());
        } catch (Exception e) { log.warn("batchGetUserInfo failed", e); }
        Map<Long, User> userMap = users.stream().collect(Collectors.toMap(User::getId, u -> u, (a, b) -> a));
        return posts.stream().map(post -> {
            User user = post.getUserId() != null ? userMap.get(post.getUserId()) : null;
            PostItemVO item = PostItemVO.builder()
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
            return PostListVO.builder().postItem(item).build();
        }).collect(Collectors.toList());
    }
}