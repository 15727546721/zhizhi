package cn.xu.api;

import cn.xu.api.model.user.LoginFormDTO;
import cn.xu.types.model.ResponseEntity;

public interface IUserServiceController {

    ResponseEntity loginByAdmin(LoginFormDTO loginFormDTO);

    ResponseEntity getInfoByToken(String token);

    String queryUserInfo(String req);

    String updateUserInfo(String req);

    String deleteUserInfo(String req);

    String creatUserInfo(String req);
}
