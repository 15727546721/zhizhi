package cn.xu.domain.user.service;

import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserRoleEntity;

import java.util.List;

public interface IUserService {
    List<UserEntity> queryUserList(int page, int size);

    void addUser(UserRoleEntity userRole);

    int updateUser(UserEntity userEntity);

    void deleteUser(Long id);
}
