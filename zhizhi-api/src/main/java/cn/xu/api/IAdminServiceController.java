package cn.xu.api;

import cn.xu.api.model.common.PageDTO;
import cn.xu.api.model.user.LoginFormDTO;
import cn.xu.api.model.user.UserDTO;
import cn.xu.types.model.ResponseEntity;

public interface IAdminServiceController {

    ResponseEntity loginByAdmin(LoginFormDTO loginFormDTO);

    ResponseEntity getInfoByToken(String token);

    ResponseEntity queryUserList(PageDTO pageDTO);

    ResponseEntity queryAdminList(PageDTO pageDTO);

    // 添加用户
    ResponseEntity addUser(UserDTO userDTO);

    // 修改用户信息
    ResponseEntity updateUser(UserDTO userDTO);

    // 删除用户
    ResponseEntity deleteUser(Long userId);

}
