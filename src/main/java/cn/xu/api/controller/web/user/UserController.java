package cn.xu.api.controller.web.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.dto.common.ResponseEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.valueobject.Email;
import cn.xu.domain.user.service.IUserService;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@Slf4j
@RequestMapping("api/user")
@RestController
public class UserController {

    @Resource
    private IUserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    public ResponseEntity<Void> register(@RequestBody @Valid RegisterRequest registerRequest) {
        // 参数校验
        validateRegisterRequest(registerRequest);

        // 注册用户
        UserEntity user = userService.register(registerRequest);

        // 自动登录
        StpUtil.login(user.getId());

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("用户注册成功")
                .build();
    }

    /**
     * 校验注册请求参数
     */
    private void validateRegisterRequest(RegisterRequest request) {
        // 密码一致性校验
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException("两次密码输入不一致");
        }

        // 密码强度校验
        if (request.getPassword().length() < 6 || request.getPassword().length() > 20) {
            throw new BusinessException("密码长度必须在6-20个字符之间");
        }

        // 用户名格式校验
        if (request.getUsername().length() < 4 || request.getUsername().length() > 20) {
            throw new BusinessException("用户名长度必须在4-20个字符之间");
        }

        // 邮箱格式校验（使用Email值对象进行校验）
        try {
            new Email(request.getEmail());
        } catch (BusinessException e) {
            throw new BusinessException("邮箱格式不正确");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginDTO> login(@RequestBody LoginRequest loginRequest) {
        UserEntity user = userService.login(loginRequest);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        StpUtil.login(user.getId());
        log.info("用户登录成功，用户ID：{}", user.getId());
        return ResponseEntity.<UserLoginDTO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(UserLoginDTO.builder().userInfo(user).token(StpUtil.getTokenValue()).build())
                .info("用户登录成功")
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        StpUtil.logout();
        log.info("用户退出成功: {}", StpUtil.getLoginIdAsLong());
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("退出成功")
                .build();
    }

    @GetMapping("/info/{id}")
    public ResponseEntity<UserEntity> getUserInfo(@PathVariable Long id) {
        UserEntity user = userService.getUserInfo(id);
        return ResponseEntity.<UserEntity>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(user)
                .build();
    }
}
