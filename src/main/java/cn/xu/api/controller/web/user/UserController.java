package cn.xu.api.controller.web.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("api/user")
@RestController
public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody RegisterRequest registerRequest) {
        if (!registerRequest.getPassword().equals(registerRequest.getConfirmPassword())) {
            return ResponseEntity.builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("两次密码输入不一致")
                    .build();
        }
        userService.register(registerRequest);
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("用户注册成功")
                .build();
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginDTO> login(@RequestBody LoginRequest loginRequest) {
        UserEntity user = userService.login(loginRequest);
        if (user == null) {
            return ResponseEntity.<UserLoginDTO>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("用户名或密码错误")
                    .build();
        }
        StpUtil.login(user.getId());
        log.info("用户登录成功，用户ID：{}", user.getId());
        return ResponseEntity.<UserLoginDTO>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(UserLoginDTO.builder().userInfo(user).token(StpUtil.getTokenValue()).build())
                .info("用户登录成功")
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity logout() {
        StpUtil.logout();
        return ResponseEntity.builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("用户登出成功")
                .build();
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<UserEntity> getUserInfo(@PathVariable Long id) {
        UserEntity userEntity = userService.getUserInfo(id);
        return ResponseEntity.<UserEntity>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .data(userEntity)
                .build();
    }
}
