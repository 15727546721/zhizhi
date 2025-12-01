package cn.xu.controller.web;

import cn.xu.cache.UserRankingCacheRepository;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.entity.Post;
import cn.xu.model.entity.User;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.service.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 排行榜控制器
 *
 * <p>支持用户排行榜、帖子排行榜等
 *
 * @author xu
 * @since 2025-11-27
 */
@Slf4j
@RestController
@RequestMapping("/api/ranking")
@Tag(name = "排行榜接口", description = "用户排行榜、帖子排行榜等")
@RequiredArgsConstructor
public class RankingController {

    private final UserRankingCacheRepository userRankingCacheRepository;
    private final UserService userService;
    private final PostMapper postMapper;

    private static final int MAX_LIMIT = 100;
    private static final int DEFAULT_LIMIT = 20;

    // ==================== 用户排行榜 ====================

    @GetMapping("/users")
    @Operation(summary = "获取用户排行榜", description = "支持按粉丝数、获赞数、帖子数、综合分数排序")
    @ApiOperationLog(description = "获取用户排行榜")
    public ResponseEntity<List<UserRankingVO>> getUserRanking(
            @Parameter(description = "排序类型：fans(粉丝数)、likes(获赞数)、posts(帖子数)、comprehensive(综合)")
            @RequestParam(defaultValue = "comprehensive") String type,
            @Parameter(description = "返回数量，默认20，最大100")
            @RequestParam(defaultValue = "20") Integer limit) {

        try {
            // 参数校验
            int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
            String safeType = validateType(type);

            // 从缓存获取用户ID列表
            List<Long> userIds = userRankingCacheRepository.getUserRankingIds(safeType, 0, safeLimit - 1);

            if (userIds.isEmpty()) {
                log.debug("用户排行榜为空: type={}", safeType);
                return ResponseEntity.<List<UserRankingVO>>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .data(new ArrayList<>())
                        .build();
            }

            // 批量获取用户信息
            List<User> users = userService.batchGetUserInfo(userIds);

            // 转换为VO并保持排序
            List<UserRankingVO> rankings = buildUserRankingVOs(userIds, users, safeType);

            log.debug("获取用户排行榜成功: type={}, count={}", safeType, rankings.size());
            return ResponseEntity.<List<UserRankingVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(rankings)
                    .build();

        } catch (Exception e) {
            log.error("获取用户排行榜失败: type={}", type, e);
            return ResponseEntity.<List<UserRankingVO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取用户排行榜失败")
                    .build();
        }
    }

    @GetMapping("/users/fans")
    @Operation(summary = "获取粉丝排行榜", description = "按粉丝数排序的用户排行榜")
    @ApiOperationLog(description = "获取粉丝排行榜")
    public ResponseEntity<List<UserRankingVO>> getFansRanking(
            @Parameter(description = "返回数量，默认20")
            @RequestParam(defaultValue = "20") Integer limit) {
        return getUserRanking("fans", limit);
    }

    @GetMapping("/users/likes")
    @Operation(summary = "获取获赞排行榜", description = "按获赞数排序的用户排行榜")
    @ApiOperationLog(description = "获取获赞排行榜")
    public ResponseEntity<List<UserRankingVO>> getLikesRanking(
            @Parameter(description = "返回数量，默认20")
            @RequestParam(defaultValue = "20") Integer limit) {
        return getUserRanking("likes", limit);
    }

    @GetMapping("/users/active")
    @Operation(summary = "获取活跃用户排行榜", description = "按帖子数排序的用户排行榜")
    @ApiOperationLog(description = "获取活跃用户排行榜")
    public ResponseEntity<List<UserRankingVO>> getActiveRanking(
            @Parameter(description = "返回数量，默认20")
            @RequestParam(defaultValue = "20") Integer limit) {
        return getUserRanking("posts", limit);
    }

    // ==================== 帖子排行榜 ====================

    @GetMapping("/posts")
    @Operation(summary = "获取帖子排行榜", description = "支持多种排序维度和时间范围")
    @ApiOperationLog(description = "获取帖子排行榜")
    public ResponseEntity<List<PostRankingVO>> getPostRanking(
            @Parameter(description = "时间范围：week(周榜)、month(月榜)、all(总榜)")
            @RequestParam(defaultValue = "week") String period,
            @Parameter(description = "排序方式：hot(热度)、likes(点赞)、favorites(收藏)、comments(评论)、latest(最新)")
            @RequestParam(defaultValue = "hot") String sort,
            @Parameter(description = "返回数量，默认20，最大100")
            @RequestParam(defaultValue = "20") Integer limit) {

        try {
            int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));
            String safeSort = validatePostSort(sort);

            // 计算时间范围
            LocalDateTime startTime = calculateStartTime(period);

