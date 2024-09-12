package cn.xu.trigger.http.user;

import cn.xu.api.IUserServiceController;
import cn.xu.api.model.user.LoginOrRegisterDTO;
import cn.xu.types.model.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/user")
@RestController
public class UserController implements IUserServiceController {
    @Override
    public ResponseEntity loginOrRegister(LoginOrRegisterDTO loginOrRegisterDTO) {
        return null;
    }
}
