package cn.xu.service.security;

import cn.xu.cache.RedisService;
import cn.xu.integration.mail.EmailService;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * 验证码服务
 *
 * <p>提供邮箱验证码的生成、存储、验证功能</p>
 * <p>支持多种场景：注册、忘记密码、绑定邮箱等</p>
 
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class VerificationCodeService {

    private final RedisService redisService;
    private final EmailService emailService;

    /** 验证码长度 */
    private static final int CODE_LENGTH = 6;

    /** 验证码有效期（秒） */
    private static final long CODE_EXPIRE_SECONDS = 300; // 5分钟

    /** 发送间隔限制（秒） */
    private static final long SEND_INTERVAL_SECONDS = 60; // 1分钟内不能重复发送

    /** 每日发送次数限制 */
    private static final int DAILY_SEND_LIMIT = 10;

    /** Redis Key前缀 */
    private static final String KEY_PREFIX_CODE = "verify:code:";
    private static final String KEY_PREFIX_INTERVAL = "verify:interval:";
    private static final String KEY_PREFIX_DAILY_COUNT = "verify:daily:";

    @Value("${app.security.verify-code.enabled:true}")
    private boolean verifyCodeEnabled;

    /**
     * 验证码场景枚举
     */
    public enum CodeScene {
        /** 注册 */
        REGISTER("register"),
        /** 登录 */
        LOGIN("login"),
        /** 忘记密码 */
        FORGOT_PASSWORD("forgot"),
        /** 绑定邮箱 */
        BIND_EMAIL("bind"),
        /** 通用验证 */
        GENERAL("general");

        private final String code;

        CodeScene(String code) {
            this.code = code;
        }

        public String getCode() {
            return code;
        }
    }

    /**
     * 发送验证码邮件
     *
     * @param email 目标邮箱
     * @param scene 使用场景
     * @throws BusinessException 当发送频率过高或发送失败时抛出
     */
    public void sendVerifyCode(String email, CodeScene scene) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(40001, "邮箱不能为空");
        }

        String trimmedEmail = email.trim().toLowerCase();
        String sceneCode = scene.getCode();

        // 1. 检查发送间隔限制
        String intervalKey = KEY_PREFIX_INTERVAL + sceneCode + ":" + trimmedEmail;
        if (redisService.hasKey(intervalKey)) {
            long ttl = redisService.getExpire(intervalKey);
            throw new BusinessException(40002, "发送过于频繁，" + ttl + "秒后再试");
        }

        // 2. 检查每日发送次数限制
        String dailyCountKey = KEY_PREFIX_DAILY_COUNT + sceneCode + ":" + trimmedEmail;
        Object countObj = redisService.get(dailyCountKey);
        int currentCount = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
        if (currentCount >= DAILY_SEND_LIMIT) {
            throw new BusinessException(40003, "今日发送次数已达上限，请明天再试");
        }

        // 3. 生成验证码
        String code = generateCode();

        // 4. 存储验证码到Redis
        String codeKey = KEY_PREFIX_CODE + sceneCode + ":" + trimmedEmail;
        redisService.set(codeKey, code, CODE_EXPIRE_SECONDS);

        // 5. 设置发送间隔限制
        redisService.set(intervalKey, "1", SEND_INTERVAL_SECONDS);

        // 6. 增加每日发送计数
        if (currentCount == 0) {
            // 第一次发送，设置24小时过期
            redisService.set(dailyCountKey, "1", 86400);
        } else {
            // 增加计数
            redisService.incr(dailyCountKey, 1);
        }

        // 7. 发送邮件
        try {
            emailService.sendVerifyCode(trimmedEmail, code);
            log.info("[验证码服务] 发送验证码成功 - email: {}, scene: {}", maskEmail(trimmedEmail), sceneCode);
        } catch (Exception e) {
            // 发送失败，清除已存储的验证码
            redisService.del(codeKey);
            log.error("[验证码服务] 发送验证码失败 - email: {}, scene: {}", maskEmail(trimmedEmail), sceneCode, e);
            throw new BusinessException(50001, "验证码发送失败，请稍后重试");
        }
    }

    /**
     * 验证验证码
     *
     * @param email 邮件
     * @param code 用户输入的验证码
     * @param scene 使用场景
     * @return true-验证成功，false-验证失败
     */
    public boolean verifyCode(String email, String code, CodeScene scene) {
        // 如果验证码功能未启用，直接返回true（方便开发测试）
        if (!verifyCodeEnabled) {
            log.warn("[验证码服务] 验证码功能已禁用，跳过验证");
            return true;
        }

        if (email == null || code == null) {
            return false;
        }

        String trimmedEmail = email.trim().toLowerCase();
        String trimmedCode = code.trim();
        String sceneCode = scene.getCode();

        String codeKey = KEY_PREFIX_CODE + sceneCode + ":" + trimmedEmail;
        Object storedCodeObj = redisService.get(codeKey);

        if (storedCodeObj == null) {
            log.warn("[验证码服务] 验证码不存在或已过期 - email: {}, scene: {}", maskEmail(trimmedEmail), sceneCode);
            return false;
        }

        String storedCode = storedCodeObj.toString();
        boolean isValid = storedCode.equals(trimmedCode);

        if (isValid) {
            // 验证成功后删除验证码（一次性使用）
            redisService.del(codeKey);
            log.info("[验证码服务] 验证码验证成功 - email: {}, scene: {}", maskEmail(trimmedEmail), sceneCode);
        } else {
            log.warn("[验证码服务] 验证码验证失败 - email: {}, scene: {}", maskEmail(trimmedEmail), sceneCode);
        }

        return isValid;
    }

    /**
     * 验证验证码（失败时抛出异常）
     *
     * @param email 邮件
     * @param code 用户输入的验证码
     * @param scene 使用场景
     * @throws BusinessException 当验证失败时抛出
     */
    public void verifyCodeOrThrow(String email, String code, CodeScene scene) {
        if (!verifyCode(email, code, scene)) {
            throw new BusinessException(40004, "验证码错误或已过期");
        }
    }

    /**
     * 生成6位数字验证码
     */
    private String generateCode() {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++) {
            sb.append(random.nextInt(10));
        }
        return sb.toString();
    }

    /**
     * 邮箱脱敏（用于日志）
     */
    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        int atIndex = email.indexOf("@");
        if (atIndex <= 2) {
            return "***" + email.substring(atIndex);
        }
        return email.substring(0, 2) + "***" + email.substring(atIndex);
    }
}