            // 查询帖子
            List<Post> posts;
            if (startTime != null) {
                posts = postMapper.findPostsByTimeRangeAndSort(startTime, safeSort, 0, safeLimit);
            } else {
                posts = postMapper.findPostsBySort(safeSort, 0, safeLimit);
            }

            // 批量获取用户信息
            List<Long> userIds = posts.stream()
                    .map(Post::getUserId)
                    .distinct()
                    .collect(Collectors.toList());

            Map<Long, User> userMap = new HashMap<>();
            if (!userIds.isEmpty()) {
                List<User> users = userService.batchGetUserInfo(userIds);
                userMap = users.stream()
                        .collect(Collectors.toMap(User::getId, u -> u, (u1, u2) -> u1));
            }

            // 转换为VO
            List<PostRankingVO> rankings = new ArrayList<>();
            int rank = 1;
            for (Post post : posts) {
                User user = userMap.get(post.getUserId());
                PostRankingVO vo = PostRankingVO.builder()
                        .rank(rank++)
                        .postId(post.getId())
                        .title(post.getTitle())
                        .coverUrl(post.getCoverUrl())
                        .userId(post.getUserId())
                        .nickname(user != null ? user.getNickname() : null)
                        .avatar(user != null ? user.getAvatar() : null)
                        .viewCount(post.getViewCount() != null ? post.getViewCount() : 0L)
                        .likeCount(post.getLikeCount() != null ? post.getLikeCount() : 0L)
                        .commentCount(post.getCommentCount() != null ? post.getCommentCount() : 0L)
                        .favoriteCount(post.getFavoriteCount() != null ? post.getFavoriteCount() : 0L)
                        .createTime(post.getCreateTime())
                        .score(calculatePostScore(post))
                        .build();
                rankings.add(vo);
            }

