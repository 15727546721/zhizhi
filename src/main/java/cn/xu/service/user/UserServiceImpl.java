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
 * 用户服务实现（兼容层）
 * <p>委托给 UserApplicationService，保持向后兼容</p>
 * <p>新代码建议直接使用 UserApplicationService</p>
 */
@Slf4j
@Service("userService")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserApplicationService applicationService;

    @Override
    public Long getCurrentUserId() {
        return applicationService.getCurrentUserId();
    }

    @Override
    public void register(UserRegisterRequest request) {
        applicationService.register(request);
    }

    @Override
    public User login(UserLoginRequest request) {
        return applicationService.login(request);
    }

    @Override
    public User loginWithIp(UserLoginRequest request, String clientIp) {
        return applicationService.loginWithIp(request, clientIp);
    }

    @Override
    public User loginWithCode(String email, String code, String clientIp) {
        return applicationService.loginWithCode(email, code, clientIp);
    }

    @Override
    public User loginByUsername(String username, String password) {
        return applicationService.loginByUsername(username, password);
    }

    @Override
    public User getUserInfo(Long userId) {
        return applicationService.getUserInfo(userId);
    }

    @Override
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        applicationService.changePassword(userId, oldPassword, newPassword);
    }

    @Override
    public void banUser(Long userId) {
        applicationService.banUser(userId);
    }

    @Override
    public void unbanUser(Long userId) {
        applicationService.unbanUser(userId);
    }

    @Override
    public List<User> queryUserList(PageRequest pageRequest) {
        return applicationService.queryUserList(pageRequest);
    }

    @Override
    public List<User> queryUserListWithFilters(String username, Integer status, Integer userType, Integer page, Integer size) {
        return applicationService.queryUserListWithFilters(username, status, userType, page, size);
    }

    @Override
    public Long countUsersByFilters(String username, Integer status, Integer userType) {
        return applicationService.countUsersByFilters(username, status, userType);
    }

    @Override
    public void addUser(SysUserRequest userRequest) {
        applicationService.addUser(userRequest);
    }

    @Override
    public void updateUser(SysUserRequest userRequest) {
        applicationService.updateUser(userRequest);
    }

    @Override
    public void deleteUser(Long userId) {
        applicationService.deleteUser(userId);
    }

    @Override
    public void batchDeleteUsers(List<Long> userIds) {
        applicationService.batchDeleteUsers(userIds);
    }

    @Override
    public String uploadAvatar(MultipartFile file) {
        return applicationService.uploadAvatar(file);
    }

    @Override
    public Map<Long, User> getBatchUserInfo(Set<Long> userIds) {
        return applicationService.getBatchUserInfo(userIds);
    }

    @Override
    public Map<Long, User> getUserMapByIds(Set<Long> userIds) {
        return applicationService.getUserMapByIds(userIds);
    }

    @Override
    public User getUserById(Long userId) {
        return applicationService.getUserById(userId);
    }

    @Override
    public String getNicknameById(Long userId) {
        return applicationService.getNicknameById(userId);
    }

    @Override
    public void updateUserProfile(Long userId, UpdateUserProfileRequest request) {
        applicationService.updateUserProfile(userId, request);
    }

    @Override
    public List<User> batchGetUsersByIds(Set<Long> userIds) {
        return applicationService.batchGetUsersByIds(userIds);
    }

    @Override
    public Map<Long, User> batchGetUserInfo(List<Long> userIds) {
        return applicationService.batchGetUserInfo(userIds);
    }

    @Override
    public List<User> findUserRanking(String sortType, int page, int size) {
        return applicationService.findUserRanking(sortType, page, size);
    }

    @Override
    public Long countAllUsers() {
        return applicationService.countAllUsers();
    }

    @Override
    public List<User> searchUsers(String keyword, Integer limit) {
        return applicationService.searchUsers(keyword, limit);
    }

    @Override
    public User initAdminUser() {
        return applicationService.initAdminUser();
    }

    @Override
    public void deleteAccount(Long userId, String password) {
        applicationService.deleteAccount(userId, password);
    }

    @Override
    public Map<Long, User> findUserInfo(Set<Long> userIds) {
        return applicationService.findUserInfo(userIds);
    }
}
