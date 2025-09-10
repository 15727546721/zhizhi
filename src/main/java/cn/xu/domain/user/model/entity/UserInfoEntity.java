package cn.xu.domain.user.model.entity;

import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 用户信息领域实体
 * 封装用户信息的业务逻辑和规则
 * 
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoEntity {
    private Long id;
    private String username;
    private String email;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String phone;
    private String region;
    private String birthday;
    private String description;
    private Integer status;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    @Builder.Default
    private List<String> roles = new ArrayList<>();
    
    /**
     * 用户状态枚举
     */
    public enum UserStatus {
        NORMAL(1, "正常"),
        DISABLED(0, "禁用"),
        DELETED(-1, "已删除");
        
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
        
        public static UserStatus fromCode(int code) {
            for (UserStatus status : values()) {
                if (status.code == code) {
                    return status;
                }
            }
            throw new IllegalArgumentException("不支持的用户状态: " + code);
        }
    }
    
    /**
     * 性别枚举
     */
    public enum Gender {
        UNKNOWN(0, "未知"),
        MALE(1, "男"),
        FEMALE(2, "女");
        
        private final int code;
        private final String desc;
        
        Gender(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        public static Gender fromCode(int code) {
            for (Gender gender : values()) {
                if (gender.code == code) {
                    return gender;
                }
            }
            return UNKNOWN;
        }
    }
    
    // ==================== 业务方法 ====================
    
    /**
     * 更新用户信息
     */
    public void updateUserInfo(String nickname, String avatar, Integer gender, String phone, String region, String birthday, String description) {
        if (this.status != null && this.status == UserStatus.DISABLED.getCode()) {
            throw new BusinessException("用户已被禁用，无法更新信息");
        }
        
        this.nickname = nickname;
        this.avatar = avatar;
        this.gender = gender;
        this.phone = phone;
        this.region = region;
        this.birthday = birthday;
        this.description = description;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 禁用用户
     */
    public void disable() {
        this.status = UserStatus.DISABLED.getCode();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 启用用户
     */
    public void enable() {
        this.status = UserStatus.NORMAL.getCode();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 判断用户是否正常
     */
    public boolean isNormal() {
        return this.status != null && this.status == UserStatus.NORMAL.getCode();
    }
    
    /**
     * 判断用户是否被禁用
     */
    public boolean isDisabled() {
        return this.status != null && this.status == UserStatus.DISABLED.getCode();
    }
    
    /**
     * 判断用户是否有指定角色
     */
    public boolean hasRole(String role) {
        return roles != null && roles.contains(role);
    }
    
    /**
     * 添加角色
     */
    public void addRole(String role) {
        if (role == null || role.trim().isEmpty()) {
            return;
        }
        
        if (roles == null) {
            roles = new ArrayList<>();
        }
        
        if (!roles.contains(role)) {
            roles.add(role);
        }
    }
    
    /**
     * 移除角色
     */
    public void removeRole(String role) {
        if (roles != null) {
            roles.remove(role);
        }
    }
    
    /**
     * 清除所有角色
     */
    public void clearRoles() {
        if (roles != null) {
            roles.clear();
        }
    }
    
    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return hasRole("ROLE_ADMIN") || hasRole("admin");
    }
    
    /**
     * 获取性别显示名
     */
    public String getGenderDisplay() {
        if (gender == null) {
            return Gender.UNKNOWN.getDesc();
        }
        return Gender.fromCode(gender).getDesc();
    }
    
    /**
     * 获取状态显示名
     */
    public String getStatusDisplay() {
        if (status == null) {
            return "未知";
        }
        try {
            return UserStatus.fromCode(status).getDesc();
        } catch (IllegalArgumentException e) {
            return "未知";
        }
    }
    
    /**
     * 验证用户信息是否完整
     */
    public boolean isProfileComplete() {
        return nickname != null && !nickname.trim().isEmpty() &&
               email != null && !email.trim().isEmpty();
    }
    
    /**
     * 验证用户名格式
     */
    public static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException("用户名不能为空");
        }
        
        if (username.length() < 3 || username.length() > 20) {
            throw new BusinessException("用户名长度必须在3-20个字符之间");
        }
        
        if (!username.matches("^[a-zA-Z0-9_]+$")) {
            throw new BusinessException("用户名只能包含字母、数字和下划线");
        }
    }
    
    /**
     * 验证邮箱格式
     */
    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException("邮箱不能为空");
        }
        
        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        if (!email.matches(emailRegex)) {
            throw new BusinessException("邮箱格式不正确");
        }
    }
}
