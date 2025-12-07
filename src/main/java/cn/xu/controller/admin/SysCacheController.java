package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 缓存管理控制器
 *
 * <p>提供Redis缓存查看、删除等管理功能</p>
 * <p>需要登录并拥有相应权限</p>
 
 */
@Slf4j
@RestController
@RequestMapping("/api/system/cache")
@Tag(name = "缓存管理", description = "缓存管理相关接口")
public class SysCacheController {

    @Autowired(required = false)
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 获取缓存信息
     *
     * <p>返回Redis服务器的基本信息，包括版本、内存、连接数等
     * <p>需要登录后才能访问
     *
     * @return Redis缓存信息
     */
    @GetMapping("/getCacheInfo")
    @Operation(summary = "获取缓存信息")
    @SaCheckLogin
    @ApiOperationLog(description = "获取缓存信息")
    public ResponseEntity<Map<String, Object>> getCacheInfo() {
        log.info("获取缓存信息");

        Map<String, Object> info = new HashMap<>();
        info.put("redisVersion", "6.x");
        info.put("usedMemory", "N/A");
        info.put("connectedClients", 0);
        info.put("uptimeInDays", 0);
        info.put("dbSize", 0);

        if (redisTemplate != null) {
            try {
                Properties redisInfo = redisTemplate.getRequiredConnectionFactory()
                        .getConnection().info();
                if (redisInfo != null) {
                    info.put("redisVersion", redisInfo.getProperty("redis_version", "N/A"));
                    info.put("usedMemory", redisInfo.getProperty("used_memory_human", "N/A"));
                    info.put("connectedClients", redisInfo.getProperty("connected_clients", "0"));
                    info.put("uptimeInDays", redisInfo.getProperty("uptime_in_days", "0"));
                }

                Long dbSize = redisTemplate.getRequiredConnectionFactory()
                        .getConnection().dbSize();
                info.put("dbSize", dbSize != null ? dbSize : 0);
            } catch (Exception e) {
                log.warn("获取Redis信息失败: {}", e.getMessage());
            }
        }

        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(info)
                .build();
    }

    /**
     * 获取缓存Key列表
     *
     * <p>分页查询缓存Key，支持模糊匹配
     * <p>需要system:cache:list权限
     *
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param pattern 匹配模式（可选）
     * @return 分页的Key列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取缓存Key列表")
    @SaCheckLogin
    @SaCheckPermission("system:cache:list")
    @ApiOperationLog(description = "获取缓存Key列表")
    public ResponseEntity<PageResponse<List<CacheKeyVO>>> getCacheList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "20") Integer pageSize,
            @RequestParam(required = false) String pattern) {
        log.info("获取缓存Key列表: pattern={}", pattern);

        List<CacheKeyVO> keys = new ArrayList<>();
        long total = 0;

        if (redisTemplate != null) {
            try {
                String searchPattern = (pattern != null && !pattern.isEmpty()) ? "*" + pattern + "*" : "*";
                Set<String> redisKeys = redisTemplate.keys(searchPattern);

                if (redisKeys != null) {
                    total = redisKeys.size();
                    keys = redisKeys.stream()
                            .skip((long) (pageNo - 1) * pageSize)
                            .limit(pageSize)
                            .map(key -> CacheKeyVO.builder()
                                    .key(key)
                                    .type(getKeyType(key))
                                    .build())
                            .collect(Collectors.toList());
                }
            } catch (Exception e) {
                log.warn("获取缓存Key列表失败: {}", e.getMessage());
            }
        }

        PageResponse<List<CacheKeyVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, total, keys);

        return ResponseEntity.<PageResponse<List<CacheKeyVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(pageResponse)
                .build();
    }

    /**
     * 获取缓存值
     *
     * <p>根据Key获取缓存的具体值
     * <p>需要system:cache:list权限
     *
     * @param key 缓存Key
     * @return 缓存值
     */
    @GetMapping("/getValue/{key}")
    @Operation(summary = "获取缓存值")
    @SaCheckLogin
    @SaCheckPermission("system:cache:list")
    @ApiOperationLog(description = "获取缓存值")
    public ResponseEntity<Object> getCacheValue(@PathVariable String key) {
        log.info("获取缓存值: key={}", key);

        Object value = null;
        if (redisTemplate != null) {
            try {
                value = redisTemplate.opsForValue().get(key);
            } catch (Exception e) {
                log.warn("获取缓存值失败: {}", e.getMessage());
            }
        }

        return ResponseEntity.<Object>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(value)
                .build();
    }

    /**
     * 删除缓存
     *
     * <p>根据Key删除指定缓存
     * <p>需要system:cache:delete权限
     *
     * @param key 缓存Key
     * @return 删除结果
     */
    @DeleteMapping("/delete/{key}")
    @Operation(summary = "删除缓存")
    @SaCheckLogin
    @SaCheckPermission("system:cache:delete")
    @ApiOperationLog(description = "删除缓存")
    public ResponseEntity<Void> deleteCache(@PathVariable String key) {
        log.info("删除缓存: key={}", key);

        if (redisTemplate != null) {
            try {
                redisTemplate.delete(key);
            } catch (Exception e) {
                log.warn("删除缓存失败: {}", e.getMessage());
            }
        }

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    /**
     * 获取缓存Key的类型
     *
     * @param key 缓存Key
     * @return 缓存类型
     */
    private String getKeyType(String key) {
        if (key.startsWith("post:")) return "帖子缓存";
        if (key.startsWith("user:")) return "用户缓存";
        if (key.startsWith("tag:")) return "标签缓存";
        if (key.startsWith("like:")) return "点赞缓存";
        if (key.startsWith("satoken:")) return "登录令牌";
        return "其他";
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CacheKeyVO {
        private String key;
        private String type;
        private Object value;
    }
}