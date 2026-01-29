package cn.xu.service.security;

import cn.xu.cache.core.RedisOperations;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 登录安全服务
 *
 * <p>提供登录失败次数限制、账户锁定等安全功能</p>
 * <p>防止暴力破解攻击</p>
 
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LoginSecurityService {

    private final RedisOperations redisOperations;
    private final IpBlockService ipBlockService;

    /** Redis Key前缀 */
    private static final String KEY_PREFIX_FAIL_COUNT = "login:fail:";
    private static final String KEY_PREFIX_LOCK = "login:lock:";

    /** 最大失败次数 */
    @Value("${app.security.login.max-fail-count:5}")
    private int maxFailCount;

    /** 锁定时间（秒） */
    @Value("${app.security.login.lock-duration:1800}")
    private long lockDurationSeconds; // 默认30分钟

    /** 失败计数过期时间（秒） */
    @Value("${app.security.login.fail-count-expire:3600}")
    private long failCountExpireSeconds; // 默认1小时

    /**
     * 检查登录前置条件
     *
     * <p>检查IP是否被封禁、账户是否被锁定</p>
     *
     * @param identifier 登录标识
     * @param clientIp 客户端IP
     * @throws BusinessException 登录失败异常
     */
    public void checkBeforeLogin(String identifier, String clientIp) {
        // 1. 检查IP是否被封禁
        if (ipBlockService.isBlocked(clientIp)) {
            log.warn("[登录安全] IP已被封禁 - ip: {}", clientIp);
            throw new BusinessException(40301, "您的IP已被限制访问，请稍后再试");
        }

        // 2. 检查账户是否被锁定
        String lockKey = KEY_PREFIX_LOCK + identifier.toLowerCase();
        if (redisOperations.hasKey(lockKey)) {
            long ttl = redisOperations.getExpire(lockKey);
            int minutes = (int) Math.ceil(ttl / 60.0);
            log.warn("[登录安全] 账户已被锁定 - identifier: {}, remainingMinutes: {}", maskIdentifier(identifier), minutes);
            throw new BusinessException(40302, "账户已被锁定，请" + minutes + "分钟后重试");
        }
    }

    /**
     * 记录登录失败
     *
     * <p>增加失败次数，达到阈值后锁定账户
     *
     * @param identifier 登录标识（邮箱或用户名）
     * @param clientIp 客户端IP
     * @return 剩余尝试次数
     */
    public int recordLoginFailure(String identifier, String clientIp) {
        String lowerIdentifier = identifier.toLowerCase();
        String failCountKey = KEY_PREFIX_FAIL_COUNT + lowerIdentifier;

        // 增加失败次数
        long currentCount;
        Object countObj = redisOperations.get(failCountKey);
        if (countObj == null) {
            // 第一次失败
            redisOperations.set(failCountKey, "1", failCountExpireSeconds);
            currentCount = 1;
        } else {
            currentCount = redisOperations.increment(failCountKey, 1);
        }

        log.warn("[登录安全] 登录失败 - identifier: {}, ip: {}, failCount: {}/{}",
                maskIdentifier(identifier), clientIp, currentCount, maxFailCount);

        // 检查是否达到锁定阈值
        if (currentCount >= maxFailCount) {
            // 锁定账户
            String lockKey = KEY_PREFIX_LOCK + lowerIdentifier;
            redisOperations.set(lockKey, "1", lockDurationSeconds);

            // 清除失败计数
            redisOperations.delete(failCountKey);

            // 记录IP异常行为
            ipBlockService.recordSuspiciousActivity(clientIp, "login_brute_force");

            log.warn("[登录安全] 账户已锁定 - identifier: {}, lockDuration: {}s",
                    maskIdentifier(identifier), lockDurationSeconds);

            return 0;
        }

        return maxFailCount - (int) currentCount;
    }

    /**
     * 清除登录失败记录
     *
     * <p>登录成功后调用，清除该账户的失败计数
     *
     * @param identifier 登录标识
     */
    public void clearLoginFailure(String identifier) {
        String failCountKey = KEY_PREFIX_FAIL_COUNT + identifier.toLowerCase();
        redisOperations.delete(failCountKey);
        log.debug("[登录安全] 清除登录失败记录 - identifier: {}", maskIdentifier(identifier));
    }

    /**
     * 获取剩余尝试次数
     *
     * @param identifier 登录标识
     * @return 剩余尝试次数
     */
    public int getRemainingAttempts(String identifier) {
        String failCountKey = KEY_PREFIX_FAIL_COUNT + identifier.toLowerCase();
        Object countObj = redisOperations.get(failCountKey);
        if (countObj == null) {
            return maxFailCount;
        }
        int failCount = Integer.parseInt(countObj.toString());
        return Math.max(0, maxFailCount - failCount);
    }

    /**
     * 检查账户是否被锁定
     *
     * @param identifier 登录标识
     * @return true-已锁定，false-未锁定
     */
    public boolean isAccountLocked(String identifier) {
        String lockKey = KEY_PREFIX_LOCK + identifier.toLowerCase();
        return redisOperations.hasKey(lockKey);
    }

    /**
     * 手动解锁账户（管理员操作）
     *
     * @param identifier 登录标识
     */
    public void unlockAccount(String identifier) {
        String lockKey = KEY_PREFIX_LOCK + identifier.toLowerCase();
        String failCountKey = KEY_PREFIX_FAIL_COUNT + identifier.toLowerCase();
        redisOperations.delete(lockKey);
        redisOperations.delete(failCountKey);
        log.info("[登录安全] 管理员解锁账户 - identifier: {}", maskIdentifier(identifier));
    }

    /**
     * 标识脱敏（用于日志）
     */
    private String maskIdentifier(String identifier) {
        if (identifier == null || identifier.length() <= 4) {
            return "***";
        }
        if (identifier.contains("@")) {
            // 邮箱脱敏
            int atIndex = identifier.indexOf("@");
            if (atIndex <= 2) {
                return "***" + identifier.substring(atIndex);
            }
            return identifier.substring(0, 2) + "***" + identifier.substring(atIndex);
        }
        // 用户名脱敏
        return identifier.substring(0, 2) + "***" + identifier.substring(identifier.length() - 2);
    }
}