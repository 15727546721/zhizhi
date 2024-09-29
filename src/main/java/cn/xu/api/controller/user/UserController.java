package cn.xu.api.controller.user;

import cn.xu.api.dto.user.LoginOrRegisterRequest;
import cn.xu.common.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class UserController {
    public ResponseEntity loginOrRegister(LoginOrRegisterRequest loginOrRegisterRequest) {
        return null;
    }
}