            log.debug("获取帖子排行榜成功: period={}, count={}", period, rankings.size());
            return ResponseEntity.<List<PostRankingVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(rankings)
                    .build();

        } catch (Exception e) {
            log.error("获取帖子排行榜失败: period={}", period, e);
            return ResponseEntity.<List<PostRankingVO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取帖子排行榜失败")
                    .build();
        }
    }

    @GetMapping("/posts/week")
    @Operation(summary = "获取帖子周榜")
    @ApiOperationLog(description = "获取帖子周榜")
    public ResponseEntity<List<PostRankingVO>> getWeeklyPostRanking(
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "hot") String sort,
            @Parameter(description = "返回数量，默认20") @RequestParam(defaultValue = "20") Integer limit) {
        return getPostRanking("week", sort, limit);
    }

    @GetMapping("/posts/month")
    @Operation(summary = "获取帖子月榜")
    @ApiOperationLog(description = "获取帖子月榜")
    public ResponseEntity<List<PostRankingVO>> getMonthlyPostRanking(
            @Parameter(description = "排序方式") @RequestParam(defaultValue = "hot") String sort,
            @Parameter(description = "返回数量，默认20") @RequestParam(defaultValue = "20") Integer limit) {
        return getPostRanking("month", sort, limit);
    }

    // ==================== 标签排行榜 ====================

    @GetMapping("/tags")
    @Operation(summary = "获取标签排行榜", description = "支持按使用数量、热度排序")
    @ApiOperationLog(description = "获取标签排行榜")
    public ResponseEntity<List<TagRankingVO>> getTagRanking(
            @Parameter(description = "排序方式：count(使用数量)、hot(热度)")
            @RequestParam(defaultValue = "count") String sort,
            @Parameter(description = "时间范围：week(周)、month(月)、all(全部)")
            @RequestParam(defaultValue = "all") String period,
            @Parameter(description = "返回数量，默认20，最大100")
            @RequestParam(defaultValue = "20") Integer limit) {

        try {
            int safeLimit = Math.max(1, Math.min(limit, MAX_LIMIT));

            // 查询热门标签
            // 使用 TagController 已有的热门标签接口逻辑
            // 这里简化处理，直接返回按使用数量排序的标签
            List<cn.xu.model.entity.Tag> tags = postMapper.findHotTags(safeLimit);

            List<TagRankingVO> rankings = new ArrayList<>();
            int rank = 1;
            for (cn.xu.model.entity.Tag tag : tags) {
                TagRankingVO vo = TagRankingVO.builder()
                        .rank(rank++)
                        .tagId(tag.getId())
                        .name(tag.getName())
                        .description(tag.getDescription())
                        .usageCount(tag.getUsageCount() != null ? tag.getUsageCount().longValue() : 0L)
                        .build();
                rankings.add(vo);
            }

            return ResponseEntity.<List<TagRankingVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(rankings)
                    .build();

        } catch (Exception e) {
            log.error("获取标签排行榜失败", e);
            return ResponseEntity.<List<TagRankingVO>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取标签排行榜失败")
                    .build();
        }
    }

    // ==================== 私有方法 ====================

    /**
     * 校验用户排序类型
     */
    private String validateType(String type) {
        if (type == null || type.isEmpty()) {
            return "comprehensive";
        }
        switch (type.toLowerCase()) {
            case "fans":
            case "likes":
            case "posts":
            case "comprehensive":
                return type.toLowerCase();
            default:
                return "comprehensive";
        }
    }

    /**
     * 校验帖子排序类型
     */
    private String validatePostSort(String sort) {
        if (sort == null || sort.isEmpty()) {
            return "hot";
        }
        switch (sort.toLowerCase()) {
            case "hot":
            case "likes":
            case "favorites":
            case "comments":
            case "latest":
            case "views":
                return sort.toLowerCase();
            default:
                return "hot";
        }
    }

    /**
     * 构建用户排行榜VO列表
     */
    private List<UserRankingVO> buildUserRankingVOs(List<Long> sortedUserIds, List<User> users, String type) {
        // 构建用户ID到User的映射
        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, u -> u, (u1, u2) -> u1));

        List<UserRankingVO> rankings = new ArrayList<>();
        int rank = 1;

        for (Long userId : sortedUserIds) {
            User user = userMap.get(userId);
            if (user != null) {
                UserRankingVO vo = UserRankingVO.builder()
                        .rank(rank++)
                        .userId(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .description(user.getDescription())
                        .fansCount(user.getFansCount() != null ? user.getFansCount() : 0L)
                        .likeCount(user.getLikeCount() != null ? user.getLikeCount() : 0L)
                        .postCount(user.getPostCount() != null ? user.getPostCount() : 0L)
                        .score(calculateScore(user, type))
                        .build();
                rankings.add(vo);
            }
        }

        return rankings;
    }

    /**
     * 计算排行分数
     */
    private Long calculateScore(User user, String type) {
        switch (type) {
            case "fans":
                return user.getFansCount() != null ? user.getFansCount() : 0L;
            case "likes":
                return user.getLikeCount() != null ? user.getLikeCount() : 0L;
            case "posts":
                return user.getPostCount() != null ? user.getPostCount() : 0L;
            case "comprehensive":
            default:
                long fans = user.getFansCount() != null ? user.getFansCount() : 0L;
                long likes = user.getLikeCount() != null ? user.getLikeCount() : 0L;
                long posts = user.getPostCount() != null ? user.getPostCount() : 0L;
                return (long) (fans * 0.4 + likes * 0.4 + posts * 0.2);
        }
    }

    /**
     * 计算时间范围起始时间
     */
    private LocalDateTime calculateStartTime(String period) {
        LocalDateTime now = LocalDateTime.now();
        switch (period.toLowerCase()) {
            case "week":
                return now.minusWeeks(1);
            case "month":
                return now.minusMonths(1);
            case "all":
            default:
                return null;
        }
    }

    /**
     * 计算帖子热度分数
     */
    private Long calculatePostScore(Post post) {
        long likes = post.getLikeCount() != null ? post.getLikeCount() : 0L;
        long comments = post.getCommentCount() != null ? post.getCommentCount() : 0L;
        long favorites = post.getFavoriteCount() != null ? post.getFavoriteCount() : 0L;
        return likes + comments * 2 + favorites * 3;
    }

    // ==================== 内部VO类 ====================

    /**
     * 用户排行榜VO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class UserRankingVO {
        /** 排名 */
        private Integer rank;
        /** 用户ID */
        private Long userId;
        /** 用户名 */
        private String username;
        /** 昵称 */
        private String nickname;
        /** 头像 */
        private String avatar;
        /** 简介 */
        private String description;
        /** 粉丝数 */
        private Long fansCount;
        /** 获赞数 */
        private Long likeCount;
        /** 帖子数 */
        private Long postCount;
        /** 排行分数 */
        private Long score;
    }

    /**
     * 帖子排行榜VO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class PostRankingVO {
        /** 排名 */
        private Integer rank;
        /** 帖子ID */
        private Long postId;
        /** 标题 */
        private String title;
        /** 封面 */
        private String coverUrl;
        /** 作者ID */
        private Long userId;
        /** 作者昵称 */
        private String nickname;
        /** 作者头像 */
        private String avatar;
        /** 浏览数 */
        private Long viewCount;
        /** 点赞数 */
        private Long likeCount;
        /** 评论数 */
        private Long commentCount;
        /** 收藏数 */
        private Long favoriteCount;
        /** 发布时间 */
        private LocalDateTime createTime;
        /** 热度分数 */
        private Long score;
    }

    /**
     * 标签排行榜VO
     */
    @lombok.Data
    @lombok.Builder
    @lombok.NoArgsConstructor
    @lombok.AllArgsConstructor
    public static class TagRankingVO {
        /** 排名 */
        private Integer rank;
        /** 标签ID */
        private Long tagId;
        /** 标签名称 */
        private String name;
        /** 标签描述 */
        private String description;
        /** 使用数量 */
        private Long usageCount;
    }
}