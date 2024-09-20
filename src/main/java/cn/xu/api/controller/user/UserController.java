package cn.xu.api.controller.user;

import cn.xu.api.dto.request.user.LoginOrRegisterDTO;
import cn.xu.common.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class UserController {
    public ResponseEntity loginOrRegister(LoginOrRegisterDTO loginOrRegisterDTO) {
        return null;
    }
}
