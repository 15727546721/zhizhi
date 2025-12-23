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
public interface IUserService {
    
    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户ID
     */
    Long getCurrentUserId();

    /**
     * 用户注册
     *
     * @param request 注册请求参数
     */
    void register(UserRegisterRequest request);

    /**
     * 用户登录
     *
     * @param request 登录请求参数
     * @return 用户信息
     */
    User login(UserLoginRequest request);

    /**
     * 用户名登录（管理员登录）
     *
     * @param username 用户名
     * @param password 密码
     * @return 用户信息
     */
    User loginByUsername(String username, String password);

    /**
     * 获取用户信息
     *
     * @param userId 用户ID
     * @return 用户信息
     */
    User getUserInfo(Long userId);

    /**
     * 修改密码
     *
     * @param userId 用户ID
     * @param oldPassword 旧密码
     * @param newPassword 新密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 禁止用户
     *
     * @param userId 用户ID
     */
    void banUser(Long userId);

    /**
     * 解封用户
     *
     * @param userId 用户ID
     */
    void unbanUser(Long userId);

    /**
     * 查询用户列表
     *
     * @param pageRequest 分页请求参数
     * @return 用户列表
     */
    List<User> queryUserList(PageRequest pageRequest);

    /**
     * 带条件查询用户列表
     *
     * @param username 用户名（模糊匹配）
     * @param status 状态
     * @param userType 用户类型
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
     */
    List<User> queryUserListWithFilters(String username, Integer status, Integer userType, Integer page, Integer size);

    /**
     * 统计带条件的用户数量
     */
    Long countUsersByFilters(String username, Integer status, Integer userType);

    /**
     * 添加用户
     *
     * @param userRequest 用户请求参数
     */
    void addUser(SysUserRequest userRequest);

    /**
     * 更新用户
     *
     * @param userRequest 用户请求参数
     */
    void updateUser(SysUserRequest userRequest);

    /**
     * 删除用户
     *
     * @param userId 用户ID
     */
    void deleteUser(Long userId);

    /**
     * 批量删除用户
     *
     * @param userIds 用户ID列表
     */
    void batchDeleteUsers(List<Long> userIds);

    /**
     * 上传用户头像
     *
     * @param file 头像文件
     * @return 头像访问URL
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户信息映射表，key为用户ID，value为用户信息
     */
    Map<Long, User> getBatchUserInfo(Set<Long> userIds);

    /**
     * 批量获取用户信息Map
     *
     * @param userIds 用户ID集合
     * @return 用户信息Map，key为用户ID，value为用户信息
     */
    Map<Long, User> getUserMapByIds(Set<Long> userIds);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户实体，如果不存在则返回null
     */
    User getUserById(Long userId);

    /**
     * 根据用户ID获取用户昵称
     *
     * @param userId 用户ID
     * @return 用户昵称
     */
    String getNicknameById(Long userId);

    /**
     * 更新用户资料
     * 
     * <p>只允许用户修改自己的资料，用户ID从token中获取
     * 
     * @param userId 用户ID（从token中获取，确保安全性）
     * @param request 更新用户资料请求
     */
    void updateUserProfile(Long userId, UpdateUserProfileRequest request);

    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID列表
     * @return 用户列表
     */
    List<User> batchGetUsersByIds(Set<Long> userIds);
    
    /**
     * 批量获取用户信息（支持List参数）
     *
     * @param userIds 用户ID列表
     * @return 用户信息Map
     */
    Map<Long, User> batchGetUserInfo(List<Long> userIds);
    
    /**
     * 查询用户排行榜
     *
     * @param sortType 排序类型
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
     */
    List<User> findUserRanking(String sortType, int page, int size);
    
    /**
     * 统计用户总数（用于排名统计）
     *
     * @return 用户总数
     */
    Long countAllUsers();
    
    /**
     * 搜索用户（用于@提及、综合搜索等）
     *
     * @param keyword 关键词
     * @param limit 限制数量
     * @return 用户列表
     */
    List<User> searchUsers(String keyword, Integer limit);
    
    /**
     * 初始化/重置管理员账号
     * 
     * <p>创建或重置Admin管理员账号，默认密码：AdminPassword123!
     * 
     * @return 管理员用户信息
     */
    User initAdminUser();
    
    /**
     * 注销账户
     * 
     * <p>永久删除用户账户，需要验证密码
     * <p>此操作不可逆
     * 
     * @param userId 用户ID
     * @param password 用户密码（用于验证身份）
     */
    void deleteAccount(Long userId, String password);
    
    /**
     * 批量获取用户信息
     * 
     * @param userIds 用户ID集合
     * @return 用户ID到用户对象的映射
     */
    java.util.Map<Long, User> findUserInfo(java.util.Set<Long> userIds);
}