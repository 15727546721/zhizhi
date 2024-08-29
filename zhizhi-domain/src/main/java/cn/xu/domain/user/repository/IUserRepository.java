package cn.xu.domain.user.repository;

import cn.xu.domain.user.model.valobj.LoginFormVO;

public interface IUserRepository {


    /**
     * 根据用户名查询用户
     * @param username
     * @return
     */
    LoginFormVO findUserByUsername(String username);
}
