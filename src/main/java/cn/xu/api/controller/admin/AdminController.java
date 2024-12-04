package cn.xu.api.controller.admin;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.dto.common.PageRequest;
import cn.xu.api.dto.user.LoginFormRequest;
import cn.xu.api.dto.user.UserRequest;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.entity.UserRoleEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.service.IUserLoginService;
import cn.xu.domain.user.service.IUserService;
import cn.xu.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@RequestMapping("/system")
@RestController
public class AdminController {

    @Resource
    private IUserLoginService userLoginService;
    @Resource
    private IUserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> loginByAdmin(@RequestBody LoginFormRequest loginFormRequest) {
        log.info("管理员登录: {}", loginFormRequest);
        if (StringUtils.isEmpty(loginFormRequest.getUsername()) || StringUtils.isEmpty(loginFormRequest.getPassword())) {
            throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode(), "管理员登录参数为空");
        }
        String token = userLoginService.loginByAdmin(new LoginFormVO(loginFormRequest.getUsername()
                , loginFormRequest.getPassword()));
        return ResponseEntity.<String>builder()
                .data(token)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("管理员登录成功")
                .build();
    }

    @GetMapping("/info")
    public ResponseEntity<UserInfoEntity> getInfoByToken(String token) {
        log.info("管理员获取信息: {}", token);
        if (StringUtils.isEmpty(token)) {
            throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode(), "管理员token为空");
        }
        UserInfoEntity userInfo = userLoginService.getInfoByToken(token);
        return ResponseEntity.<UserInfoEntity>builder()
                .data(userInfo)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("管理员获取信息成功")
                .build();
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(String token) {
        log.info("管理员退出: {}", token);
        if (StringUtils.isEmpty(token)) {
            throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode(), "管理员token为空");
        }
        Object userId = StpUtil.getLoginIdByToken(token);
        if (userId == null) {
            throw new AppException(Constants.UserErrorCode.ILLEGAL_TOKEN.getCode(),
                    Constants.UserErrorCode.ILLEGAL_TOKEN.getInfo());
        }
        StpUtil.logout(userId);
        return ResponseEntity.<String>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("管理员退出成功")
                .build();
    }


    @GetMapping("/user/list")
    public ResponseEntity queryUserList(@ModelAttribute PageRequest pageRequest) {
        int page = pageRequest.getPage();
        int size = pageRequest.getSize();
        log.info("查询用户列表: page={}, size={}", page, size);
        page = page < 1 ? 1 : page;
        size = size < 1 ? 10 : size;
        List<UserEntity> userEntityList = userService.queryUserList(page, size);
        return ResponseEntity.<List<UserEntity>>builder()
                .data(userEntityList)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("查询用户列表成功")
                .build();
    }

    @GetMapping("/admin/list")
    public ResponseEntity queryAdminList(@ModelAttribute PageRequest pageRequest) {
        int page = pageRequest.getPage();
        int size = pageRequest.getSize();
        log.info("查询管理员列表: page={}, size={}", page, size);
        page = page < 1 ? 1 : page;
        size = size < 1 ? 10 : size;
        List<UserEntity> userEntityList = userService.queryAdminList(page, size);
        return ResponseEntity.<List<UserEntity>>builder()
                .data(userEntityList)
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("查询管理员列表成功")
                .build();
    }

    @PostMapping("/user/add")
    public ResponseEntity addUser(@RequestBody UserRequest userRequest) {
        log.info("添加用户: {}", userRequest);
        if (StringUtils.isEmpty(userRequest.getUsername()) || StringUtils.isEmpty(userRequest.getPassword())
                || StringUtils.isEmpty(userRequest.getEmail())) {
            throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode(), "添加用户参数为空");
            }
        if (userRequest.getRoleId() == null) {
            throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode(), "角色不能为空");
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
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("添加用户成功")
                .build();
    }

    @PostMapping("/user/update")
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
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("更新用户成功")
                    .build();
        } else {
            return ResponseEntity.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("更新用户失败")
                    .build();
        }
    }

    @DeleteMapping("/user/delete")
    public ResponseEntity deleteUser(@RequestParam Long userId) {
        log.info("删除用户: {}", userId);
        userService.deleteUser(userId);
        return ResponseEntity.<String>builder()
                .code(Constants.ResponseCode.SUCCESS.getCode())
                .info("删除用户成功")
                .build();
    }

}
