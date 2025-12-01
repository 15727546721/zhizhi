package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.repository.mapper.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * 数据统计控制器
 * 
 * @author xu
 * @since 2025-11-30
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

    @Operation(summary = "获取数据统计概览")
    @GetMapping("/overview")
    @SaCheckLogin
    @SaCheckPermission("system:statistics:view")
    @ApiOperationLog(description = "获取数据统计概览")
    public ResponseEntity<StatisticsOverview> getOverview() {
        log.info("获取数据统计概览");
        
        StatisticsOverview overview = StatisticsOverview.builder()
                .postCount(safeCount(() -> postMapper.countAll()))
                .userCount(safeCount(() -> userMapper.countAll()))
                .commentCount(safeCount(() -> commentMapper.countAll()))
                .tagCount(safeCount(() -> tagMapper.countAll()))
                .likeCount(safeCount(() -> likeMapper.countAll()))
                .favoriteCount(safeCount(() -> favoriteMapper != null ? favoriteMapper.countAll() : 0L))
                .followCount(safeCount(() -> followMapper != null ? followMapper.countAll() : 0L))
                .build();
        
        return ResponseEntity.<StatisticsOverview>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info(ResponseCode.SUCCESS.getMessage())
                .data(overview)
                .build();
    }

    @Operation(summary = "获取帖子统计")
    @GetMapping("/posts")
    @SaCheckLogin
    @SaCheckPermission("system:statistics:view")
    @ApiOperationLog(description = "获取帖子统计")
    public ResponseEntity<Map<String, Object>> getPostStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", safeCount(() -> postMapper.countAll()));
        stats.put("published", safeCount(() -> postMapper.countByStatus(1)));
        stats.put("draft", safeCount(() -> postMapper.countByStatus(0)));
        stats.put("featured", safeCount(() -> postMapper.countFeatured()));
        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }

    @Operation(summary = "获取用户统计")
    @GetMapping("/users")
    @SaCheckLogin
    @SaCheckPermission("system:statistics:view")
    @ApiOperationLog(description = "获取用户统计")
    public ResponseEntity<Map<String, Object>> getUserStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("total", safeCount(() -> userMapper.countAll()));
        stats.put("active", safeCount(() -> userMapper.countByStatus(1)));
        stats.put("disabled", safeCount(() -> userMapper.countByStatus(0)));
        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }

    @Operation(summary = "获取互动统计")
    @GetMapping("/interactions")
    @SaCheckLogin
    @SaCheckPermission("system:statistics:view")
    @ApiOperationLog(description = "获取互动统计")
    public ResponseEntity<Map<String, Object>> getInteractionStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("comments", safeCount(() -> commentMapper.countAll()));
        stats.put("likes", safeCount(() -> likeMapper.countAll()));
        stats.put("favorites", safeCount(() -> favoriteMapper != null ? favoriteMapper.countAll() : 0L));
        stats.put("follows", safeCount(() -> followMapper != null ? followMapper.countAll() : 0L));
        // 按类型统计点赞
        stats.put("postLikes", safeCount(() -> likeMapper.countByType(1)));
        stats.put("commentLikes", safeCount(() -> likeMapper.countByType(3)));
        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(stats)
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
}
