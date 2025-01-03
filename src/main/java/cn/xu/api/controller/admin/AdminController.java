package cn.xu.api.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.dto.common.PageRequest;
import cn.xu.api.dto.user.LoginFormRequest;
import cn.xu.api.dto.user.UserPasswordRequest;
import cn.xu.api.dto.user.UserRequest;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.user.constant.UserErrorCode;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.entity.UserRoleEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.service.IUserLoginService;
import cn.xu.domain.user.service.IUserService;
import cn.xu.exception.AppException;
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
            throw new AppException(ResponseCode.NULL_PARAMETER.getCode(), "管理员登录参数为空");
        }
        String token = userLoginService.loginByAdmin(new LoginFormVO(loginFormRequest.getUsername()
                , loginFormRequest.getPassword()));
        return ResponseEntity.<String>builder()
                .data(token)
                .code(ResponseCode.SUCCESS.getCode())
                .info("管理员登录成功")
                .build();
    }

    @GetMapping("user/info")
    @Operation(summary = "获取用户信息")
    @SaCheckLogin
    public ResponseEntity<UserInfoEntity> getInfoByToken(@RequestParam(required = false) String token) {
        log.info("管理员获取信息: {}", token);
        if (StringUtils.isEmpty(token)) {
            token = StpUtil.getTokenValue();
//            throw new AppException(ResponseCode.NULL_PARAMETER.getCode(), "管理员token为空");
        }

        UserInfoEntity userInfo = userLoginService.getInfoByToken(token);
        return ResponseEntity.<UserInfoEntity>builder()
                .data(userInfo)
                .code(ResponseCode.SUCCESS.getCode())
                .info("管理员获取信息成功")
                .build();
    }

    @PostMapping("/logout")
    @Operation(summary = "退出登录")
    public ResponseEntity<String> logout(String token) {
        log.info("管理员退出: {}", token);
        if (StringUtils.isEmpty(token)) {
            throw new AppException(ResponseCode.NULL_PARAMETER.getCode(), "管理员token为空");
        }
        Object userId = StpUtil.getLoginIdByToken(token);
        if (userId == null) {
            throw new AppException(UserErrorCode.ILLEGAL_TOKEN.getCode(),
                    UserErrorCode.ILLEGAL_TOKEN.getMessage());
        }
        StpUtil.logout(userId);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("管理员退出成功")
                .build();
    }

    @GetMapping("/logout")
    @Operation(summary = "退出登录")
    public ResponseEntity<String> logout() {
        log.info("管理员退出 {}", StpUtil.getLoginId());
        StpUtil.logout();
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("管理员退出成功")
                .build();
    }

    @PostMapping(value = "/updatePassword")
    @Operation(summary = "修改密码")
    public ResponseEntity updatePassword(@RequestBody UserPasswordRequest userPasswordRequest) {
        userService.updatePassword(userPasswordRequest);
        return ResponseEntity.builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("修改密码成功")
                .build();
    }

    @GetMapping("/user/list")
    @Operation(summary = "查询用户列表")
    public ResponseEntity queryUserList(PageRequest pageRequest) {
        int page = pageRequest.getPage();
        int size = pageRequest.getSize();
        log.info("查询用户列表: page={}, size={}", page, size);
        page = page < 1 ? 1 : page;
        size = size < 1 ? 10 : size;
        List<UserEntity> userEntityList = userService.queryUserList(page, size);
        return ResponseEntity.<List<UserEntity>>builder()
                .data(userEntityList)
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询用户列表成功")
                .build();
    }

    @PostMapping("/user/add")
    @Operation(summary = "添加用户")
    public ResponseEntity addUser(@RequestBody UserRequest userRequest) {
        log.info("添加用户: {}", userRequest);
        if (StringUtils.isEmpty(userRequest.getUsername()) || StringUtils.isEmpty(userRequest.getPassword())
                || StringUtils.isEmpty(userRequest.getEmail())) {
            throw new AppException(ResponseCode.NULL_PARAMETER.getCode(), "添加用户参数为空");
        }
        if (userRequest.getRoleId() == null) {
            throw new AppException(ResponseCode.NULL_PARAMETER.getCode(), "角色不能为空");
        }
        userService.addUser(UserRoleEntity.builder()
                .username(userRequest.getUsername())
                .password(SaSecureUtil.sha256(userRequest.getPassword()))
                .email(userRequest.getEmail())
                .status(userRequest.getStatus())
                .nickname(userRequest.getNickname())
                .avatar(userRequest.getAvatar())
                .roleId(userRequest.getRoleId())
                .build());

        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("添加用户成功")
                .build();
    }

    @PostMapping("/user/update")
    @Operation(summary = "更新用户")
    public ResponseEntity updateUser(@RequestBody UserRequest userRequest) {
        log.info("更新用户: {}", userRequest);
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userRequest.getUsername());
        userEntity.setPassword(userRequest.getPassword());
        userEntity.setEmail(userRequest.getEmail());
        userEntity.setNickname(userRequest.getNickname());
        userEntity.setStatus(userRequest.getStatus());

        int result = userService.updateUser(userEntity);
        if (result > 0) {
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("更新用户成功")
                    .build();
        } else {
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("更新用户失败")
                    .build();
        }
    }

    @DeleteMapping("/user/delete")
    @Operation(summary = "删除用户")
    public ResponseEntity deleteUser(@RequestParam Long userId) {
        log.info("删除用户: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除用户成功")
                .build();
    }

    /**
     * 查询用户个人信息
     */
    @GetMapping("/user/profile/info")
    @Operation(summary = "查询用户个人信息")
    public ResponseEntity queryUserInfo() {
        log.info("查询用户个人信息");
        Long id = StpUtil.getLoginIdAsLong();
        UserInfoEntity userInfoEntity = userService.queryUserInfo(id);
        return ResponseEntity.<UserInfoEntity>builder()
                .data(userInfoEntity)
                .code(ResponseCode.SUCCESS.getCode())
                .info("查询用户个人信息成功")
                .build();
    }

    /**
     * 更新用户个人信息
     */
    @PostMapping("/user/profile/update")
    @Operation(summary = "更新用户个人信息")
    public ResponseEntity updateUserInfo(@RequestBody UserInfoEntity userInfoEntity) {
        log.info("更新用户个人信息: {}", userInfoEntity);
        Long id = StpUtil.getLoginIdAsLong();
        userInfoEntity.setId(id);
        userService.updateUserInfo(userInfoEntity);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新用户个人信息成功")
                .build();
    }

    /**
     * 上传头像
     */
    @PostMapping("/user/profile/avatar")
    @Operation(summary = "上传头像")
    public ResponseEntity uploadAvatar(String avatar) {
        log.info("上传头像: {}", avatar);
        Long id = StpUtil.getLoginIdAsLong();
        userService.uploadAvatar(id, avatar);
        return ResponseEntity.<String>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("上传头像成功")
                .build();
    }
}
