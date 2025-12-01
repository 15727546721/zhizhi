package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
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
import cn.xu.service.user.UserService;
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
 * 首页控制器
 *
 * @author xu
 * @since 2025-11-25
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
    private UserService userService;
    @Resource
    private FollowService followService;

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

    @GetMapping("/following")
    @SaCheckLogin
    @Operation(summary = "getFollowingPosts")
    @ApiOperationLog(description = "getFollowingPosts")
    public ResponseEntity<PageResponse<List<PostListVO>>> getFollowingPosts(
            @Parameter(description = "page") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "size") @RequestParam(defaultValue = "10") Integer size) {
        Long currentUserId = StpUtil.getLoginIdAsLong();
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

    @GetMapping("/search")
    @Operation(summary = "searchPosts")
    @ApiOperationLog(description = "searchPosts")
    public ResponseEntity<PageResponse<List<PostListVO>>> searchPosts(
            @Parameter(description = "keyword") @RequestParam String keyword,
            @Parameter(description = "page") @RequestParam(defaultValue = "1") Integer page,
            @Parameter(description = "size") @RequestParam(defaultValue = "10") Integer size) {
        List<Post> posts = postService.getLatestPosts(page, size);
        long total = postService.countAllPosts();
        List<PostListVO> result = convert(posts);
        PageResponse<List<PostListVO>> pageResponse = PageResponse.ofList(page, size, total, result);
        return ResponseEntity.<PageResponse<List<PostListVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode()).data(pageResponse).build();
    }

    private List<PostListVO> convert(List<Post> posts) {
        if (posts == null || posts.isEmpty()) return Collections.emptyList();
        Set<Long> userIds = new HashSet<>();
        posts.forEach(p -> { if (p.getUserId() != null) userIds.add(p.getUserId()); });
        List<User> users = Collections.emptyList();
        try { users = userService.batchGetUserInfo(new ArrayList<>(userIds)); } catch (Exception e) { log.warn("batchGetUserInfo failed", e); }
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
