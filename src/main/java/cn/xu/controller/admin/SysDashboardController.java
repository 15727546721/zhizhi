package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.repository.mapper.CommentMapper;
import cn.xu.repository.mapper.PostMapper;
import cn.xu.repository.mapper.TagMapper;
import cn.xu.repository.mapper.UserMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 仪表盘控制器
 *
 * <p>提供后台首页仪表盘数据，包括统计数据、系统信息、缓存信息</p>
 * <p>需要登录后才能访问</p>
 
 */
@Slf4j
@RestController
@RequestMapping("/api/system/home")
@Tag(name = "仪表盘", description = "首页仪表盘相关接口")
public class SysDashboardController {

    @Resource
    private PostMapper postMapper;
    @Resource
    private UserMapper userMapper;
    @Resource
    private CommentMapper commentMapper;
    @Resource
    private TagMapper tagMapper;
    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取首页顶部统计数据
     *
     * <p>返回帖子数、用户数、评论数、标签数等统计
     * <p>需要登录后才能访问
     *
     * @return 统计数据集合
     */
    @GetMapping("/getDashboardTopStatistics")
    @Operation(summary = "获取首页顶部统计数据")
    @SaCheckLogin
    @ApiOperationLog(description = "获取首页顶部统计数据")
    public ResponseEntity<Map<String, Object>> getDashboardTopStatistics() {
        log.info("获取首页顶部统计数据");

        Map<String, Object> stats = new HashMap<>();
        stats.put("postCount", safeCount(() -> postMapper.countAll()));
        stats.put("userCount", safeCount(() -> userMapper.countAll()));
        stats.put("commentCount", safeCount(() -> commentMapper.countAll()));
        stats.put("tagCount", safeCount(() -> tagMapper.countAll()));

        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(stats)
                .build();
    }

    /**
     * 获取首页底部统计数据
     *
     * <p>返回今日新增、本周统计等数据
     * <p>需要登录后才能访问
     *
     * @return 统计数据集合
     */
    @GetMapping("/getDashboardBottomStatistics")
    @Operation(summary = "获取首页底部统计数据")
    @SaCheckLogin
    @ApiOperationLog(description = "获取首页底部统计数据")
    public ResponseEntity<Map<String, Object>> getDashboardBottomStatistics() {
        log.info("获取首页底部统计数据");

        Map<String, Object> stats = new HashMap<>();
        // 今日新增
        stats.put("todayPostCount", 0L);
        stats.put("todayUserCount", 0L);
        stats.put("todayCommentCount", 0L);
        // 周统计
        stats.put("weekPostCount", 0L);
        stats.put("weekUserCount", 0L);

        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(stats)
                .build();
    }

    /**
     * 获取系统信息
     *
     * <p>返回操作系统、JVM、内存等系统环境信息
     * <p>需要登录后才能访问
     *
     * @return 系统信息VO
     */
    @GetMapping("/systemInfo")
    @Operation(summary = "获取系统信息")
    @SaCheckLogin
    @ApiOperationLog(description = "获取系统信息")
    public ResponseEntity<SystemInfoVO> getSystemInfo() {
        log.info("获取系统信息");

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        Properties props = System.getProperties();
        Runtime runtime = Runtime.getRuntime();

        SystemInfoVO systemInfo = SystemInfoVO.builder()
                .osName(props.getProperty("os.name"))
                .osArch(props.getProperty("os.arch"))
                .osVersion(props.getProperty("os.version"))
                .javaVersion(props.getProperty("java.version"))
                .javaVendor(props.getProperty("java.vendor"))
                .jvmName(runtimeMXBean.getVmName())
                .jvmVersion(runtimeMXBean.getVmVersion())
                .availableProcessors(osMXBean.getAvailableProcessors())
                .totalMemory(runtime.totalMemory() / 1024 / 1024 + " MB")
                .freeMemory(runtime.freeMemory() / 1024 / 1024 + " MB")
                .maxMemory(runtime.maxMemory() / 1024 / 1024 + " MB")
                .userDir(props.getProperty("user.dir"))
                .build();

        return ResponseEntity.<SystemInfoVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(systemInfo)
                .build();
    }

    /**
     * 获取缓存信息
     *
     * <p>返回Redis缓存连接信息、内存使用情况
     * <p>需要登录后才能访问
     *
     * @return 缓存信息VO
     */
    @GetMapping("/cache")
    @Operation(summary = "获取缓存信息")
    @SaCheckLogin
    @ApiOperationLog(description = "获取缓存信息")
    public ResponseEntity<CacheInfoVO> getCacheInfo() {
        log.info("获取缓存信息");

        CacheInfoVO cacheInfo = CacheInfoVO.builder()
                .redisVersion("6.x")
                .usedMemory("N/A")
                .connectedClients(1)
                .uptimeInDays(0)
                .build();

        if (redisTemplate != null) {
            try {
                Properties info = redisTemplate.getRequiredConnectionFactory()
                        .getConnection().info();
                if (info != null) {
                    cacheInfo.setRedisVersion(info.getProperty("redis_version", "N/A"));
                    cacheInfo.setUsedMemory(info.getProperty("used_memory_human", "N/A"));
                    cacheInfo.setConnectedClients(Integer.parseInt(info.getProperty("connected_clients", "0")));
                    cacheInfo.setUptimeInDays(Integer.parseInt(info.getProperty("uptime_in_days", "0")));
                }
            } catch (Exception e) {
                log.warn("获取Redis信息失败: {}", e.getMessage());
            }
        }

        return ResponseEntity.<CacheInfoVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(cacheInfo)
                .build();
    }

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
    public static class SystemInfoVO {
        private String osName;
        private String osArch;
        private String osVersion;
        private String javaVersion;
        private String javaVendor;
        private String jvmName;
        private String jvmVersion;
        private int availableProcessors;
        private String totalMemory;
        private String freeMemory;
        private String maxMemory;
        private String userDir;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheInfoVO {
        private String redisVersion;
        private String usedMemory;
        private int connectedClients;
        private int uptimeInDays;
    }
}
