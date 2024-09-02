package cn.xu.api;

import cn.xu.api.model.common.PageDTO;
import cn.xu.api.model.user.LoginFormDTO;
import cn.xu.types.model.ResponseEntity;

public interface IUserServiceController {

    ResponseEntity loginByAdmin(LoginFormDTO loginFormDTO);

    ResponseEntity getInfoByToken(String token);

    ResponseEntity queryUserList(PageDTO pageDTO);

    ResponseEntity queryAdminList(PageDTO pageDTO);

}
