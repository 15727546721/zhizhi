package cn.xu.domain.user.service;

import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;

public interface IUserLoginService {

    /**
     * 登录
     */
    String loginByAdmin(LoginFormVO loginFormVO);

    /**
     * 根据token获取用户信息
     */
    UserInfoEntity getInfoByToken(String token);

    /**
     * 获取当前登录用户的信息和角色
     */
    UserInfoEntity getCurrentUserInfoAndRoles();
}
