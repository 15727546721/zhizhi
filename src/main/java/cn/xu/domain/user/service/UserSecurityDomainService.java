package cn.xu.domain.user.service;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.valobj.Password;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.regex.Pattern;

/**
 * 用户安全领域服务
 * 处理跨实体的用户安全相关业务逻辑
 */
@Slf4j
@Service
public class UserSecurityDomainService {

    private static final int MAX_LOGIN_ATTEMPTS = 5;
    private static final int LOCKOUT_DURATION_MINUTES = 30;
    private static final Pattern STRONG_PASSWORD_PATTERN = 
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");

    /**
     * 验证用户登录安全性
     * @param userEntity 用户实体
     * @param inputPassword 输入的密码
     * @return 是否验证通过
     */
    public boolean validateLoginSecurity(UserEntity userEntity, String inputPassword) {
        if (userEntity == null || inputPassword == null) {
            return false;
        }

        // 检查账户状态
        if (userEntity.isLocked()) {
            throw new BusinessException("账户已被锁定，请稍后再试");
        }

        if (userEntity.isBanned()) {
            throw new BusinessException("账户已被封禁");
        }

        // 验证密码
        if (!userEntity.validatePassword(inputPassword)) {
            // 记录失败尝试
            userEntity.recordFailedLogin();
            
            // 检查是否需要锁定账户
            if (userEntity.getFailedLoginAttempts() >= MAX_LOGIN_ATTEMPTS) {
                userEntity.lockAccount(LOCKOUT_DURATION_MINUTES);
                log.warn("用户账户因多次登录失败被锁定 - userId: {}", userEntity.getId());
                throw new BusinessException("登录失败次数过多，账户已被锁定30分钟");
            }
            
            return false;
        }

        // 登录成功，重置失败计数
        userEntity.resetFailedLoginAttempts();
        return true;
    }

    /**
     * 验证密码强度
     * @param password 密码
     * @return 是否为强密码
     */
    public boolean isStrongPassword(Password password) {
        if (password == null) {
            return false;
        }
        
        String passwordValue = password.getValue();
        return STRONG_PASSWORD_PATTERN.matcher(passwordValue).matches();
    }

    /**
     * 检查密码是否需要更新
     * @param userEntity 用户实体
     * @return 是否需要更新密码
     */
    public boolean shouldUpdatePassword(UserEntity userEntity) {
        if (userEntity.getPasswordUpdateTime() == null) {
            return true; // 从未更新过密码
        }

        // 检查密码是否超过90天未更新
        LocalDateTime lastUpdate = userEntity.getPasswordUpdateTime();
        long daysSinceUpdate = ChronoUnit.DAYS.between(lastUpdate, LocalDateTime.now());
        
        return daysSinceUpdate > 90;
    }

    /**
     * 验证用户操作权限
     * @param operatorUser 操作者
     * @param targetUser 目标用户
     * @param operation 操作类型
     * @return 是否有权限
     */
    public boolean hasPermissionForUserOperation(UserEntity operatorUser, UserEntity targetUser, String operation) {
        if (operatorUser == null || targetUser == null) {
            return false;
        }

        // 用户只能操作自己的数据
        if (operatorUser.getId().equals(targetUser.getId())) {
            return true;
        }

        // 管理员可以操作所有用户（除了超级管理员）
        if (operatorUser.isAdmin() && !targetUser.isSuperAdmin()) {
            return true;
        }

        // 超级管理员可以操作所有用户
        if (operatorUser.isSuperAdmin()) {
            return true;
        }

        return false;
    }

    /**
     * 检查用户行为是否异常
     * @param userEntity 用户实体
     * @param actionType 行为类型
     * @return 是否异常
     */
    public boolean isAbnormalBehavior(UserEntity userEntity, String actionType) {
        if (userEntity == null) {
            return true;
        }

        // 检查账户状态
        if (userEntity.isBanned() || userEntity.isLocked()) {
            return true;
        }

        // 检查操作频率（简化实现）
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime lastActionTime = userEntity.getLastActionTime();
        
        if (lastActionTime != null) {
            long secondsSinceLastAction = ChronoUnit.SECONDS.between(lastActionTime, now);
            
            // 如果操作间隔少于1秒，可能是机器人行为
            if (secondsSinceLastAction < 1) {
                log.warn("检测到可能的机器人行为 - userId: {}, actionType: {}", 
                        userEntity.getId(), actionType);
                return true;
            }
        }

        return false;
    }

    /**
     * 生成安全的随机密码
     * @return 随机密码
     */
    public String generateSecurePassword() {
        // 简化实现，实际应该使用更安全的随机数生成器
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789@$!%*?&";
        StringBuilder password = new StringBuilder();
        
        for (int i = 0; i < 12; i++) {
            int index = (int) (Math.random() * chars.length());
            password.append(chars.charAt(index));
        }
        
        return password.toString();
    }
}