package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.user.UpdateUserRequest;
import cn.xu.api.web.model.dto.user.UserLoginRequest;
import cn.xu.api.web.model.dto.user.UserRegisterRequest;
import cn.xu.api.web.model.vo.user.UserLoginResponse;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.domain.user.service.UserValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("api/user")
@RestController
@Tag(name = "用户接口", description = "用户相关接口")
public class UserController {

    @Resource
    private IUserService userService;
    
    @Resource
    private UserValidationService userValidationService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    @ApiOperationLog(description = "用户注册")
    public ResponseEntity<Void> register(@RequestBody UserRegisterRequest registerRequest) {
        // 参数校验
        userValidationService.validateRegisterParams(
            registerRequest.getUsername(),
            registerRequest.getPassword(),
            registerRequest.getConfirmPassword(),
            registerRequest.getEmail()
        );

        // 注册用户
        userService.register(registerRequest);

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("用户注册成功")
                .build();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    @ApiOperationLog(description = "用户登录")
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest loginRequest) {
        // 参数校验
        userValidationService.validateLoginParams(
            loginRequest.getEmail(),
            loginRequest.getPassword()
        );
        
        UserEntity user = userService.login(loginRequest);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        StpUtil.login(user.getId());
        log.info("用户登录成功，用户ID：{}", user.getId());
        return ResponseEntity.<UserLoginResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(UserLoginResponse.builder().userInfo(user).token(StpUtil.getTokenValue()).build())
                .info("用户登录成功")
                .build();
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "用户退出接口")
    @ApiOperationLog(description = "用户退出")
    public ResponseEntity<Void> logout() {
        long useId = StpUtil.getLoginIdAsLong();
        StpUtil.logout();
        log.info("用户退出成功: {}", useId);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("退出成功")
                .build();
    }

    @GetMapping("/info/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    @ApiOperationLog(description = "获取用户信息")
    public ResponseEntity<UserEntity> getUserInfo(@Parameter(description = "用户ID") @PathVariable Long id) {
        log.info("获取用户信息，用户ID：{}", id);
        UserEntity user = userService.getUserInfo(id);
        return ResponseEntity.<UserEntity>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(user)
                .build();
    }

    @PostMapping("/update")
    @ApiOperationLog(description = "更新用户信息")
    @SaCheckLogin
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserRequest user) {
        long userId = StpUtil.getLoginIdAsLong();
        if (userId != user.getId()) {
            throw new BusinessException("只能更新自己的信息");
        }
        userService.update(user);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("用户信息更新成功")
                .build();
    }
}