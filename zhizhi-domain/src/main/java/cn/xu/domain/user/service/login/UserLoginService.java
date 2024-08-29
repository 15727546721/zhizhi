package cn.xu.domain.user.service.login;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserLoginService;
import cn.xu.types.common.Constants;
import cn.xu.types.exception.AppException;
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
            throw new AppException(Constants.ResponseCode.NULL_PARAMETER.getCode(), "登录参数不能为空");
        }
        String password = SaSecureUtil.sha256(loginFormVO.getPassword());
        LoginFormVO userByUsername = userRepository.findUserByUsername(loginFormVO.getUsername());
        if (ObjectUtils.isEmpty(userByUsername)) {
            throw new AppException(Constants.ResponseCode.NULL_RESPONSE.getCode(), "该用户不存在");
        }
        if (!userByUsername.getPassword().equals(password)) {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "密码错误");
        }
        if (!userByUsername.getRole().equals("admin")) {
            throw new AppException(Constants.ResponseCode.UN_ERROR.getCode(), "权限不足");
        }
        // 登录
        StpUtil.login(userByUsername.getId());
        // 返回token
        return StpUtil.getTokenValue();
    }

    @Override
    public UserEntity getInfoByToken(String token) {
        Object userId = StpUtil.getLoginIdByToken(token);
        if (ObjectUtils.isEmpty(userId)) {
            throw new AppException(Constants.ResponseCode.ILLEGAL_PARAMETER.getCode(), "token无效");
        }
        UserEntity userEntity = userRepository.findUserById(Long.valueOf(userId.toString()));
        log.info("用户信息:{}", userEntity);
        return userEntity;
    }

}
