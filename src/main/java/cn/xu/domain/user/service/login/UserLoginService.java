package cn.xu.domain.user.service.login;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.exception.BusinessException;
import cn.xu.domain.user.constant.UserErrorCode;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.model.entity.UserInfoEntity;
import cn.xu.domain.user.model.valobj.Password;
import cn.xu.domain.user.model.vo.LoginFormResponse;
import cn.xu.domain.user.model.vo.UserFormResponse;
import cn.xu.domain.user.repository.IUserRepository;
import cn.xu.domain.user.service.IUserLoginService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import javax.annotation.Resource;
import java.util.List;

/**
 * 用户登录服务实现类
 * 
 * @author Lily
 */
@Slf4j
@Service
public class UserLoginService implements IUserLoginService {

    @Resource
    private IUserRepository userRepository;

    @Override
    public String loginByAdmin(LoginFormResponse loginFormResponse) {
        if (ObjectUtils.isEmpty(loginFormResponse)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "登录参数不能为空");
        }
        UserFormResponse userFormVO = userRepository.findUsernameAndPasswordByUsername(loginFormResponse.getUsername());
        if (ObjectUtils.isEmpty(userFormVO)) {
            throw new BusinessException(UserErrorCode.USER_NOT_FOUND.getCode(), "用户不存在");
        }
        
        // 使用Password.matches方法验证密码
        if (!Password.matches(loginFormResponse.getPassword(), userFormVO.getPassword())) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "密码错误");
        }

        // 登录
        StpUtil.login(userFormVO.getId());
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
                .orElse(null);
        if (user == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "用户不存在");
        }

        return convertToUserInfoEntity(user);
    }

    /**
     * 将用户实体转换为用户信息实体
     *
     * @param user 用户实体
     * @return 用户信息实体
     */
    private UserInfoEntity convertToUserInfoEntity(UserEntity user) {
        return UserInfoEntity.builder()
                .id(user.getId())
                .username(user.getUsernameValue())
                .email(user.getEmailValue())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .phone(user.getPhoneValue())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                .description(user.getDescription())
                .status(user.getStatusCode())
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
                .orElse(null);
        if (user == null) {
            throw new BusinessException(ResponseCode.NULL_RESPONSE.getCode(), "用户不存在");
        }

        List<String> roles = userRepository.findRolesByUserId(userId);

        UserInfoEntity userInfo = convertToUserInfoEntity(user);
        userInfo.setRoles(roles);

        return userInfo;
    }
}