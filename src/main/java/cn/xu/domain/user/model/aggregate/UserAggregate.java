package cn.xu.domain.user.model.aggregate;

import cn.xu.application.common.ResponseCode;
import cn.xu.domain.user.model.entity.RoleEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.valobj.Email;
import cn.xu.domain.user.model.valobj.Password;
import cn.xu.domain.user.model.valobj.Phone;
import cn.xu.domain.user.model.valobj.Username;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户聚合根
 * 管理用户及其角色的一致性边界
 * 封装用户注册、登录、信息更新等业务逻辑
 */
@Data
@Builder
@Slf4j
public class UserAggregate {
    
    private Long id;
    private UserEntity user;
    private List<RoleEntity> roles;
    
    // 领域事件列表
    private List<Object> domainEvents = new ArrayList<>();

    // ==================== 聚合根业务方法 ====================

    /**
     * 用户注册
     */
    public static UserAggregate register(String username, String password, String email, String nickname) {
        // 创建新用户
        UserEntity user = UserEntity.createNewUser(username, password, email, nickname);
        
        // 默认角色（可以根据业务需求设定）
        List<RoleEntity> defaultRoles = Collections.singletonList(
            RoleEntity.builder().id(1L).name("USER").code("user").build()
        );
        
        UserAggregate aggregate = UserAggregate.builder()
                .user(user)
                .roles(defaultRoles)
                .domainEvents(new ArrayList<>())
                .build();
        
        // 添加用户注册事件
        aggregate.addDomainEvent(new UserRegisteredEvent(
            user.getId(), user.getUsernameValue(), user.getEmailValue(), LocalDateTime.now()
        ));
        
        return aggregate;
    }

    /**
     * 用户登录
     */
    public void login(String inputPassword, String ip) {
        validateCanLogin();
        
        if (!user.validatePassword(inputPassword)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户名或密码错误");
        }
        
        // 更新最后登录信息
        user.updateLastLogin(ip);
        
        // 添加登录事件
        addDomainEvent(new UserLoggedInEvent(user.getId(), ip, LocalDateTime.now()));
    }

    /**
     * 更新用户个人信息
     */
    public void updateProfile(String nickname, String region, String birthday, String description, String phone) {
        user.validateCanPerformAction();
        user.updateProfile(nickname, region, birthday, description, phone);
        
        addDomainEvent(new UserProfileUpdatedEvent(user.getId(), LocalDateTime.now()));
    }

    /**
     * 更新用户头像
     */
    public void updateAvatar(String avatarUrl) {
        user.validateCanPerformAction();
        user.updateAvatar(avatarUrl);
        
        addDomainEvent(new UserAvatarUpdatedEvent(user.getId(), avatarUrl, LocalDateTime.now()));
    }

    /**
     * 修改密码
     */
    public void changePassword(String oldPassword, String newPassword) {
        user.validateCanPerformAction();
        user.updatePassword(oldPassword, newPassword);
        
        addDomainEvent(new UserPasswordChangedEvent(user.getId(), LocalDateTime.now()));
    }

    /**
     * 封禁用户
     */
    public void banUser() {
        user.ban();
        
        addDomainEvent(new UserBannedEvent(user.getId(), LocalDateTime.now()));
    }

    /**
     * 解封用户
     */
    public void unbanUser() {
        user.unban();
        
        addDomainEvent(new UserUnbannedEvent(user.getId(), LocalDateTime.now()));
    }

    /**
     * 关注其他用户（被关注者会增加粉丝数，关注者会增加关注数）
     */
    public void followUser() {
        user.validateCanPerformAction();
        user.increaseFollowCount();
        
        addDomainEvent(new UserFollowedEvent(user.getId(), LocalDateTime.now()));
    }

    /**
     * 取消关注
     */
    public void unfollowUser() {
        user.validateCanPerformAction();
        user.decreaseFollowCount();
        
        addDomainEvent(new UserUnfollowedEvent(user.getId(), LocalDateTime.now()));
    }

    /**
     * 被其他用户关注（增加粉丝数）
     */
    public void gainFollower() {
        user.increaseFansCount();
    }

    /**
     * 失去粉丝
     */
    public void loseFollower() {
        user.decreaseFansCount();
    }
    
