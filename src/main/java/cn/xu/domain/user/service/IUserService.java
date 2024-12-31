package cn.xu.domain.user.service;

import cn.xu.api.controller.web.user.LoginRequest;
import cn.xu.api.controller.web.user.RegisterRequest;
import cn.xu.api.dto.user.UserPasswordRequest;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.entity.UserRoleEntity;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface IUserService {
    List<UserEntity> queryUserList(int page, int size);

    void addUser(UserRoleEntity userRole);

    int updateUser(UserEntity userEntity);

    void deleteUser(Long id);

    void updatePassword(UserPasswordRequest userPasswordRequest);

    UserInfoEntity queryUserInfo(Long id);

    void updateUserInfo(UserInfoEntity userInfoEntity);

    void uploadAvatar(Long id, String avatar);

    UserEntity getUserInfo(Long id);

    void register(RegisterRequest registerRequest);

    UserEntity login(LoginRequest loginRequest);

    /**
     * 批量获取用户信息
     * @param userIds
     * @return
     */
    Map<Long, UserEntity> getBatchUserInfo(Set<Long> userIds);
}
