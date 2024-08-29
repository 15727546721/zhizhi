package cn.xu.trigger.http.admin;

import cn.xu.api.IUserService;
import cn.xu.types.model.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RequestMapping("/admin")
@RestController
public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping("/login")
    public ResponseEntity login() {

        return ResponseEntity.builder().build();
    }

    @PostMapping("/logout")
    public String logout() {
        return "logout success";
    }

    @PostMapping("/info")
    public String info() {
        return "info success";
    }
}
