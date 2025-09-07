package cn.xu.domain.user.model.entity;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.user.model.valobj.Email;
import cn.xu.domain.user.model.valobj.Password;
import cn.xu.domain.user.model.valobj.Phone;
import cn.xu.domain.user.model.valobj.Username;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户领域实体
 * 封装用户相关的业务逻辑和规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserEntity {
    private Long id;
    private Username username;
    private Password password;
    private String nickname;
    private Email email;
    private String avatar;
    private UserStatus status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Integer gender;
    private Phone phone;
    private String region;
    private String birthday;
    private String description;
    private Integer followCount; // 关注数量
    private Integer fansCount;   // 粉丝数量
    private Integer likeCount;   // 获赞数量
    private LocalDateTime lastLoginTime; // 最后登录时间
    private String lastLoginIp; // 最后登录IP
    private Integer failedLoginAttempts; // 登录失败次数
    private LocalDateTime lockoutEndTime; // 锁定结束时间
    private LocalDateTime passwordUpdateTime; // 密码更新时间
    private LocalDateTime lastActionTime; // 最后操作时间

    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        NORMAL(0, "正常"),
        BANNED(1, "封禁"),
        PENDING(2, "待审核");

        private final int code;
        private final String desc;

        UserStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public int getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

        public static UserStatus fromCode(Integer code) {
            if (code == null) return NORMAL;
            for (UserStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            return NORMAL;
        }
    }

    // ==================== 业务方法 ====================

    /**
     * 创建新用户
     */
    public static UserEntity createNewUser(String username, String password, String email, String nickname) {
        return UserEntity.builder()
                .username(new Username(username))
                .password(new Password(password))
                .email(new Email(email))
                .nickname(nickname)
                .status(UserStatus.NORMAL)
                .followCount(0)
                .fansCount(0)
                .likeCount(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    /**
     * 验证密码
     */
    public boolean validatePassword(String inputPassword) {
        if (this.password == null) {
            return false;
        }
        return Password.matches(inputPassword, this.password.getValue());
    }

    /**
     * 更新密码
     */
    public void updatePassword(String oldPassword, String newPassword) {
        if (!validatePassword(oldPassword)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "原密码错误");
        }
        this.password = new Password(newPassword);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 封禁用户
     */
    public void ban() {
        if (this.status == UserStatus.BANNED) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户已被封禁");
        }
        this.status = UserStatus.BANNED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 解封用户
     */
    public void unban() {
        if (this.status != UserStatus.BANNED) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未被封禁");
        }
        this.status = UserStatus.NORMAL;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 判断用户是否被封禁
     */
    public boolean isBanned() {
        return this.status == UserStatus.BANNED;
    }

    /**
     * 判断用户是否正常
     */
    public boolean isNormal() {
        return this.status == UserStatus.NORMAL;
    }

    /**
     * 增加关注数
     */
    public void increaseFollowCount() {
        this.followCount = (this.followCount == null ? 0 : this.followCount) + 1;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 减少关注数
     */
    public void decreaseFollowCount() {
        this.followCount = Math.max(0, (this.followCount == null ? 0 : this.followCount) - 1);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 增加粉丝数
     */
    public void increaseFansCount() {
        this.fansCount = (this.fansCount == null ? 0 : this.fansCount) + 1;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 减少粉丝数
     */
    public void decreaseFansCount() {
        this.fansCount = Math.max(0, (this.fansCount == null ? 0 : this.fansCount) - 1);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 增加获赞数
     */
    public void increaseLikeCount() {
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 减少获赞数
     */
    public void decreaseLikeCount() {
        this.likeCount = Math.max(0, (this.likeCount == null ? 0 : this.likeCount) - 1);
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新最后登录信息
     */
    public void updateLastLogin(String ip) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新个人信息
     */
    public void updateProfile(String nickname, String region, String birthday, String description, String phone) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname.trim();
        }
        
        if (phone != null && !phone.trim().isEmpty()) {
            this.phone = new Phone(phone);
        }
        
        this.region = region;
        this.birthday = birthday;
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新头像
     */
    public void updateAvatar(String avatarUrl) {
        if (avatarUrl == null || avatarUrl.trim().isEmpty()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "头像地址不能为空");
        }
        this.avatar = avatarUrl;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 验证用户是否可以执行某个操作
     */
    public void validateCanPerformAction() {
        if (isBanned()) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "账户已被封禁，无法执行此操作");
        }
        if (this.status == UserStatus.PENDING) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "账户待审核，无法执行此操作");
        }
    }

    // ==================== 兼容性方法 ====================
    
    /**
     * 获取用户名字符串（兼容现有代码）
     */
    public String getUsernameValue() {
        return username != null ? username.getValue() : null;
    }
    
    /**
     * 获取邮箱字符串（兼容现有代码）
     */
    public String getEmailValue() {
        return email != null ? email.getValue() : null;
    }
    
    /**
     * 获取手机号字符串（兼容现有代码）
     */
    public String getPhoneValue() {
        return phone != null ? phone.getValue() : null;
    }
    
    /**
     * 获取状态代码（兼容现有代码）
     */
    public Integer getStatusCode() {
        return status != null ? status.getCode() : 0;
    }
    
    /**
     * 设置状态（兼容现有代码）
     */
    public void setStatusByCode(Integer statusCode) {
        this.status = UserStatus.fromCode(statusCode);
        this.updateTime = LocalDateTime.now();
    }
    
    // ==================== 安全相关方法 ====================
    
    /**
     * 锁定账户
     */
    public void lockAccount(int lockoutDurationMinutes) {
        this.lockoutEndTime = LocalDateTime.now().plusMinutes(lockoutDurationMinutes);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 重置失败登录次数
     */
    public void resetFailedLoginAttempts() {
        this.failedLoginAttempts = 0;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 获取密码更新时间
     */
    public LocalDateTime getPasswordUpdateTime() {
        return passwordUpdateTime;
    }
    
    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        // 这里可以根据实际业务需求设置管理员判断逻辑
        // 例如根据用户角色或特殊标识
        return false; // 默认非管理员
    }
    
    /**
     * 判断是否为超级管理员
     */
    public boolean isSuperAdmin() {
        // 这里可以根据实际业务需求设置超级管理员判断逻辑
        return false; // 默认非超级管理员
    }
    
    /**
     * 判断账户是否被锁定
     */
    public boolean isLocked() {
        if (lockoutEndTime == null) {
            return false;
        }
        return LocalDateTime.now().isBefore(lockoutEndTime);
    }
    
    /**
     * 获取最后操作时间
     */
    public LocalDateTime getLastActionTime() {
        return lastActionTime;
    }
    
    /**
     * 获取失败登录次数
     */
    public Integer getFailedLoginAttempts() {
        return failedLoginAttempts != null ? failedLoginAttempts : 0;
    }
    
    /**
     * 记录失败登录
     */
    public void recordFailedLogin() {
        this.failedLoginAttempts = getFailedLoginAttempts() + 1;
        this.updateTime = LocalDateTime.now();
    }
}
