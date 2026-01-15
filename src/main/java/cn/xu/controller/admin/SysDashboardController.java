package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.cache.core.RedisOperations;
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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.Resource;
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
    private RedisOperations redisOps;

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
    public ResponseEntity<ServerInfoVO> getSystemInfo() {
        log.info("获取系统信息");

        RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
        OperatingSystemMXBean osMXBean = ManagementFactory.getOperatingSystemMXBean();
        Properties props = System.getProperties();
        Runtime runtime = Runtime.getRuntime();

        // CPU信息
        CpuInfo cpu = new CpuInfo();
        cpu.setCpuNum(osMXBean.getAvailableProcessors());
        cpu.setUsed(0.0); // 需要额外计算
        cpu.setSys(0.0);
        cpu.setFree(100.0);

        // 内存信息
        MemInfo mem = new MemInfo();
        long totalMem = 0;
        long freeMem = 0;
        try {
            if (osMXBean instanceof com.sun.management.OperatingSystemMXBean) {
                com.sun.management.OperatingSystemMXBean sunOsMXBean = (com.sun.management.OperatingSystemMXBean) osMXBean;
                totalMem = sunOsMXBean.getTotalPhysicalMemorySize();
                freeMem = sunOsMXBean.getFreePhysicalMemorySize();
            }
        } catch (Exception e) {
            log.warn("获取系统内存信息失败");
        }
        mem.setTotal(String.format("%.2f", totalMem / 1024.0 / 1024.0 / 1024.0));
        mem.setUsed(String.format("%.2f", (totalMem - freeMem) / 1024.0 / 1024.0 / 1024.0));
        mem.setFree(String.format("%.2f", freeMem / 1024.0 / 1024.0 / 1024.0));
        mem.setUsage(totalMem > 0 ? (double) (totalMem - freeMem) * 100 / totalMem : 0);

        // JVM信息
        JvmInfo jvm = new JvmInfo();
        jvm.setTotal(String.valueOf(runtime.totalMemory() / 1024 / 1024));
        jvm.setUsed(String.valueOf((runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024));
        jvm.setFree(String.valueOf(runtime.freeMemory() / 1024 / 1024));
        jvm.setUsage(runtime.totalMemory() > 0 ? (double) (runtime.totalMemory() - runtime.freeMemory()) * 100 / runtime.totalMemory() : 0);
        jvm.setName(runtimeMXBean.getVmName());
        jvm.setVersion(props.getProperty("java.version"));
        jvm.setHome(props.getProperty("java.home"));
        
        // 计算运行时长
        long uptime = runtimeMXBean.getUptime();
        long days = uptime / (1000 * 60 * 60 * 24);
        long hours = (uptime % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (uptime % (1000 * 60 * 60)) / (1000 * 60);
        jvm.setRunTime(days + "天" + hours + "小时" + minutes + "分钟");
        jvm.setStartTime(java.time.LocalDateTime.ofInstant(
                java.time.Instant.ofEpochMilli(runtimeMXBean.getStartTime()),
                java.time.ZoneId.systemDefault()).toString().replace("T", " "));

        // 系统信息
        SysInfo sys = new SysInfo();
        sys.setComputerName(getHostName());
        sys.setComputerIp(getHostIp());
        sys.setOsName(props.getProperty("os.name"));
        sys.setOsArch(props.getProperty("os.arch"));
        sys.setUserDir(props.getProperty("user.dir"));

        // 磁盘信息
        java.util.List<SysFileInfo> sysFiles = new java.util.ArrayList<>();
        java.io.File[] roots = java.io.File.listRoots();
        for (java.io.File root : roots) {
            SysFileInfo sysFile = new SysFileInfo();
            sysFile.setDirName(root.getPath());
            sysFile.setSysTypeName(System.getProperty("os.name"));
            sysFile.setTypeName("本地磁盘");
            sysFile.setTotal(formatBytes(root.getTotalSpace()));
            sysFile.setFree(formatBytes(root.getFreeSpace()));
            sysFile.setUsed(formatBytes(root.getTotalSpace() - root.getFreeSpace()));
            sysFile.setUsage(root.getTotalSpace() > 0 ? 
                    (double) (root.getTotalSpace() - root.getFreeSpace()) * 100 / root.getTotalSpace() : 0);
            sysFiles.add(sysFile);
        }

        ServerInfoVO serverInfo = ServerInfoVO.builder()
                .cpu(cpu)
                .mem(mem)
                .jvm(jvm)
                .sys(sys)
                .sysFiles(sysFiles)
                .build();

        return ResponseEntity.<ServerInfoVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(serverInfo)
                .build();
    }

    private String getHostName() {
        try {
            return java.net.InetAddress.getLocalHost().getHostName();
        } catch (Exception e) {
            return "未知";
        }
    }

    private String getHostIp() {
        try {
            return java.net.InetAddress.getLocalHost().getHostAddress();
        } catch (Exception e) {
            return "127.0.0.1";
        }
    }

    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.2f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / 1024.0 / 1024.0);
        return String.format("%.2f GB", bytes / 1024.0 / 1024.0 / 1024.0);
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

        if (redisOps != null) {
            try {
                Properties info = redisOps.getRedisTemplate().getRequiredConnectionFactory()
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
    public static class ServerInfoVO {
        private CpuInfo cpu;
        private MemInfo mem;
        private JvmInfo jvm;
        private SysInfo sys;
        private java.util.List<SysFileInfo> sysFiles;
    }

    @Data
    public static class CpuInfo {
        private int cpuNum;
        private double used;
        private double sys;
        private double free;
    }

    @Data
    public static class MemInfo {
        private String total;
        private String used;
        private String free;
        private double usage;
    }

    @Data
    public static class JvmInfo {
        private String total;
        private String used;
        private String free;
        private double usage;
        private String name;
        private String version;
        private String home;
        private String startTime;
        private String runTime;
    }

    @Data
    public static class SysInfo {
        private String computerName;
        private String computerIp;
        private String osName;
        private String osArch;
        private String userDir;
    }

    @Data
    public static class SysFileInfo {
        private String dirName;
        private String sysTypeName;
        private String typeName;
        private String total;
        private String free;
        private String used;
        private double usage;
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
