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
 *
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User implements Serializable {

    // ========== 状态常量 ==========
    /**
     * 正常状态
     */
    public static final int STATUS_NORMAL = 1;
    /**
     * 禁用状态
     */
    public static final int STATUS_DISABLED = 0;
    /**
     * 待审核状态
     */
    public static final int STATUS_PENDING = 2;
    /**
     * 已删除状态
     */
    public static final int STATUS_DELETED = -1;

    // ========== 用户类型常量 ==========
    /**
     * 普通用户
     */
    public static final int USER_TYPE_NORMAL = 1;
    /**
     * 官方账号
     */
    public static final int USER_TYPE_OFFICIAL = 2;
    /**
     * 管理员
     */
    public static final int USER_TYPE_ADMIN = 3;

    /**
     * 用户ID
     */
    private Long id;
    /**
     * 账号名（唯一标识、不可修改、用于登录）
     */
    private String username;
    /**
     * 密码(加密后)
     */
    private String password;
    /**
     * 邮箱
     */
    private String email;
    /**
     * 显示名（用户可修改、用于前台展示）
     */
    private String nickname;
    /**
     * 头像URL
     */
    private String avatar;
    /**
     * 性别(1:男 0:女)
     */
    private Integer gender;
    /**
     * 手机号
     */
    private String phone;
    /**
     * 地区
     */
    private String region;
    /**
     * 生日
     */
    private String birthday;
    /**
     * 个人简介
     */
    private String description;
    /**
     * 账号状态(1:正常 0:禁用)
     */
    private Integer status;
    /**
     * 用户类型
     */
    private Integer userType;
    /**
     * 关注数量
     */
    private Long followCount;
    /**
     * 粉丝数量
     */
    private Long fansCount;
    /**
     * 获赞数量
     */
    private Long likeCount;
    /**
     * 最后登录时间
     */
    private LocalDateTime lastLoginTime;
    /**
     * 最后登录IP
     */
    private String lastLoginIp;
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 帖子数量
     */
    private Long postCount;

    /**
     * 评论数量
     */
    private Long commentCount;

    // ========== 业务方法 ==========

    /**
     * 是否正常状态
     */
    public boolean isNormal() {
        return this.status != null && this.status == STATUS_NORMAL;
    }

    /**
     * 是否被禁用
     */
    public boolean isDisabled() {
        return this.status != null && this.status == STATUS_DISABLED;
    }

    /**
     * 激活用户
     */
    public void activate() {
        this.status = STATUS_NORMAL;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 禁用用户
     */
    public void disable() {
        this.status = STATUS_DISABLED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 增加关注数
     */
    public void increaseFollowCount() {
        this.followCount = (this.followCount == null ? 0 : this.followCount) + 1;
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
        this.fansCount = (this.fansCount == null ? 0 : this.fansCount) + 1;
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
        this.likeCount = (this.likeCount == null ? 0 : this.likeCount) + 1;
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
        this.postCount = (this.postCount == null ? 0 : this.postCount) + 1;
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
        this.commentCount = (this.commentCount == null ? 0 : this.commentCount) + 1;
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
     * 更新最后登录信息
     */
    public void updateLastLogin(String ip) {
        this.lastLoginTime = LocalDateTime.now();
        this.lastLoginIp = ip;
    }

    /**
     * 加密密码
     */
    public void encryptPassword() {
        if (this.password != null && !this.password.isEmpty()) {
            this.password = SaSecureUtil.sha256(this.password);
        }
    }

    /**
     * 验证密码
     */
    public boolean verifyPassword(String rawPassword) {
        if (this.password == null || rawPassword == null) {
            return false;
        }
        String encryptedPassword = SaSecureUtil.sha256(rawPassword);
        return this.password.equals(encryptedPassword);
    }

    /**
     * 更新密码（自动加密）
     */
    public void updatePassword(String newPassword) {
        if (newPassword != null && !newPassword.isEmpty()) {
            this.password = SaSecureUtil.sha256(newPassword);
            this.updateTime = LocalDateTime.now();
        }
    }

    // ========== 字段校验常量 ==========
    private static final int MIN_USERNAME_LENGTH = 4;
    private static final int MAX_USERNAME_LENGTH = 20;
    private static final String USERNAME_PATTERN = "^[a-zA-Z0-9_\\u4e00-\\u9fa5]+$";
    private static final String EMAIL_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);
    private static final String PHONE_REGEX = "^1[3-9]\\d{9}$";
    private static final Pattern PHONE_PATTERN = Pattern.compile(PHONE_REGEX);

    /**
     * 创建新用户
     */
    public static User createNewUser(String username, String password, String email, String nickname) {
        LocalDateTime now = LocalDateTime.now();

        User user = User.builder()
                .username(username)
                .password(password)
                .email(email)
                .nickname(nickname != null ? nickname : username)
                .status(STATUS_NORMAL)
                .followCount(0L)
                .fansCount(0L)
                .likeCount(0L)
                .createTime(now)
                .updateTime(now)
                .build();

        // 验证并加密密码
        user.validateFields();
        user.encryptPassword();

        return user;
    }

    /**
     * 验证用户字段是否合法
     */
    public void validateFields() {
        validateUsername(this.username);
        validateEmail(this.email);
        if (this.phone != null && !this.phone.trim().isEmpty()) {
            validatePhone(this.phone);
        }
    }

    /**
     * 验证用户名格式
     */
    public static void validateUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "用户名不能为空");
        }

        String trimmed = username.trim();
        if (trimmed.length() < MIN_USERNAME_LENGTH) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(),
                    "用户名长度不能少于" + MIN_USERNAME_LENGTH + "个字符");
        }

        if (trimmed.length() > MAX_USERNAME_LENGTH) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(),
                    "用户名长度不能超过" + MAX_USERNAME_LENGTH + "个字符");
        }

        if (!trimmed.matches(USERNAME_PATTERN)) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(),
                    "用户名只能包含字母、数字、下划线和中文字符");
        }
    }

    /**
     * 验证邮箱格式
     */
    public static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱不能为空");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱格式不正确");
        }
    }

    /**
     * 验证手机号格式
     */
    public static void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return; // 手机号可选
        }

        if (!PHONE_PATTERN.matcher(phone).matches()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "手机号格式不正确");
        }
    }

    /**
     * 验证用户是否可以执行操作
     */
    public void validateCanPerformAction() {
        if (this.status == null || this.status == STATUS_DISABLED) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "账户已被禁用，无法执行此操作");
        }
        if (this.status == STATUS_PENDING) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "账户待审核，无法执行此操作");
        }
    }

    /**
     * 判断是否为待审核状态
     */
    public boolean isPending() {
        return this.status != null && this.status == STATUS_PENDING;
    }

    /**
     * 封禁用户
     */
    public void ban() {
        if (this.status != null && this.status == STATUS_DISABLED) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户已被封禁");
        }
        this.status = STATUS_DISABLED;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 解封用户
     */
    public void unban() {
        if (this.status == null || this.status != STATUS_DISABLED) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户未被封禁");
        }
        this.status = STATUS_NORMAL;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 更新个人资料
     */
    public void updateProfile(String nickname, String region, String description, String phone) {
        if (nickname != null && !nickname.trim().isEmpty()) {
            this.nickname = nickname.trim();
        }

        if (phone != null && !phone.trim().isEmpty()) {
            validatePhone(phone);
            this.phone = phone;
        }

        this.region = region;
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
     * 修改密码（需要验证旧密码）
     */
    public void changePassword(String oldPassword, String newPassword) {
        if (!verifyPassword(oldPassword)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "原密码错误");
        }

        if (newPassword == null || newPassword.trim().isEmpty()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "新密码不能为空");
        }

        if (newPassword.length() < 6) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "密码长度不能少于6位");
        }

        this.password = SaSecureUtil.sha256(newPassword);
        this.updateTime = LocalDateTime.now();
    }

    // ========== 用户类型判断方法 ==========

    /**
     * 判断是否为普通用户
     */
    public boolean isNormalUser() {
        return this.userType != null && this.userType == USER_TYPE_NORMAL;
    }

    /**
     * 判断是否为官方账号
     */
    public boolean isOfficialAccount() {
        return this.userType != null && this.userType == USER_TYPE_OFFICIAL;
    }

    /**
     * 判断是否为管理员（基于userType）
     */
    public boolean isAdminByType() {
        return this.userType != null && this.userType == USER_TYPE_ADMIN;
    }

    /**
     * 判断是否为超级管理员
     */
    public boolean isSuperAdmin() {
        return this.id != null && this.id == 1L;
    }

    /**
     * 判断是否为管理员
     */
    public boolean isAdmin() {
        return isSuperAdmin() || isAdminByType();
    }
}