    /**
     * 直接设置关注数
     * 
     * @param followCount 关注数
     */
    public void setFollowCount(Long followCount) {
        user.setFollowCount(followCount);
    }
    
    /**
     * 直接设置粉丝数
     * 
     * @param fansCount 粉丝数
     */
    public void setFansCount(Long fansCount) {
        user.setFansCount(fansCount);
    }
    
    /**
     * 获取关注数
     * 
     * @return 关注数
     */
    public Long getFollowCount() {
        return user.getFollowCount();
    }
    
    /**
     * 获取粉丝数
     * 
     * @return 粉丝数
     */
    public Long getFansCount() {
        return user.getFansCount();
    }

    /**
     * 获得点赞
     */
    public void receiveLike() {
        user.increaseLikeCount();
    }

    /**
     * 失去点赞
     */
    public void loseLike() {
        user.decreaseLikeCount();
    }

    /**
     * 分配角色
     */
    public void assignRole(RoleEntity role) {
        if (role == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "角色不能为空");
        }
        
        if (hasRole(role.getId())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "用户已拥有该角色");
        }
        
        if (this.roles == null) {
            this.roles = new ArrayList<>();
        }
        
        this.roles.add(role);
        
        addDomainEvent(new UserRoleAssignedEvent(user.getId(), role.getId(), LocalDateTime.now()));
    }

    /**
     * 移除角色
     */
    public void removeRole(Long roleId) {
        if (this.roles == null || this.roles.isEmpty()) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "用户没有任何角色");
        }
        
        boolean removed = this.roles.removeIf(role -> role.getId().equals(roleId));
        
        if (!removed) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "用户不拥有指定角色");
        }
        
        addDomainEvent(new UserRoleRemovedEvent(user.getId(), roleId, LocalDateTime.now()));
    }

    // ==================== 查询方法 ====================

    /**
     * 检查用户是否拥有指定角色
     */
    public boolean hasRole(Long roleId) {
        return this.roles != null && 
               this.roles.stream().anyMatch(role -> role.getId().equals(roleId));
    }

    /**
     * 检查用户是否拥有指定权限
     */
    public boolean hasPermission(String permission) {
        return this.roles != null &&
               this.roles.stream().anyMatch(role -> role.hasPermission(permission));
    }

    /**
     * 获取用户角色名称列表
     */
    public List<String> getRoleNames() {
        return this.roles != null ?
               this.roles.stream().map(RoleEntity::getName).collect(Collectors.toList()) :
               Collections.emptyList();
    }

    /**
     * 获取用户角色ID列表
     */
    public List<Long> getRoleIds() {
        return this.roles != null ?
               this.roles.stream().map(RoleEntity::getId).collect(Collectors.toList()) :
               Collections.emptyList();
    }

    /**
     * 获取聚合根ID
     */
    public Long getId() {
        return this.id;
    }
    
    /**
     * 获取用户ID
     */
    public Long getUserId() {
        return user != null ? user.getId() : null;
    }
    
    /**
     * 获取用户实体
     */
    public UserEntity getUserEntity() {
        return this.user;
    }
    
    /**
     * 获取角色实体列表
     */
    public List<RoleEntity> getRoleEntities() {
        return this.roles;
    }

    /**
     * 获取用户名
     */
    public String getUsername() {
        return user != null ? user.getUsernameValue() : null;
    }

    /**
     * 获取邮箱
     */
    public String getEmail() {
        return user != null ? user.getEmailValue() : null;
    }

    /**
     * 判断用户是否被封禁
     */
    public boolean isBanned() {
        return user != null && user.isBanned();
    }

    /**
     * 判断用户是否正常
     */
    public boolean isNormal() {
        return user != null && user.isNormal();
    }

    // ==================== 私有辅助方法 ====================

    /**
     * 验证用户是否可以登录
     */
    private void validateCanLogin() {
        if (user == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户不存在");
        }
        
        if (user.isBanned()) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "账户已被封禁");
        }
    }

    /**
     * 添加领域事件
     */
    private void addDomainEvent(Object event) {
        if (this.domainEvents == null) {
            this.domainEvents = new ArrayList<>();
        }
        this.domainEvents.add(event);
        log.debug("添加领域事件: {}", event.getClass().getSimpleName());
    }

    /**
     * 获取并清空领域事件
     */
    public List<Object> pullDomainEvents() {
        List<Object> events = new ArrayList<>(this.domainEvents);
        this.domainEvents.clear();
        return events;
    }

    // ==================== 内部事件类定义 ====================

    public static class UserRegisteredEvent {
        private final Long userId;
        private final String username;
        private final String email;
        private final LocalDateTime registerTime;

        public UserRegisteredEvent(Long userId, String username, String email, LocalDateTime registerTime) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.registerTime = registerTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public String getUsername() { return username; }
        public String getEmail() { return email; }
        public LocalDateTime getRegisterTime() { return registerTime; }
    }

    public static class UserLoggedInEvent {
        private final Long userId;
        private final String ip;
        private final LocalDateTime loginTime;

        public UserLoggedInEvent(Long userId, String ip, LocalDateTime loginTime) {
            this.userId = userId;
            this.ip = ip;
            this.loginTime = loginTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public String getIp() { return ip; }
        public LocalDateTime getLoginTime() { return loginTime; }
    }

    public static class UserProfileUpdatedEvent {
        private final Long userId;
        private final LocalDateTime updateTime;

        public UserProfileUpdatedEvent(Long userId, LocalDateTime updateTime) {
            this.userId = userId;
            this.updateTime = updateTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public LocalDateTime getUpdateTime() { return updateTime; }
    }

    public static class UserAvatarUpdatedEvent {
        private final Long userId;
        private final String avatarUrl;
        private final LocalDateTime updateTime;

        public UserAvatarUpdatedEvent(Long userId, String avatarUrl, LocalDateTime updateTime) {
            this.userId = userId;
            this.avatarUrl = avatarUrl;
            this.updateTime = updateTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public String getAvatarUrl() { return avatarUrl; }
        public LocalDateTime getUpdateTime() { return updateTime; }
    }

    public static class UserPasswordChangedEvent {
        private final Long userId;
        private final LocalDateTime changeTime;

        public UserPasswordChangedEvent(Long userId, LocalDateTime changeTime) {
            this.userId = userId;
            this.changeTime = changeTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public LocalDateTime getChangeTime() { return changeTime; }
    }

    public static class UserBannedEvent {
        private final Long userId;
        private final LocalDateTime banTime;

        public UserBannedEvent(Long userId, LocalDateTime banTime) {
            this.userId = userId;
            this.banTime = banTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public LocalDateTime getBanTime() { return banTime; }
    }

    public static class UserUnbannedEvent {
        private final Long userId;
        private final LocalDateTime unbanTime;

        public UserUnbannedEvent(Long userId, LocalDateTime unbanTime) {
            this.userId = userId;
            this.unbanTime = unbanTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public LocalDateTime getUnbanTime() { return unbanTime; }
    }

    public static class UserFollowedEvent {
        private final Long userId;
        private final LocalDateTime followTime;

        public UserFollowedEvent(Long userId, LocalDateTime followTime) {
            this.userId = userId;
            this.followTime = followTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public LocalDateTime getFollowTime() { return followTime; }
    }

    public static class UserUnfollowedEvent {
        private final Long userId;
        private final LocalDateTime unfollowTime;

        public UserUnfollowedEvent(Long userId, LocalDateTime unfollowTime) {
            this.userId = userId;
            this.unfollowTime = unfollowTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public LocalDateTime getUnfollowTime() { return unfollowTime; }
    }

    public static class UserRoleAssignedEvent {
        private final Long userId;
        private final Long roleId;
        private final LocalDateTime assignTime;

        public UserRoleAssignedEvent(Long userId, Long roleId, LocalDateTime assignTime) {
            this.userId = userId;
            this.roleId = roleId;
            this.assignTime = assignTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public Long getRoleId() { return roleId; }
        public LocalDateTime getAssignTime() { return assignTime; }
    }

    public static class UserRoleRemovedEvent {
        private final Long userId;
        private final Long roleId;
        private final LocalDateTime removeTime;

        public UserRoleRemovedEvent(Long userId, Long roleId, LocalDateTime removeTime) {
            this.userId = userId;
            this.roleId = roleId;
            this.removeTime = removeTime;
        }

        // getters...
        public Long getUserId() { return userId; }
        public Long getRoleId() { return roleId; }
        public LocalDateTime getRemoveTime() { return removeTime; }
    }
}