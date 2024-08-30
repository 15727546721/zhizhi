package cn.xu.trigger.http.admin;

import cn.xu.api.IUserServiceController;
import cn.xu.api.model.user.LoginFormDTO;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.service.IUserLoginService;
import cn.xu.types.common.Constants;
import cn.xu.types.exception.AppException;
import cn.xu.types.model.ResponseEntity;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Slf4j
@RequestMapping("/admin")
@RestController
public class UserController implements IUserServiceController{

    @Resource
    private IUserLoginService userLoginService;

    @PostMapping("/login")
    @Override
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
    @Override
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
    public String logout() {
        return "logout success";
    }

    @PostMapping("/info")
    public String info() {
        return "info success";
    }

    @Override
    public String queryUserInfo(String req) {
        return null;
    }

    @Override
    public String updateUserInfo(String req) {
        return null;
    }

    @Override
    public String deleteUserInfo(String req) {
        return null;
    }

    @Override
    public String creatUserInfo(String req) {
        return null;
    }
}
