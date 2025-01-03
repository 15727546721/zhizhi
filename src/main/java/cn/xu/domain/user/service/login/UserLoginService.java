package cn.xu.domain.user.service.login;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.domain.user.constant.UserErrorCode;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserLoginService;
import cn.xu.exception.AppException;
import cn.xu.infrastructure.common.ResponseCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;

@Slf4j
@Service
public class UserLoginService implements IUserLoginService {

    @Resource
    private IUserRepository userRepository;

    @Override
    public String loginByAdmin(LoginFormVO loginFormVO) {
        if (ObjectUtils.isEmpty(loginFormVO)) {
            throw new AppException(ResponseCode.NULL_PARAMETER.getCode(), "登录参数不能为空");
        }
        String password = SaSecureUtil.sha256(loginFormVO.getPassword());
        LoginFormVO userByUsername = userRepository.findUserByUsername(loginFormVO.getUsername());
        if (ObjectUtils.isEmpty(userByUsername)) {
            throw new AppException(ResponseCode.NULL_RESPONSE.getCode(), "该用户不存在");
        }
        if (!userByUsername.getPassword().equals(password)) {
            throw new AppException(ResponseCode.UN_ERROR.getCode(), "密码错误");
        }
//        if (!userByUsername.getRole().equals("admin")) {
//            throw new AppException(ResponseCode.UN_ERROR.getCode(), "权限不足");
//        }
        // 登录
        StpUtil.login(userByUsername.getId());
        // 返回token
        return StpUtil.getTokenValue();
    }

    @Override
    public UserInfoEntity getInfoByToken(String token) {
        Object userId = StpUtil.getLoginIdByToken(token);
        if (ObjectUtils.isEmpty(userId)) {
            throw new AppException(UserErrorCode.ILLEGAL_TOKEN.getCode(),
                    UserErrorCode.ILLEGAL_TOKEN.getMessage());
        }
        UserInfoEntity userInfo = userRepository.findUserInfoById(Long.valueOf(userId.toString()));
        log.info("用户信息:{}", userInfo);
        return userInfo;
    }

}
