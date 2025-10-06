package cn.xu.api.system.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.system.model.dto.user.LoginFormRequest;
import cn.xu.api.system.model.dto.user.SysUserRequest;
import cn.xu.api.system.model.dto.user.UserPasswordRequest;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.request.PageRequest;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.vo.LoginFormResponse;
import cn.xu.domain.user.service.IUserLoginService;
import cn.xu.domain.user.service.IUserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequestMapping("/system")
@RestController
@Tag(name = "后台用户管理", description = "后台用户相关接口")
public class SysUserController {

    @Resource
    private IUserLoginService userLoginService;
    @Resource
    private IUserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    @ApiOperationLog(description = "管理员登录")
    public ResponseEntity<String> loginByAdmin(@RequestBody @Valid LoginFormRequest loginFormRequest) {
        log.info("管理员登录: {}", loginFormRequest);
        if (StringUtils.isEmpty(loginFormRequest.getUsername()) || StringUtils.isEmpty(loginFormRequest.getPassword())) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "管理员登录参数为空");
        }
        String token = userLoginService.loginByAdmin(new LoginFormResponse(loginFormRequest.getUsername(), loginFormRequest.getPassword()));
        log.info("管理员登录成功: {}", token);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(token)
                .build();
    }

    @GetMapping("/user/info")
    @Operation(summary = "获取用户信息")
    @SaCheckLogin
    @ApiOperationLog(description = "获取用户信息")
    public ResponseEntity<UserInfoEntity> getInfoByToken(@Parameter(description = "认证令牌") @RequestHeader(required = false, value = "Authorization")
                                                         String token) {
        log.info("获取用户信息: {}", token);
        
        // 处理 token 前缀，使其更加健壮
        String actualToken = token;
        if (token != null && token.startsWith("Bearer ")) {
            actualToken = token.substring(7); // "Bearer ".length() = 7
        }
        
        UserInfoEntity userInfo = userLoginService.getInfoByToken(actualToken);
        return ResponseEntity.<UserInfoEntity>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取用户信息成功")
                .data(userInfo)
                .build();
    }

    @GetMapping("/logout")
    @Operation(summary = "退出登录")
    @ApiOperationLog(description = "退出登录")
    public ResponseEntity<String> logout() {
        StpUtil.logout();
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("退出成功")
                .build();
    }

    @PostMapping(value = "/updatePassword")
    @Operation(summary = "修改密码")
    @ApiOperationLog(description = "修改密码")
    public ResponseEntity<Void> updatePassword(@RequestBody @Valid UserPasswordRequest userPasswordRequest) {
        userService.changePassword(userPasswordRequest.getUserId(), userPasswordRequest.getOldPassword(), userPasswordRequest.getNewPassword());
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("密码修改成功")
                .build();
    }

    @GetMapping("/user/list")
    @Operation(summary = "查询用户列表")
    @ApiOperationLog(description = "查询用户列表")
    public ResponseEntity<List<UserEntity>> queryUserList(PageRequest pageRequest) {
        List<UserEntity> users = userService.queryUserList(pageRequest);
        return ResponseEntity.<List<UserEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询成功")
                .data(users)
                .build();
    }

    @PostMapping("/user/add")
    @Operation(summary = "添加用户")
    @ApiOperationLog(description = "添加用户")
    public ResponseEntity<Void> addUser(@RequestBody @Valid SysUserRequest userRequest) {
        userService.addUser(userRequest);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("添加成功")
                .build();
    }

    @PostMapping("/user/update")
    @Operation(summary = "更新用户")
    @ApiOperationLog(description = "更新用户")
    public ResponseEntity<Void> updateUser(@RequestBody @Valid SysUserRequest userRequest) {
        userService.updateUser(userRequest);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新成功")
                .build();
    }

    @PostMapping("/user/delete/{id}")
    @Operation(summary = "删除用户")
    @ApiOperationLog(description = "删除用户")
    public ResponseEntity<Void> deleteUser(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    @PostMapping("/upload")
    @Operation(summary = "上传用户头像")
    @ApiOperationLog(description = "上传用户头像")
    public ResponseEntity<String> upload(@Parameter(description = "头像文件") @RequestParam("file") MultipartFile file) {
        String url = userService.uploadAvatar(file);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("上传成功")
                .data(url)
                .build();
    }
}