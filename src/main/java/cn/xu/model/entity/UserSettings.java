package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户设置PO（充血模型）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSettings implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private Long userId;
    
    /** 个人资料可见性: 1-公开 2-仅关注者可见 3-私密 */
    private Integer profileVisibility;
    
    /** 是否显示在线状态: 0-不显示 1-显示 */
    private Integer showOnlineStatus;
    
    /** 邮件通知: 0-关闭 1-开启 */
    private Integer emailNotification;
    
    /** 浏览器通知: 0-关闭 1-开启 */
    private Integer browserNotification;
    
    /** 消息提示音: 0-关闭 1-开启 */
    private Integer soundNotification;
    
    /** 邮箱是否已验证: 0-未验证 1-已验证 */
    private Integer emailVerified;
    
    /** 邮箱验证令牌 */
    private String emailVerifyToken;
    
    /** 邮箱验证令牌过期时间 */
    private LocalDateTime emailVerifyExpireTime;
    
    /** 密码重置令牌 */
    private String passwordResetToken;
    
    /** 密码重置令牌过期时间 */
    private LocalDateTime passwordResetExpireTime;
    
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // ========== 业务方法 ==========
    
    /**
     * 创建默认设置
     */
    public static UserSettings createDefault(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        return UserSettings.builder()
                .userId(userId)
                .profileVisibility(1)      // 默认公开
                .showOnlineStatus(1)       // 默认显示在线状态
                .emailNotification(1)      // 默认开启邮件通知
                .browserNotification(1)    // 默认开启浏览器通知
                .soundNotification(0)      // 默认关闭提示音
                .emailVerified(0)          // 默认未验证
                .createTime(now)
                .updateTime(now)
                .build();
    }
    
    /**
     * 更新隐私设置
     */
    public void updatePrivacySettings(Integer profileVisibility, Boolean showOnlineStatus) {
        if (profileVisibility != null) {
            this.profileVisibility = profileVisibility;
        }
        if (showOnlineStatus != null) {
            this.showOnlineStatus = showOnlineStatus ? 1 : 0;
        }
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新通知设置
     */
    public void updateNotificationSettings(Boolean emailNotification, Boolean browserNotification, Boolean soundNotification) {
        if (emailNotification != null) {
            this.emailNotification = emailNotification ? 1 : 0;
        }
        if (browserNotification != null) {
            this.browserNotification = browserNotification ? 1 : 0;
        }
        if (soundNotification != null) {
            this.soundNotification = soundNotification ? 1 : 0;
        }
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 检查邮箱验证令牌是否有效
     */
    public boolean isEmailVerifyTokenValid() {
        if (emailVerifyToken == null || emailVerifyExpireTime == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(emailVerifyExpireTime);
    }
    
    /**
     * 检查密码重置令牌是否有效
     */
    public boolean isPasswordResetTokenValid() {
        if (passwordResetToken == null || passwordResetExpireTime == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(passwordResetExpireTime);
    }
    
    // ========== 便捷的 Boolean 访问方法 ==========
    
    public Boolean getShowOnlineStatusBool() {
        return showOnlineStatus != null && showOnlineStatus == 1;
    }
    
    public Boolean getEmailNotificationBool() {
        return emailNotification != null && emailNotification == 1;
    }
    
    public Boolean getBrowserNotificationBool() {
        return browserNotification != null && browserNotification == 1;
    }
    
    public Boolean getSoundNotificationBool() {
        return soundNotification != null && soundNotification == 1;
    }
    
    public Boolean getEmailVerifiedBool() {
        return emailVerified != null && emailVerified == 1;
    }
}

