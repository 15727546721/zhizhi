package cn.xu.service.user;

import cn.xu.common.request.PageRequest;
import cn.xu.model.dto.user.SysUserRequest;
import cn.xu.model.dto.user.UpdateUserProfileRequest;
import cn.xu.model.dto.user.UserLoginRequest;
import cn.xu.model.dto.user.UserRegisterRequest;
import cn.xu.model.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

/**
 * 用户应用服务（门面）
 * <p>Controller 层调用入口，协调各子服务</p>
 * <p>拆分职责：</p>
 * <ul>
 *   <li>UserAuthService - 认证相关</li>
 *   <li>UserQueryService - 查询操作</li>
 *   <li>UserCommandService - 写操作</li>
 *   <li>UserAdminService - 管理员操作</li>
 *   <li>UserRankingService - 排行榜</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserApplicationService {

    private final UserAuthService authService;
    private final UserQueryService queryService;
    private final UserCommandService commandService;
    private final UserAdminService adminService;
    private final UserRankingService rankingService;

    // ==================== 认证相关 ====================

    public Long getCurrentUserId() {
        return authService.getCurrentUserId();
    }

    public void register(UserRegisterRequest request) {
        authService.register(request);
    }

    public User login(UserLoginRequest request) {
        return authService.login(request);
    }

    public User loginWithIp(UserLoginRequest request, String clientIp) {
        return authService.loginWithIp(request, clientIp);
    }

    public User loginWithCode(String email, String code, String clientIp) {
        return authService.loginWithCode(email, code, clientIp);
    }

    public User loginByUsername(String username, String password) {
        return authService.loginByUsername(username, password);
    }

    public void changePassword(Long userId, String oldPassword, String newPassword) {
        authService.changePassword(userId, oldPassword, newPassword);
    }

    // ==================== 查询操作 ====================

    public User getUserById(Long userId) {
        return queryService.getById(userId);
    }

    public User getUserInfo(Long userId) {
        return queryService.findById(userId).orElse(null);
    }

    public String getNicknameById(Long userId) {
        return queryService.getNicknameById(userId);
    }

    public Map<Long, User> batchGetUserInfo(List<Long> userIds) {
        return queryService.batchGetByIds(userIds);
    }

    public List<User> batchGetUsersByIds(Set<Long> userIds) {
        return queryService.batchGetListByIds(userIds);
    }

    public Map<Long, User> getBatchUserInfo(Set<Long> userIds) {
        return queryService.batchGetByIds(userIds);
    }

    public Map<Long, User> getUserMapByIds(Set<Long> userIds) {
        return queryService.batchGetByIds(userIds);
    }

    public Map<Long, User> findUserInfo(Set<Long> userIds) {
        return queryService.batchGetByIds(userIds);
    }

    public List<User> searchUsers(String keyword, Integer limit) {
        return queryService.searchUsers(keyword, limit);
    }

    public List<User> queryUserList(PageRequest pageRequest) {
        if (pageRequest == null) {
            pageRequest = PageRequest.of(1, 20);
        }
        return queryService.findByPage(pageRequest.getPageNo(), pageRequest.getPageSize());
    }

    public List<User> queryUserListWithFilters(String username, Integer status, Integer userType, Integer page, Integer size) {
        if (page == null || page < 1) page = 1;
        if (size == null || size < 1) size = 10;
        return queryService.findByConditions(username, status, userType, page, size);
    }

    public Long countUsersByFilters(String username, Integer status, Integer userType) {
        return queryService.countByConditions(username, status, userType);
    }

    public Long countAllUsers() {
        return queryService.countAll();
    }

    // ==================== 写操作 ====================

    public void updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        commandService.updateProfile(userId, request);
    }

    public String uploadAvatar(MultipartFile file) {
        return commandService.uploadAvatar(file);
    }

    public void updateUserAvatar(Long userId, String avatarUrl) {
        commandService.updateAvatar(userId, avatarUrl);
    }

    public void banUser(Long userId) {
        commandService.banUser(userId);
    }

    public void unbanUser(Long userId) {
        commandService.unbanUser(userId);
    }

    public void deleteUser(Long userId) {
        commandService.deleteUser(userId);
    }

    public void deleteAccount(Long userId, String password) {
        commandService.deleteAccount(userId, password);
    }

    // ==================== 管理员操作 ====================

    public void addUser(SysUserRequest userRequest) {
        adminService.addUser(userRequest);
    }

    public void updateUser(SysUserRequest userRequest) {
        adminService.updateUser(userRequest);
    }

    public void batchDeleteUsers(List<Long> userIds) {
        adminService.batchDeleteUsers(userIds);
    }

    public User initAdminUser() {
        return adminService.initAdminUser();
    }

    // ==================== 排行榜 ====================

    public List<User> findUserRanking(String sortType, int page, int size) {
        return rankingService.findUserRanking(sortType, page, size);
    }
}
