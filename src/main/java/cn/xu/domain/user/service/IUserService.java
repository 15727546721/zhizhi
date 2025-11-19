package cn.xu.domain.user.service;

import cn.xu.api.system.model.dto.user.SysUserRequest;
import cn.xu.api.web.model.dto.user.UpdateUserProfileRequest;
import cn.xu.api.web.model.dto.user.UpdateUserRequest;
import cn.xu.api.web.model.dto.user.UserLoginRequest;
import cn.xu.api.web.model.dto.user.UserRegisterRequest;
import cn.xu.common.request.PageRequest;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IUserService {
    /**
     * 获取当前登录用户ID
     *
     * @return 当前登录用户ID
     */
    Long getCurrentUserId();

    /**
     * 用户注册
     */
    void register(UserRegisterRequest request);

    /**
     * 用户登录
     */
    UserEntity login(UserLoginRequest request);

    /**
     * 获取用户信息
     */
    UserEntity getUserInfo(Long userId);

    /**
     * 修改密码
     */
    void changePassword(Long userId, String oldPassword, String newPassword);

    /**
     * 封禁用户
     */
    void banUser(Long userId);

    /**
     * 解封用户
     */
    void unbanUser(Long userId);

    /**
     * 查询用户列表
     */
    List<UserEntity> queryUserList(PageRequest pageRequest);

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
     * 查询用户个人信息
     */
    UserInfoEntity queryUserInfo();

    /**
     * 更新用户个人信息
     */
    void updateUserInfo(UserInfoEntity userInfoEntity);

    /**
     * 上传用户头像
     *
     * @param file 头像文件
     * @return 头像访问URL
     */
    String uploadAvatar(MultipartFile file);

    /**
     * 批量获取用户信息
     */
    Map<Long, UserEntity> getBatchUserInfo(Set<Long> userIds);

    /**
     * 批量获取用户信息Map
     *
     * @param userIds 用户ID集合
     * @return 用户信息Map, key为用户ID, value为用户信息
     */
    Map<Long, UserEntity> getUserMapByIds(Set<Long> userIds);

    /**
     * 根据用户ID获取用户信息
     *
     * @param userId 用户ID
     * @return 用户实体，如果不存在则返回null
     */
    UserEntity getUserById(Long userId);

    /**
     * 根据用户ID获取用户名
     *
     * @param userId 用户ID
     * @return 用户名
     */
    String getNicknameById(Long userId);

    /**
     * 更新用户信息
     *
     * @param user
     * @deprecated 请使用 updateUserProfile 方法
     */
    @Deprecated
    void update(UpdateUserRequest user);
    
    /**
     * 更新用户资料
     * 只允许用户修改自己的资料，用户ID从token中获取
     * 
     * @param userId 用户ID（从token中获取，确保安全性）
     * @param request 更新用户资料请求
     */
    void updateUserProfile(Long userId, UpdateUserProfileRequest request);

    /**
     * 批量获取用户信息
     *
     * @param userIds 用户ID集合
     * @return 用户信息集合
     */
    List<UserEntity> batchGetUserInfo(List<Long> userIds);
    
    /**
     * 查询用户排行榜
     *
     * @param sortType 排序类型：fans(粉丝数)、likes(获赞数)、posts(帖子数)、comprehensive(综合)
     * @param page 页码
     * @param size 每页数量
     * @return 用户列表
     */
    List<UserEntity> findUserRanking(String sortType, int page, int size);
    
    /**
     * 统计用户总数（用于排行榜）
     *
     * @return 用户总数
     */
    Long countAllUsers();
}
