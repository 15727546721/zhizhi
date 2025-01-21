package cn.xu.domain.user.service;

import cn.xu.api.system.model.dto.user.UserRequest;
import cn.xu.api.web.model.dto.user.LoginRequest;
import cn.xu.api.web.model.dto.user.RegisterRequest;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.infrastructure.common.request.PageRequest;
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
    UserEntity register(RegisterRequest request);

    /**
     * 用户登录
     */
    UserEntity login(LoginRequest request);

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
    void addUser(UserRequest userRequest);

    /**
     * 更新用户
     */
    void updateUser(UserRequest userRequest);

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
}
