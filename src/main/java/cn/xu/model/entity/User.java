package cn.xu.model.entity;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.xu.common.ResponseCode;
import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.regex.Pattern;

/**
 * 用户实体
 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    // ========== 状态常量 ==========
    
    /** 正常状态 */
    public static final int STATUS_NORMAL = 1;
    
    /** 禁用状态 */
    public static final int STATUS_DISABLED = 0;
    
    /** 待审核状态 */
    public static final int STATUS_PENDING = 2;
    
    /** 已删除状态 */
    public static final int STATUS_DELETED = -1;

    // ========== 用户类型常量 ==========
    
    /** 普通用户 */
    public static final int USER_TYPE_NORMAL = 1;
    
    /** 官方账号 */
    public static final int USER_TYPE_OFFICIAL = 2;
    
    /** 管理员 */
    public static final int USER_TYPE_ADMIN = 3;

    // ========== 字段 ==========
    
    private Long id;
    private String username;
    private String password;
    private String email;
    private String nickname;
    private String avatar;
    private Integer gender;
    private String phone;
    private String region;
    private String birthday;
    private String description;
    private Integer status;
    private Integer userType;
    private Long followCount;
    private Long fansCount;
    private Long likeCount;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Long postCount;
    private Long commentCount;

    // ========== 工厂方法 ==========
    
    /**
     * 创建新用户
     *
     * @param username 用户名
     * @param password 密码（明文）
     * @param email 邮箱
     * @param nickname 昵称
     * @return 新用户实例
     */
    public static User createNewUser(String username, String password, String email, String nickname) {
        return User.builder()
                .username(username)
                .password(SaSecureUtil.sha256(password))
                .email(email)
                .nickname(nickname != null ? nickname : username)
                .status(STATUS_NORMAL)
                .userType(USER_TYPE_NORMAL)
                .followCount(0L)
                .fansCount(0L)
                .likeCount(0L)
                .postCount(0L)
                .commentCount(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }

    // ========== 业务方法 ==========
    
    /**
     * 是否正常状态
     */
    public boolean isNormal() {
        return STATUS_NORMAL == status;
    }
    
    /**
     * 是否禁用状态
     */
    public boolean isDisabled() {
        return STATUS_DISABLED == status;
    }
    
    /**
     * 增加关注数
     */
    public void increaseFollowCount() {
        this.followCount = (this.followCount == null ? 0L : this.followCount) + 1;
    }
    
    /**
     * 减少关注数
     */
    public void decreaseFollowCount() {
        if (this.followCount != null && this.followCount > 0) {
            this.followCount--;
        }
    }
    
    /**
     * 增加粉丝数
     */
    public void increaseFansCount() {
        this.fansCount = (this.fansCount == null ? 0L : this.fansCount) + 1;
    }
    
    /**
     * 减少粉丝数
     */
    public void decreaseFansCount() {
        if (this.fansCount != null && this.fansCount > 0) {
            this.fansCount--;
        }
    }
    
    /**
     * 增加点赞数
     */
    public void increaseLikeCount() {
        this.likeCount = (this.likeCount == null ? 0L : this.likeCount) + 1;
    }
    
    /**
     * 减少点赞数
     */
    public void decreaseLikeCount() {
        if (this.likeCount != null && this.likeCount > 0) {
            this.likeCount--;
        }
    }
    
    /**
     * 增加帖子数
     */
    public void increasePostCount() {
        this.postCount = (this.postCount == null ? 0L : this.postCount) + 1;
    }
    
    /**
     * 减少帖子数
     */
    public void decreasePostCount() {
        if (this.postCount != null && this.postCount > 0) {
            this.postCount--;
        }
    }
    
    /**
     * 增加评论数
     */
    public void increaseCommentCount() {
        this.commentCount = (this.commentCount == null ? 0L : this.commentCount) + 1;
    }
    
    /**
     * 减少评论数
     */
    public void decreaseCommentCount() {
        if (this.commentCount != null && this.commentCount > 0) {
            this.commentCount--;
        }
    }
    
    /**
     * 更新登录信息
     */
    public void updateLoginInfo(String ip) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 验证密码
     */
    public boolean verifyPassword(String rawPassword) {
        if (rawPassword == null || this.password == null) {
            return false;
        }
        return this.password.equals(SaSecureUtil.sha256(rawPassword));
    }
    
    /**
     * 修改密码
     */
    public void changePassword(String newPassword) {
        this.password = SaSecureUtil.sha256(newPassword);
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新密码（别名方法）
     */
    public void updatePassword(String newPassword) {
        changePassword(newPassword);
    }
    
    /**
     * 加密密码
     */
    public void encryptPassword() {
        if (this.password != null) {
            this.password = SaSecureUtil.sha256(this.password);
        }
    }
    
    /**
     * 是否官方账号
     */
    public boolean isOfficialAccount() {
        return USER_TYPE_OFFICIAL == userType;
    }
    
    /**
     * 是否管理员
     */
    public boolean isAdmin() {
        return USER_TYPE_ADMIN == userType;
    }
}