package cn.xu.domain.user.service.login;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.application.common.ResponseCode;
import cn.xu.domain.user.constant.UserErrorCode;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.LoginFormVO;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserLoginService;
import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

@Slf4j
@Service
public class UserLoginService implements IUserLoginService {

    @Resource
    private IUserRepository userRepository;

    @Override
    public String loginByAdmin(LoginFormVO loginFormVO) {
        if (ObjectUtils.isEmpty(loginFormVO)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "登录参数不能为空");
        }

        UserEntity user = userRepository.findByUsername(loginFormVO.getUsername())
                .orElseThrow(() -> new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "该用户不存在"));

        String password = SaSecureUtil.sha256(loginFormVO.getPassword());
        if (!user.getPassword().equals(password)) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "密码错误");
        }

        // 登录
        StpUtil.login(user.getId());
        // 返回token
        return StpUtil.getTokenValue();
    }

    @Override
    public UserInfoEntity getInfoByToken(String token) {
        Object userId = StpUtil.getLoginIdByToken(token);
        if (ObjectUtils.isEmpty(userId)) {
            throw new BusinessException(UserErrorCode.ILLEGAL_TOKEN.getCode(),
                    UserErrorCode.ILLEGAL_TOKEN.getMessage());
        }

        UserEntity user = userRepository.findById(Long.valueOf(userId.toString()))
                .orElseThrow(() -> new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "用户不存在"));

        return convertToUserInfoEntity(user);
    }

    private UserInfoEntity convertToUserInfoEntity(UserEntity user) {
        return UserInfoEntity.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .phone(user.getPhone())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                .description(user.getDescription())
                .status(user.getStatus())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    @Override
    public UserInfoEntity getCurrentUserInfoAndRoles() {
        // 获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        if (userId == null) {
            throw new BusinessException(UserErrorCode.ILLEGAL_TOKEN.getCode(),
                    UserErrorCode.ILLEGAL_TOKEN.getMessage());
        }

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "用户不存在"));

        List<String> roles = userRepository.findRolesByUserId(userId);

        UserInfoEntity userInfo = convertToUserInfoEntity(user);
        userInfo.setRoles(roles);

        return userInfo;
    }
}
