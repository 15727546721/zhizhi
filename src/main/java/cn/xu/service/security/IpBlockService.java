package cn.xu.service.security;

import cn.xu.cache.core.RedisOperations;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * IP封禁服务
 * <p>提供IP黑名单管理、可疑行为记录等功能</p>
 * <p>支持自动封禁和手动解封</p>

 */
@Slf4j
@Service
@RequiredArgsConstructor
public class IpBlockService {

    private final RedisOperations redisOperations;

    /** Redis Key前缀 */
    private static final String KEY_PREFIX_BLOCKED = "ip:blocked:";
    private static final String KEY_PREFIX_SUSPICIOUS = "ip:suspicious:";
    private static final String KEY_PREFIX_WHITELIST = "ip:whitelist";
    private static final String KEY_PREFIX_PERMANENT_BLOCK = "ip:permanent:blocked";
    
    /** 可疑行为阈值 */
    @Value("${app.security.ip.suspicious-threshold:10}")
    private int suspiciousThreshold;
    
    /** 自动封禁时长（秒） */
    @Value("${app.security.ip.auto-block-duration:3600}")
    private long autoBlockDurationSeconds; // 默认1小时
    
    /** 可疑行为计数过期时间（秒） */
    @Value("${app.security.ip.suspicious-expire:3600}")
    private long suspiciousExpireSeconds; // 默认1小时

    /**
     * 检查IP是否被封禁
     * 
     * @param ip IP地址
     * @return true-已封禁，false-未封禁
     */
    public boolean isBlocked(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        
        // 检查白名单
        if (isInWhitelist(ip)) {
            return false;
        }
        
        // 检查永久封禁
        if (isPermanentBlocked(ip)) {
            return true;
        }
        
        // 检查临时封禁
        String blockedKey = KEY_PREFIX_BLOCKED + ip;
        return redisOperations.hasKey(blockedKey);
    }

    /**
     * 封禁IP（临时）
     * 
     * @param ip IP地址
     * @param durationSeconds 封禁时长（秒）
     * @param reason 封禁原因
     */
    public void blockIp(String ip, long durationSeconds, String reason) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        
        // 检查白名单
        if (isInWhitelist(ip)) {
            log.warn("[IP封禁] IP在白名单中，跳过封禁 - ip: {}", ip);
            return;
        }
        
        String blockedKey = KEY_PREFIX_BLOCKED + ip;
        String blockInfo = String.format("%s|%s|%s", 
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                reason,
                durationSeconds);
        
        redisOperations.set(blockedKey, blockInfo, durationSeconds);
        log.warn("[IP封禁] 封禁IP - ip: {}, duration: {}s, reason: {}", ip, durationSeconds, reason);
    }

    /**
     * 封禁IP（使用默认时长）
     * 
     * @param ip IP地址
     * @param reason 封禁原因
     */
    public void blockIp(String ip, String reason) {
        blockIp(ip, autoBlockDurationSeconds, reason);
    }

    /**
     * 解封IP
     * 
     * @param ip IP地址
     */
    public void unblockIp(String ip) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        
        String blockedKey = KEY_PREFIX_BLOCKED + ip;
        redisOperations.delete(blockedKey);
        
        // 同时从永久封禁列表移除
        redisOperations.sRemove(KEY_PREFIX_PERMANENT_BLOCK, ip);
        
        log.info("[IP封禁] 解封IP - ip: {}", ip);
    }

    /**
     * 永久封禁IP
     * 
     * @param ip IP地址
     * @param reason 封禁原因
     */
    public void permanentBlockIp(String ip, String reason) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        
        // 检查白名单
        if (isInWhitelist(ip)) {
            log.warn("[IP封禁] IP在白名单中，跳过永久封禁 - ip: {}", ip);
            return;
        }
        
        redisOperations.sAdd(KEY_PREFIX_PERMANENT_BLOCK, ip);
        log.warn("[IP封禁] 永久封禁IP - ip: {}, reason: {}", ip, reason);
    }

    /**
     * 检查是否永久封禁
     * 
     * @param ip IP地址
     * @return true-已永久封禁
     */
    public boolean isPermanentBlocked(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        return redisOperations.sIsMember(KEY_PREFIX_PERMANENT_BLOCK, ip);
    }

    /**
     * 记录可疑行为
     * 
     * <p>达到阈值后自动封禁
     * 
     * @param ip IP地址
     * @param activityType 行为类型
     */
    public void recordSuspiciousActivity(String ip, String activityType) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        
        // 检查白名单
        if (isInWhitelist(ip)) {
            return;
        }
        
        String suspiciousKey = KEY_PREFIX_SUSPICIOUS + ip;
        
        // 增加可疑行为计数
        long currentCount;
        Object countObj = redisOperations.get(suspiciousKey);
        if (countObj == null) {
            redisOperations.set(suspiciousKey, "1", suspiciousExpireSeconds);
            currentCount = 1;
        } else {
            currentCount = redisOperations.increment(suspiciousKey, 1);
        }
        
        log.debug("[IP封禁] 记录可疑行为 - ip: {}, type: {}, count: {}/{}", 
                ip, activityType, currentCount, suspiciousThreshold);
        
        // 检查是否达到封禁阈值
        if (currentCount >= suspiciousThreshold) {
            blockIp(ip, "auto_block_suspicious_activity:" + activityType);
            // 清除计数
            redisOperations.delete(suspiciousKey);
        }
    }

    /**
     * 添加IP到白名单
     * 
     * @param ip IP地址
     */
    public void addToWhitelist(String ip) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        redisOperations.sAdd(KEY_PREFIX_WHITELIST, ip);
        log.info("[IP封禁] 添加IP到白名单 - ip: {}", ip);
    }

    /**
     * 从白名单移除IP
     * 
     * @param ip IP地址
     */
    public void removeFromWhitelist(String ip) {
        if (ip == null || ip.isEmpty()) {
            return;
        }
        redisOperations.sRemove(KEY_PREFIX_WHITELIST, ip);
        log.info("[IP封禁] 从白名单移除IP - ip: {}", ip);
    }

    /**
     * 检查IP是否在白名单中
     * 
     * @param ip IP地址
     * @return true-在白名单中
     */
    public boolean isInWhitelist(String ip) {
        if (ip == null || ip.isEmpty()) {
            return false;
        }
        // localhost和内网IP默认在白名单
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip) || ip.startsWith("192.168.") || ip.startsWith("10.")) {
            return true;
        }
        return redisOperations.sIsMember(KEY_PREFIX_WHITELIST, ip);
    }

    /**
     * 获取封禁IP列表
     * 
     * @return 封禁的IP列表
     */
    public Set<String> getBlockedIps() {
        Set<Object> permanentBlocked = redisOperations.sMembers(KEY_PREFIX_PERMANENT_BLOCK);
        if (permanentBlocked == null || permanentBlocked.isEmpty()) {
            return Collections.emptySet();
        }
        return permanentBlocked.stream()
                .map(Object::toString)
                .collect(Collectors.toSet());
    }

    /**
     * 获取封禁信息
     * 
     * @param ip IP地址
     * @return 封禁信息，未封禁返回null
     */
    public String getBlockInfo(String ip) {
        if (ip == null || ip.isEmpty()) {
            return null;
        }
        String blockedKey = KEY_PREFIX_BLOCKED + ip;
        Object info = redisOperations.get(blockedKey);
        return info != null ? info.toString() : null;
    }
}