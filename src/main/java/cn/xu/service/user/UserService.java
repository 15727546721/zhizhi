package cn.xu.service.user;

import cn.xu.common.request.PageRequest;
import cn.xu.model.dto.user.SysUserRequest;
import cn.xu.model.dto.user.UpdateUserProfileRequest;
import cn.xu.model.dto.user.UserLoginRequest;
import cn.xu.model.dto.user.UserRegisterRequest;
import cn.xu.model.entity.User;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 用户服务接口
 * <p>定义用户认证、资料管理、管理员操作等功能</p>
 */
public interface UserService {
    
    /**
     * 获取当前登录用户ID
     */
    Long getCurrentUserId();

    /**
     * 用户注册
     */
    void register(UserRegisterRequest request);

    /**
     * 用户登录
     */
    User login(UserLoginRequest request);

    /**
     * 用户名登录（管理员登录）
     */
    User loginByUsername(String username, String password);

    /**
     * 获取用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 禁止用户
     */
    void banUser(Long userId);

    /**
     * 解封用户
     */
    void unbanUser(Long userId);

    /**
     * 查询用户列表
     */
    List<User> queryUserList(PageRequest pageRequest);

    /**
     * 带条件查询用户列表
     */
    List<User> queryUserListWithFilters(String username, Integer status, Integer userType, Integer page, Integer size);

    /**
     * 统计带条件的用户数量
     */
    Long countUsersByFilters(String username, Integer status, Integer userType);

    /**
     * 添加用户
     */
    void addUser(SysUserRequest userRequest);

    /**
     * 更新用户
     */
    void updateUser(SysUserRequest userRequest);

    /**
     * 删除用户
     */
    void deleteUser(Long userId);

    /**
     * 批量删除用户
     */
    void batchDeleteUsers(List<Long> userIds);

    /**
     * 上传用户头像
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 批量获取用户信息
     */
    Map<Long, User> getBatchUserInfo(Set<Long> userIds);

    /**
     * 批量获取用户信息Map
     */
    Map<Long, User> getUserMapByIds(Set<Long> userIds);

    /**
     * 根据用户ID获取用户信息
     */
    User getUserById(Long userId);

    /**
     * 根据用户ID获取用户昵称
     */
    String getNicknameById(Long userId);

    /**
     * 更新用户资料
     */
    void updateUserProfile(Long userId, UpdateUserProfileRequest request);

    /**
     * 批量获取用户信息
     */
    List<User> batchGetUsersByIds(Set<Long> userIds);
    
    /**
     * 批量获取用户信息（支持List参数）
     */
    Map<Long, User> batchGetUserInfo(List<Long> userIds);
    
    /**
     * 查询用户排行榜
     */
    List<User> findUserRanking(String sortType, int page, int size);
    
    /**
     * 统计用户总数
     */
    Long countAllUsers();
    
    /**
     * 搜索用户
     */
    List<User> searchUsers(String keyword, Integer limit);
    
    /**
     * 初始化/重置管理员账号
     */
    User initAdminUser();
    
    /**
     * 注销账户
     */
    void deleteAccount(Long userId, String password);
    
    /**
     * 批量获取用户信息
     */
    Map<Long, User> findUserInfo(Set<Long> userIds);
}
