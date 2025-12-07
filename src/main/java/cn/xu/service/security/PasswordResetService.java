package cn.xu.service.security;

import cn.xu.cache.RedisService;
import cn.xu.integration.mail.EmailService;
import cn.xu.model.entity.User;
import cn.xu.repository.IUserRepository;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 密码重置服务
 * <p>提供忘记密码、重置密码功能</p>
 * <p>支持邮件验证码方式重置</p>

 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PasswordResetService {

    private final RedisService redisService;
    private final EmailService emailService;
    private final IUserRepository userRepository;
    private final VerificationCodeService verificationCodeService;

    /** Redis Key前缀 */
    private static final String KEY_PREFIX_RESET_TOKEN = "password:reset:token:";
    private static final String KEY_PREFIX_RESET_EMAIL = "password:reset:email:";

    /** 重置令牌有效期（秒） */
    private static final long TOKEN_EXPIRE_SECONDS = 86400; // 24小时

    /** 每日重置次数限制 */
    private static final int DAILY_RESET_LIMIT = 3;
    private static final String KEY_PREFIX_DAILY_RESET = "password:reset:daily:";

    @Value("${app.frontend.reset-password-url:http://localhost:3000/reset-password}")
    private String resetPasswordUrl;

    /**
     * 发送密码重置邮件（令牌方式）
     *
     * @param email 注册邮箱
     * @throws BusinessException 当邮箱不存在或发送失败时抛出
     */
    public void sendResetEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(40001, "邮箱不能为空");
        }

        String trimmedEmail = email.trim().toLowerCase();

        // 1. 检查邮箱是否存在
        User user = userRepository.findByEmail(trimmedEmail).orElse(null);
        if (user == null) {
            // 为了安全，不暴露邮箱是否存在
            log.warn("[密码重置] 邮箱不存在 - email: {}", maskEmail(trimmedEmail));
            // 仍然返回成功，防止邮箱枚举攻击
            return;
        }

        // 2. 检查每日重置次数
        String dailyKey = KEY_PREFIX_DAILY_RESET + trimmedEmail;
        Object countObj = redisService.get(dailyKey);
        int currentCount = countObj != null ? Integer.parseInt(countObj.toString()) : 0;
        if (currentCount >= DAILY_RESET_LIMIT) {
            throw new BusinessException(40003, "今日密码重置次数已达上限，请明天再试");
        }

        // 3. 生成重置令牌
        String token = UUID.randomUUID().toString().replace("-", "");

        // 4. 存储令牌（token -> email 映射）
        String tokenKey = KEY_PREFIX_RESET_TOKEN + token;
        redisService.set(tokenKey, trimmedEmail, TOKEN_EXPIRE_SECONDS);

        // 5. 增加每日重置计数
        if (currentCount == 0) {
            redisService.set(dailyKey, "1", 86400); // 设置24小时过期
        } else {
            redisService.incr(dailyKey, 1);
        }

        // 6. 发送重置邮件
        try {
            emailService.sendPasswordResetEmail(trimmedEmail, token, resetPasswordUrl);
            log.info("[密码重置] 发送重置邮件成功 - email: {}", maskEmail(trimmedEmail));
        } catch (Exception e) {
            // 发送失败，清除令牌
            redisService.del(tokenKey);
            log.error("[密码重置] 发送重置邮件失败 - email: {}", maskEmail(trimmedEmail), e);
            throw new BusinessException(50001, "邮件发送失败，请稍后重试");
        }
    }

    /**
     * 发送密码重置验证码
     *
     * @param email 注册邮箱
     * @throws BusinessException 当邮箱不存在或发送失败时抛出
     */
    public void sendResetCode(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(40001, "邮箱不能为空");
        }

        String trimmedEmail = email.trim().toLowerCase();

        // 检查邮箱是否存在
        User user = userRepository.findByEmail(trimmedEmail).orElse(null);
        if (user == null) {
            // 为了安全，不暴露邮箱是否存在
            log.warn("[密码重置] 邮箱不存在 - email: {}", maskEmail(trimmedEmail));
            throw new BusinessException(40401, "该邮箱未注册");
        }

        // 发送验证码
        verificationCodeService.sendVerifyCode(trimmedEmail, VerificationCodeService.CodeScene.FORGOT_PASSWORD);
    }

    /**
     * 通过令牌重置密码
     *
     * @param token 重置令牌
     * @param newPassword 新密码
     * @throws BusinessException 当令牌无效或已过期时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByToken(String token, String newPassword) {
        if (token == null || token.trim().isEmpty()) {
            throw new BusinessException(40001, "重置令牌不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BusinessException(40001, "新密码不能为空");
        }

        String trimmedToken = token.trim();

        // 1. 验证令牌
        String tokenKey = KEY_PREFIX_RESET_TOKEN + trimmedToken;
        Object emailObj = redisService.get(tokenKey);
        if (emailObj == null) {
            throw new BusinessException(40004, "重置链接无效或已过期");
        }

        String email = emailObj.toString();

        // 2. 查找用户
        User user = userRepository.findByEmail(email).orElse(null);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        // 3. 更新密码
        user.updatePassword(newPassword);
        userRepository.save(user);

        // 4. 删除令牌（一次性使用）
        redisService.del(tokenKey);

        log.info("[密码重置] 密码重置成功 - email: {}", maskEmail(email));
    }

    /**
     * 通过验证码重置密码
     *
     * @param email 邮件
     * @param code 验证码
     * @param newPassword 新密码
     * @throws BusinessException 当验证码错误或已过期时抛出
     */
    @Transactional(rollbackFor = Exception.class)
    public void resetPasswordByCode(String email, String code, String newPassword) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(40001, "邮箱不能为空");
        }
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException(40001, "验证码不能为空");
        }
        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BusinessException(40001, "新密码不能为空");
        }

        String trimmedEmail = email.trim().toLowerCase();

        // 1. 验证验证码
        verificationCodeService.verifyCodeOrThrow(trimmedEmail, code, VerificationCodeService.CodeScene.FORGOT_PASSWORD);

        // 2. 查找用户
        User user = userRepository.findByEmail(trimmedEmail).orElse(null);
        if (user == null) {
            throw new BusinessException(40401, "用户不存在");
        }

        // 3. 更新密码
        user.updatePassword(newPassword);
        userRepository.save(user);

        log.info("[密码重置] 密码重置成功（验证码方式） - email: {}", maskEmail(trimmedEmail));
    }

    /**
     * 验证重置令牌是否有效
     *
     * @param token 重置令牌
     * @return true-有效，false-无效
     */
    public boolean isTokenValid(String token) {
        if (token == null || token.trim().isEmpty()) {
            return false;
        }
        String tokenKey = KEY_PREFIX_RESET_TOKEN + token.trim();
        return redisService.hasKey(tokenKey);
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