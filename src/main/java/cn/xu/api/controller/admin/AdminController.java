package cn.xu.api.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.dto.request.common.PageDTO;
import cn.xu.api.dto.request.user.LoginFormDTO;
import cn.xu.api.dto.request.user.UserDTO;
import cn.xu.common.Constants;
import cn.xu.common.ResponseEntity;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
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
@RequestMapping("/admin")
@RestController
public class AdminController{

    @Resource
    private IUserLoginService userLoginService;
    @Resource
    private IUserService userService;

    @PostMapping("/login")
    public ResponseEntity<String> loginByAdmin(@RequestBody LoginFormDTO loginFormDTO) {
        log.info("管理员登录: {}", loginFormDTO);
        if (StringUtils.isEmpty(loginFormDTO.getUsername()) || StringUtils.isEmpty(loginFormDTO.getPassword())) {
            throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode(), "管理员登录参数为空");
        }
        String token = userLoginService.loginByAdmin(new LoginFormVO(loginFormDTO.getUsername()
                , loginFormDTO.getPassword()));
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
    public ResponseEntity queryUserList(@ModelAttribute PageDTO pageDTO) {
        int page = pageDTO.getPage();
        int size = pageDTO.getSize();
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
    public ResponseEntity queryAdminList(@ModelAttribute PageDTO pageDTO) {
        int page = pageDTO.getPage();
        int size = pageDTO.getSize();
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
    public ResponseEntity addUser(@RequestBody UserDTO userDTO) {
        log.info("添加用户: {}", userDTO);
        UserEntity userEntity = UserEntity.builder()
                .username(userDTO.getUsername())
                .email(userDTO.getEmail())
                .status(userDTO.getStatus())
                .nickname(userDTO.getNickname())
                .build();

        int result = userService.addUser(userEntity);
        if (result > 0) {
            return ResponseEntity.<String>builder()
                    .code(Constants.ResponseCode.SUCCESS.getCode())
                    .info("添加用户成功")
                    .build();
        } else {
            return ResponseEntity.<String>builder()
                    .code(Constants.ResponseCode.UN_ERROR.getCode())
                    .info("添加用户失败")
                    .build();
        }
    }

    @PostMapping("/user/update")
    public ResponseEntity updateUser(@RequestBody UserDTO userDTO) {
        log.info("更新用户: {}", userDTO);
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(userDTO.getUsername());
        userEntity.setPassword(userDTO.getPassword());
        userEntity.setEmail(userDTO.getEmail());
        userEntity.setNickname(userDTO.getNickname());
        userEntity.setStatus(userDTO.getStatus());

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
