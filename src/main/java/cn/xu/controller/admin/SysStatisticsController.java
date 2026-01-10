package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.repository.mapper.*;
import cn.xu.service.statistics.OnlineUserStatisticsService;
import cn.xu.service.statistics.PostViewStatisticsService;
import cn.xu.service.statistics.StatisticsCacheService;
import cn.xu.service.statistics.VisitStatisticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 数据统计控制器
 * 
 * <p>提供后台数据统计功能，包括帖子、用户、互动等统计</p>
 * <p>需要登录并拥有相应权限</p>
 
 */
@Slf4j
@RestController
@RequestMapping("/api/system/statistics")
@Tag(name = "数据统计", description = "后台数据统计接口")
public class SysStatisticsController {

    @Resource
    private PostMapper postMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private LikeMapper likeMapper;
    @Resource
    private TagMapper tagMapper;
    @Autowired(required = false)
    private FavoriteMapper favoriteMapper;
    @Autowired(required = false)
    private FollowMapper followMapper;
    @Autowired(required = false)
    private ReportMapper reportMapper;
    @Autowired(required = false)
    private FeedbackMapper feedbackMapper;
    @Autowired(required = false)
    private AnnouncementMapper announcementMapper;
    
    @Resource
    private VisitStatisticsService visitStatisticsService;
    @Resource
    private PostViewStatisticsService postViewStatisticsService;
    @Resource
    private OnlineUserStatisticsService onlineUserStatisticsService;
    @Resource
    private StatisticsCacheService statisticsCacheService;

    /**
     * 获取数据统计概览
     * 
     * <p>返回所有核心数据的统计概览
     * <p>需要system:statistics:view权限
     * 
     * @return 统计概览数据
     */
    @Operation(summary = "获取数据统计概览")
    @GetMapping("/overview")
    @SaCheckLogin
    @SaCheckPermission("system:statistics:view")
    @ApiOperationLog(description = "获取数据统计概览")
    public ResponseEntity<StatisticsOverview> getOverview() {
        log.info("获取数据统计概览");
        
        StatisticsOverview overview = statisticsCacheService.getOrCompute("overview", StatisticsOverview.class, () ->
            StatisticsOverview.builder()
                .postCount(safeCount(() -> postMapper.countAll()))
                .userCount(safeCount(() -> userMapper.countAll()))
                .commentCount(safeCount(() -> commentMapper.countAll()))
                .tagCount(safeCount(() -> tagMapper.countAll()))
                .likeCount(safeCount(() -> likeMapper.countAll()))
                .favoriteCount(safeCount(() -> favoriteMapper != null ? favoriteMapper.countAll() : 0L))
                .followCount(safeCount(() -> followMapper != null ? followMapper.countAll() : 0L))
                .build()
        );
        
        return ResponseEntity.<StatisticsOverview>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(overview)
                .build();
    }

    /**
     * 获取帖子统计
     * 
     * <p>返回帖子总数、已发布、草稿、加精数量
     * <p>需要system:statistics:view权限
     * 
     * @return 帖子统计数据
     */
    @Operation(summary = "获取帖子统计")
    @GetMapping("/posts")
    @SaCheckLogin
    @SaCheckPermission("system:statistics:view")
    @ApiOperationLog(description = "获取帖子统计")
    public ResponseEntity<Map<String, Object>> getPostStatistics() {
        Map<String, Object> stats = statisticsCacheService.getOrCompute("posts", Map.class, () -> {
            Map<String, Object> data = new HashMap<>();
            data.put("total", safeCount(() -> postMapper.countAll()));
            data.put("published", safeCount(() -> postMapper.countByStatus(1)));
            data.put("draft", safeCount(() -> postMapper.countByStatus(0)));
            data.put("featured", safeCount(() -> postMapper.countFeatured()));
            return data;
        });
        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }

