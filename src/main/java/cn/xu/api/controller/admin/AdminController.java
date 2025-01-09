package cn.xu.api.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.dto.common.PageRequest;
import cn.xu.api.dto.user.LoginFormRequest;
import cn.xu.api.dto.user.UserPasswordRequest;
import cn.xu.api.dto.user.UserRequest;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.service.IUserLoginService;
import cn.xu.domain.user.service.IUserService;
import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RequestMapping("/system")
@RestController
@Tag(name = "后台用户管理", description = "后台用户相关接口")
public class AdminController {

    @Resource
    private IUserLoginService userLoginService;
    @Resource
    private IUserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public ResponseEntity<String> loginByAdmin(@RequestBody LoginFormRequest loginFormRequest) {
        log.info("管理员登录: {}", loginFormRequest);
        if (StringUtils.isEmpty(loginFormRequest.getUsername()) || StringUtils.isEmpty(loginFormRequest.getPassword())) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "管理员登录参数为空");
        }
        String token = userLoginService.loginByAdmin(new LoginFormVO(loginFormRequest.getUsername(), loginFormRequest.getPassword()));
        log.info("管理员登录成功: {}", token);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(token)
                .build();
    }

    @GetMapping("/user/info")
    @Operation(summary = "获取用户信息")
    @SaCheckLogin
    public ResponseEntity<UserInfoEntity> getInfoByToken(@RequestHeader(required = false, value = "Authorization")
                                                             String token) {
        log.info("获取用户信息: {}", token);
        UserInfoEntity userInfo = userLoginService.getInfoByToken(token.substring("Bearer".length() + 1));
        return ResponseEntity.<UserInfoEntity>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取用户信息成功")
                .data(userInfo)
                .build();
    }

    @GetMapping("/logout")
    @Operation(summary = "退出登录")
    public ResponseEntity<String> logout() {
        StpUtil.logout();
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("退出成功")
                .build();
    }

    @PostMapping(value = "/updatePassword")
    @Operation(summary = "修改密码")
    public ResponseEntity<Void> updatePassword(@RequestBody UserPasswordRequest userPasswordRequest) {
        userService.changePassword(userPasswordRequest.getUserId(), userPasswordRequest.getOldPassword(), userPasswordRequest.getNewPassword());
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("密码修改成功")
                .build();
    }

    @GetMapping("/user/list")
    @Operation(summary = "查询用户列表")
    public ResponseEntity<List<UserEntity>> queryUserList(PageRequest pageRequest) {
        List<UserEntity> users = userService.queryUserList(pageRequest);
        return ResponseEntity.<List<UserEntity>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(users)
                .build();
    }

    @PostMapping("/user/add")
    @Operation(summary = "添加用户")
    public ResponseEntity<Void> addUser(@RequestBody UserRequest userRequest) {
        userService.addUser(userRequest);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("用户添加成功")
                .build();
    }

    @PostMapping("/user/update")
    @Operation(summary = "更新用户")
    public ResponseEntity<Void> updateUser(@RequestBody UserRequest userRequest) {
        userService.updateUser(userRequest);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("用户更新成功")
                .build();
    }

    @DeleteMapping("/user/delete")
    @Operation(summary = "删除用户")
    public ResponseEntity<Void> deleteUser(@RequestParam Long userId) {
        userService.deleteUser(userId);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("用户删除成功")
                .build();
    }

    @GetMapping("/user/profile/info")
    @Operation(summary = "查询用户个人信息")
    public ResponseEntity<UserInfoEntity> queryUserInfo() {
        UserInfoEntity userInfo = userService.queryUserInfo();
        return ResponseEntity.<UserInfoEntity>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(userInfo)
                .build();
    }

    @PostMapping("/user/profile/update")
    @Operation(summary = "更新用户个人信息")
    public ResponseEntity<Void> updateUserInfo(@RequestBody UserInfoEntity userInfoEntity) {
        userService.updateUserInfo(userInfoEntity);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("用户个人信息更新成功")
                .build();
    }

    @PostMapping("/user/profile/avatar")
    @Operation(summary = "上传头像")
    public ResponseEntity<Void> uploadAvatar(String avatar) {
        userService.uploadAvatar(avatar);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("头像上传成功")
                .build();
    }

    @GetMapping("/user/info/roles")
    @Operation(summary = "获取用户信息及角色")
    @SaCheckLogin
    public ResponseEntity<UserInfoEntity> getUserInfoAndRoles() {
        log.info("获取当前登录用户信息及角色");
        UserInfoEntity userInfo = userLoginService.getCurrentUserInfoAndRoles();
        return ResponseEntity.<UserInfoEntity>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取用户信息及角色成功")
                .data(userInfo)
                .build();
    }
}
