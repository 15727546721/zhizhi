package cn.xu.api;

import cn.xu.api.model.user.LoginOrRegisterDTO;
import cn.xu.types.model.ResponseEntity;

public interface IUserServiceController {

    // 前台用户登录/注册
    ResponseEntity loginOrRegister(LoginOrRegisterDTO loginOrRegisterDTO);
}
