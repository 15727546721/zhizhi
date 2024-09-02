package cn.xu.domain.user.service;

import cn.xu.domain.user.model.entity.UserEntity;

public interface IUserService {
    UserEntity queryUserList(int page, int size);

    UserEntity queryAdminList(int page, int size);
}