    /**
     * 获取用户统计
     * 
     * <p>返回用户总数、活跃用户、禁用用户数量
     * <p>需要system:statistics:view权限
     * 
     * @return 用户统计数据
     */
    @Operation(summary = "获取用户统计")
    @GetMapping("/users")
    @SaCheckLogin
    @SaCheckPermission("system:statistics:view")
    @ApiOperationLog(description = "获取用户统计")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = statisticsCacheService.getOrCompute("users", Map.class, () -> {
            Map<String, Object> data = new HashMap<>();
            data.put("total", safeCount(() -> userMapper.countAll()));
            data.put("active", safeCount(() -> userMapper.countByStatus(1)));
            data.put("disabled", safeCount(() -> userMapper.countByStatus(0)));
            return data;
        });
        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }

    /**
     * 获取互动统计
     * 
     * <p>返回评论、点赞、收藏、关注等互动数据统计
     * <p>需要system:statistics:view权限
     * 
     * @return 互动统计数据
     */
    @Operation(summary = "获取互动统计")
    @GetMapping("/interactions")
    @SaCheckLogin
    @SaCheckPermission("system:statistics:view")
    @ApiOperationLog(description = "获取互动统计")
    public ResponseEntity<Map<String, Object>> getInteractionStatistics() {
        Map<String, Object> stats = statisticsCacheService.getOrCompute("interactions", Map.class, () -> {
            Map<String, Object> data = new HashMap<>();
            data.put("comments", safeCount(() -> commentMapper.countAll()));
            data.put("likes", safeCount(() -> likeMapper.countAll()));
            data.put("favorites", safeCount(() -> favoriteMapper != null ? favoriteMapper.countAll() : 0L));
            data.put("follows", safeCount(() -> followMapper != null ? followMapper.countAll() : 0L));
            data.put("postLikes", safeCount(() -> likeMapper.countByType(1)));
            data.put("commentLikes", safeCount(() -> likeMapper.countByType(3)));
            return data;
        });
        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }

    // ==================== 仪表盘统计接口 ====================

    /**
     * 获取仪表盘统计数据
     * 
     * <p>返回仪表盘所需的核心统计数据
     * 
     * @return 仪表盘统计数据
     */
    @Operation(summary = "获取仪表盘统计数据")
    @GetMapping("/dashboard")
    @SaCheckLogin
    @ApiOperationLog(description = "获取仪表盘统计数据")
    public ResponseEntity<DashboardStats> getDashboardStats() {
        log.info("获取仪表盘统计数据");
        
        // 使用60秒缓存（因为包含实时在线用户数）
        DashboardStats stats = statisticsCacheService.getOrCompute("dashboard", DashboardStats.class, () -> {
            // 总数统计
            long totalPosts = safeCount(() -> postMapper.countAll());
            long totalUsers = safeCount(() -> userMapper.countAll());
            long totalComments = safeCount(() -> commentMapper.countAll());
            long totalTags = safeCount(() -> tagMapper.countAll());
            
            // 今日统计
            LocalDateTime todayStart = LocalDate.now().atStartOfDay();
            long todayPosts = safeCount(() -> postMapper.countByCreateTimeAfter(todayStart));
            long todayUsers = safeCount(() -> userMapper.countByCreateTimeAfter(todayStart));
            long todayComments = safeCount(() -> commentMapper.countByCreateTimeAfter(todayStart));
            
            // 昨日统计（用于计算环比）
            LocalDateTime yesterdayStart = LocalDate.now().minusDays(1).atStartOfDay();
            LocalDateTime yesterdayEnd = todayStart;
            long yesterdayPosts = safeCount(() -> postMapper.countByCreateTimeBetween(yesterdayStart, yesterdayEnd));
            long yesterdayComments = safeCount(() -> commentMapper.countByCreateTimeBetween(yesterdayStart, yesterdayEnd));
            
            // 待处理事项
            long pendingReports = safeCount(() -> reportMapper != null ? reportMapper.countByStatusValue(0) : 0L);
            long pendingFeedbacks = safeCount(() -> feedbackMapper != null ? feedbackMapper.countByStatus(0) : 0L);
            
            // 在线用户数
            int onlineUserCount = onlineUserStatisticsService.getOnlineUserCount();
            
            return DashboardStats.builder()
                    .totalPosts(totalPosts)
                    .totalUsers(totalUsers)
                    .totalComments(totalComments)
                    .totalTags(totalTags)
                    .todayPosts(todayPosts)
                    .todayUsers(todayUsers)
                    .todayComments(todayComments)
                    .yesterdayPosts(yesterdayPosts)
                    .yesterdayComments(yesterdayComments)
                    .pendingReports(pendingReports)
                    .pendingFeedbacks(pendingFeedbacks)
                    .onlineUserCount(onlineUserCount)
                    .build();
        }, 60); // 60秒缓存
        
        return ResponseEntity.<DashboardStats>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }

    /**
     * 获取数据趋势
     * 
     * <p>返回近N天的数据趋势
     * 
     * @param days 天数，默认7天
     * @return 趋势数据
     */
    @Operation(summary = "获取数据趋势")
    @GetMapping("/trend")
    @SaCheckLogin
    @ApiOperationLog(description = "获取数据趋势")
    public ResponseEntity<TrendData> getTrend(
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") Integer days) {
        log.info("获取数据趋势: days={}", days);
        
        TrendData trendData = statisticsCacheService.getOrCompute("trend_" + days, TrendData.class, () -> {
            List<String> dates = new ArrayList<>();
            List<Long> postCounts = new ArrayList<>();
            List<Long> userCounts = new ArrayList<>();
            List<Long> commentCounts = new ArrayList<>();
            
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd");
            
            for (int i = days - 1; i >= 0; i--) {
                LocalDate date = LocalDate.now().minusDays(i);
                LocalDateTime dayStart = date.atStartOfDay();
                LocalDateTime dayEnd = date.plusDays(1).atStartOfDay();
                
                dates.add(date.format(formatter));
                postCounts.add(safeCount(() -> postMapper.countByCreateTimeBetween(dayStart, dayEnd)));
                userCounts.add(safeCount(() -> userMapper.countByCreateTimeBetween(dayStart, dayEnd)));
                commentCounts.add(safeCount(() -> commentMapper.countByCreateTimeBetween(dayStart, dayEnd)));
            }
            
            return TrendData.builder()
                    .dates(dates)
                    .postCounts(postCounts)
                    .userCounts(userCounts)
                    .commentCounts(commentCounts)
                    .build();
        });
        
        return ResponseEntity.<TrendData>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(trendData)
                .build();
    }

    /**
     * 获取热门帖子排行
     * 
     * @param limit 数量限制
     * @return 热门帖子列表（含环比数据）
     */
    @Operation(summary = "获取热门帖子排行")
    @GetMapping("/hot-posts")
    @SaCheckLogin
    @ApiOperationLog(description = "获取热门帖子排行")
    public ResponseEntity<List<HotPostVO>> getHotPosts(
            @Parameter(description = "数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门帖子排行: limit={}", limit);
        
        List<Map<String, Object>> hotPosts = postMapper.selectHotPosts(limit);
        if (hotPosts == null || hotPosts.isEmpty()) {
            return ResponseEntity.<List<HotPostVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(new ArrayList<>())
                    .build();
        }
        
        // 获取帖子ID列表
        List<Long> postIds = hotPosts.stream()
                .map(p -> ((Number) p.get("id")).longValue())
                .collect(Collectors.toList());
        
        // 批量获取昨日浏览量
        Map<Long, Long> yesterdayViews = postViewStatisticsService.getYesterdayViewCounts(postIds);
        
        // 构建返回数据
        List<HotPostVO> result = hotPosts.stream().map(post -> {
            Long postId = ((Number) post.get("id")).longValue();
            long todayViewCount = post.get("viewCount") != null ? ((Number) post.get("viewCount")).longValue() : 0;
            long yesterdayViewCount = yesterdayViews.getOrDefault(postId, 0L);
            double growthRate = postViewStatisticsService.calculateGrowthRate(todayViewCount, yesterdayViewCount);
            
            return HotPostVO.builder()
                    .id(postId)
                    .title((String) post.get("title"))
                    .viewCount(todayViewCount)
                    .likeCount(post.get("likeCount") != null ? ((Number) post.get("likeCount")).longValue() : 0)
                    .commentCount(post.get("commentCount") != null ? ((Number) post.get("commentCount")).longValue() : 0)
                    .authorName((String) post.get("authorName"))
                    .yesterdayViewCount(yesterdayViewCount)
                    .growthRate(Math.round(growthRate * 10) / 10.0) // 保留一位小数
                    .build();
        }).collect(Collectors.toList());
        
        return ResponseEntity.<List<HotPostVO>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(result)
                .build();
    }

    /**
     * 获取活跃用户排行
     * 
     * @param limit 数量限制
     * @return 活跃用户列表
     */
    @Operation(summary = "获取活跃用户排行")
    @GetMapping("/active-users")
    @SaCheckLogin
    @ApiOperationLog(description = "获取活跃用户排行")
    public ResponseEntity<List<Map<String, Object>>> getActiveUsers(
            @Parameter(description = "数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取活跃用户排行: limit={}", limit);
        
        List<Map<String, Object>> activeUsers = userMapper.selectActiveUsers(limit);
        
        return ResponseEntity.<List<Map<String, Object>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(activeUsers != null ? activeUsers : new ArrayList<>())
                .build();
    }

    /**
     * 获取热门标签排行
     * 
     * @param limit 数量限制
     * @return 热门标签列表
     */
    @Operation(summary = "获取热门标签排行")
    @GetMapping("/hot-tags")
    @SaCheckLogin
    @ApiOperationLog(description = "获取热门标签排行")
    public ResponseEntity<List<Map<String, Object>>> getHotTags(
            @Parameter(description = "数量") @RequestParam(defaultValue = "10") Integer limit) {
        log.info("获取热门标签排行: limit={}", limit);
        
        List<Map<String, Object>> hotTags = tagMapper.selectHotTags(limit);
        
        return ResponseEntity.<List<Map<String, Object>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(hotTags != null ? hotTags : new ArrayList<>())
                .build();
    }

    /**
     * 获取内容类型分布
     * 
     * @return 内容类型分布数据
     */
    @Operation(summary = "获取内容类型分布")
    @GetMapping("/content-distribution")
    @SaCheckLogin
    @ApiOperationLog(description = "获取内容类型分布")
    public ResponseEntity<List<Map<String, Object>>> getContentDistribution() {
        log.info("获取内容类型分布");
        
        // 按帖子状态统计
        List<Map<String, Object>> distribution = new ArrayList<>();
        
        Map<String, Object> published = new HashMap<>();
        published.put("name", "已发布");
        published.put("value", safeCount(() -> postMapper.countByStatus(1)));
        distribution.add(published);
        
        Map<String, Object> draft = new HashMap<>();
        draft.put("name", "草稿");
        draft.put("value", safeCount(() -> postMapper.countByStatus(0)));
        distribution.add(draft);
        
        Map<String, Object> featured = new HashMap<>();
        featured.put("name", "精选");
        featured.put("value", safeCount(() -> postMapper.countFeatured()));
        distribution.add(featured);
        
        return ResponseEntity.<List<Map<String, Object>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(distribution)
                .build();
    }

    /**
     * 获取UV/PV统计数据
     * 
     * @return UV/PV统计数据
     */
    @Operation(summary = "获取UV/PV统计数据")
    @GetMapping("/visit")
    @SaCheckLogin
    @ApiOperationLog(description = "获取UV/PV统计数据")
    public ResponseEntity<VisitStats> getVisitStats() {
        log.info("获取UV/PV统计数据");
        
        VisitStats stats = VisitStats.builder()
                .todayUV(visitStatisticsService.getTodayUV())
                .yesterdayUV(visitStatisticsService.getYesterdayUV())
                .totalUV(visitStatisticsService.getTotalUV())
                .todayPV(visitStatisticsService.getTodayPV())
                .yesterdayPV(visitStatisticsService.getYesterdayPV())
                .totalPV(visitStatisticsService.getTotalPV())
                .build();
        
        return ResponseEntity.<VisitStats>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }

    /**
     * 获取UV/PV趋势数据
     * 
     * @param days 天数
     * @return UV/PV趋势数据
     */
    @Operation(summary = "获取UV/PV趋势数据")
    @GetMapping("/visit-trend")
    @SaCheckLogin
    @ApiOperationLog(description = "获取UV/PV趋势数据")
    public ResponseEntity<VisitTrendData> getVisitTrend(
            @Parameter(description = "天数") @RequestParam(defaultValue = "7") Integer days) {
        log.info("获取UV/PV趋势数据: days={}", days);
        
        VisitTrendData trendData = VisitTrendData.builder()
                .dates(visitStatisticsService.getDateLabels(days))
                .uvCounts(visitStatisticsService.getUVTrend(days))
                .pvCounts(visitStatisticsService.getPVTrend(days))
                .build();
        
        return ResponseEntity.<VisitTrendData>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(trendData)
                .build();
    }

    /**
     * 安全执行统计查询
     */
    private Long safeCount(CountSupplier supplier) {
        try {
            Long count = supplier.get();
            return count != null ? count : 0L;
        } catch (Exception e) {
            log.warn("统计查询失败: {}", e.getMessage());
            return 0L;
        }
    }

    @FunctionalInterface
    interface CountSupplier {
        Long get();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class StatisticsOverview {
        private Long postCount;
        private Long userCount;
        private Long commentCount;
        private Long tagCount;
        private Long likeCount;
        private Long favoriteCount;
        private Long followCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DashboardStats {
        private Long totalPosts;
        private Long totalUsers;
        private Long totalComments;
        private Long totalTags;
        private Long todayPosts;
        private Long todayUsers;
        private Long todayComments;
        private Long yesterdayPosts;
        private Long yesterdayComments;
        private Long pendingReports;
        private Long pendingFeedbacks;
        private Integer onlineUserCount;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HotPostVO {
        private Long id;
        private String title;
        private Long viewCount;
        private Long likeCount;
        private Long commentCount;
        private String authorName;
        private Long yesterdayViewCount;
        private Double growthRate; // 环比增长率
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TrendData {
        private List<String> dates;
        private List<Long> postCounts;
        private List<Long> userCounts;
        private List<Long> commentCounts;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitStats {
        private Long todayUV;
        private Long yesterdayUV;
        private Long totalUV;
        private Long todayPV;
        private Long yesterdayPV;
        private Long totalPV;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitTrendData {
        private List<String> dates;
        private List<Long> uvCounts;
        private List<Long> pvCounts;
    }
}