package cn.xu.domain.user.service;

import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;

public interface IUserLoginService {

    /**
     * 登录
     *
     * @param username
     * @param password
     * @return
     */
    String loginByAdmin(LoginFormVO loginFormVO);

    /**
     * 根据token获取用户信息
     */
    UserInfoEntity getInfoByToken(String token);
}
