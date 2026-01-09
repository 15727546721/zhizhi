package cn.xu.service.user;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.model.dto.user.UserLoginRequest;
import cn.xu.model.dto.user.UserRegisterRequest;
import cn.xu.model.entity.User;
import cn.xu.model.entity.UserSettings;
import cn.xu.repository.UserRepository;
import cn.xu.repository.UserSettingsRepository;
import cn.xu.event.publisher.UserEventPublisher;
import cn.xu.service.security.LoginSecurityService;
import cn.xu.service.security.VerificationCodeService;
import cn.xu.support.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 用户认证服务
 * <p>负责用户注册、登录、密码管理等认证相关功能</p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserAuthService {

    private final UserRepository userRepository;
    private final UserSettingsRepository userSettingsRepository;
    private final UserEventPublisher userEventPublisher;
    private final UserSettingsService userSettingsService;
    private final VerificationCodeService verificationCodeService;
    private final LoginSecurityService loginSecurityService;

    /**
     * 获取当前登录用户ID
     */
    public Long getCurrentUserId() {
        try {
            if (StpUtil.isLogin()) {
                return StpUtil.getLoginIdAsLong();
            }
        } catch (Exception e) {
            log.debug("[用户] 获取当前登录用户ID失败", e);
        }
        return null;
    }

    /**
     * 用户注册
     */
    @Transactional(rollbackFor = Exception.class)
    public void register(UserRegisterRequest request) {
        if (request == null) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "注册信息不能为空");
        }

        try {
            // 验证邮箱验证码
            if (request.getVerifyCode() != null && !request.getVerifyCode().isEmpty()) {
                verificationCodeService.verifyCodeOrThrow(
                        request.getEmail(),
                        request.getVerifyCode(),
                        VerificationCodeService.CodeScene.REGISTER);
            }

            // 验证邮箱是否存在
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "该邮箱已被注册");
            }

            // 创建新用户
            User newUser = User.createNewUser(
                    request.getEmail(),
                    request.getPassword(),
                    request.getUsername(),
                    request.getNickname()
            );
            userRepository.save(newUser);

            // 创建默认用户设置
            try {
                userSettingsService.getOrCreateDefaultSettings(newUser.getId());
            } catch (Exception e) {
                log.warn("为用户创建默认设置失败, userId: {}", newUser.getId());
            }

            // 发布用户注册事件
            userEventPublisher.publishRegistered(newUser.getId(), newUser.getNickname());
            log.info("用户注册成功, email: {}", request.getEmail());
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户注册失败", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "注册失败，请稍后重试");
        }
    }

    /**
     * 用户登录
     */
    public User login(UserLoginRequest request) {
        return loginWithIp(request, null);
    }

    /**
     * 用户登录（带IP检查）
     */
    public User loginWithIp(UserLoginRequest request, String clientIp) {
        if (request == null || StringUtils.isBlank(request.getEmail()) || StringUtils.isBlank(request.getPassword())) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "邮箱和密码不能为空");
        }

        String email = request.getEmail().trim().toLowerCase();

        try {
            // 登录前安全检查
            if (clientIp != null) {
                loginSecurityService.checkBeforeLogin(email, clientIp);
            }

            // 验证用户
            User user = userRepository.findByEmailWithPassword(email).orElse(null);
            if (user == null) {
                handleLoginFailure(email, clientIp);
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "邮箱或密码错误");
            }

            // 验证密码
            if (!user.verifyPassword(request.getPassword())) {
                handleLoginFailure(email, clientIp);
                throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(), "邮箱或密码错误");
            }

            // 检查用户状态
            if (user.getStatus() != User.STATUS_NORMAL) {
                throw new BusinessException(ResponseCode.USER_DISABLED.getCode(), "用户已被禁用");
            }

            // 登录成功处理
            loginSecurityService.clearLoginFailure(email);
            StpUtil.login(user.getId());
            updateLoginInfo(user, clientIp);

            log.info("用户登录成功, userId: {}, ip: {}", user.getId(), clientIp);
            return user;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户登录失败, email: {}", email, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "登录失败，请稍后重试");
        }
    }

    /**
     * 验证码登录
     */
    public User loginWithCode(String email, String code, String clientIp) {
        if (StringUtils.isBlank(email) || StringUtils.isBlank(code)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "邮箱和验证码不能为空");
        }

        String trimmedEmail = email.trim().toLowerCase();

        try {
            // 验证验证码
            boolean verified = verificationCodeService.verifyCode(trimmedEmail, code, VerificationCodeService.CodeScene.LOGIN);
            if (!verified) {
                verified = verificationCodeService.verifyCode(trimmedEmail, code, VerificationCodeService.CodeScene.GENERAL);
            }
            if (!verified) {
                throw new BusinessException(40004, "验证码错误或已过期");
            }

            // 查找或自动注册用户
            User user = userRepository.findByEmail(trimmedEmail).orElse(null);
            if (user == null) {
                user = autoRegisterUser(trimmedEmail);
                log.info("新用户自动注册成功, userId: {}", user.getId());
            }

            // 检查用户状态
            if (user.getStatus() != User.STATUS_NORMAL) {
                throw new BusinessException(ResponseCode.USER_DISABLED.getCode(), "用户已被禁用");
            }

            // 执行登录
            StpUtil.login(user.getId());
            updateLoginInfo(user, clientIp);

            log.info("验证码登录成功, userId: {}, ip: {}", user.getId(), clientIp);
            return user;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("验证码登录失败, email: {}", trimmedEmail, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "登录失败，请稍后重试");
        }
    }

    /**
     * 用户名登录（管理员登录）
     */
    public User loginByUsername(String username, String password) {
        if (StringUtils.isBlank(username) || StringUtils.isBlank(password)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "用户名和密码不能为空");
        }

        try {
            User user = userRepository.findByUsername(username).orElse(null);
            if (user == null || !user.verifyPassword(password)) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户名或密码错误");
            }

            if (user.getStatus() != User.STATUS_NORMAL) {
                throw new BusinessException(ResponseCode.USER_DISABLED.getCode(), "用户已被禁用");
            }

            StpUtil.login(user.getId());
            updateLoginInfo(user, null);

            log.info("用户名登录成功, userId: {}", user.getId());
            return user;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("用户名登录失败, username: {}", username, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "登录失败，请稍后重试");
        }
    }

    /**
     * 修改密码
     */
    @Transactional(rollbackFor = Exception.class)
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        if (userId == null || StringUtils.isBlank(oldPassword) || StringUtils.isBlank(newPassword)) {
            throw new BusinessException(ResponseCode.NULL_PARAMETER.getCode(), "参数不能为空");
        }

        try {
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在"));

            if (!user.verifyPassword(oldPassword)) {
                throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(), "原密码错误");
            }

            user.updatePassword(newPassword);
            userRepository.save(user);
            log.info("用户修改密码成功, userId: {}", userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("修改密码失败, userId: {}", userId, e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "修改密码失败，请稍后重试");
        }
    }

    // ==================== 私有方法 ====================

    private void handleLoginFailure(String email, String clientIp) {
        if (clientIp != null) {
            int remaining = loginSecurityService.recordLoginFailure(email, clientIp);
            if (remaining > 0) {
                throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(),
                        "邮箱或密码错误，剩余尝试次数：" + remaining);
            } else {
                throw new BusinessException(ResponseCode.PASSWORD_ERROR.getCode(),
                        "登录失败次数过多，账户已被锁定");
            }
        }
    }

    private void updateLoginInfo(User user, String clientIp) {
        user.setLastLoginTime(LocalDateTime.now());
        if (clientIp != null) {
            user.setLastLoginIp(clientIp);
        }
        userRepository.save(user);
    }

    private User autoRegisterUser(String email) {
        String emailPrefix = email.substring(0, email.indexOf("@"));
        String username = emailPrefix;

        int retry = 0;
        while (userRepository.findByUsername(username).isPresent() && retry < 10) {
            username = emailPrefix + "_" + (1000 + new java.util.Random().nextInt(9000));
            retry++;
        }

        String nickname = "zhizhi_" + (100000 + new java.util.Random().nextInt(900000));
        String randomPassword = java.util.UUID.randomUUID().toString().substring(0, 16);

        User user = User.builder()
                .username(username)
                .email(email)
                .nickname(nickname)
                .password(randomPassword)
                .status(User.STATUS_NORMAL)
                .userType(User.USER_TYPE_NORMAL)
                .followCount(0L)
                .fansCount(0L)
                .likeCount(0L)
                .postCount(0L)
                .commentCount(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();

        user.encryptPassword();
        userRepository.save(user);

        UserSettings settings = UserSettings.createDefault(user.getId());
        userSettingsRepository.save(settings);

        return user;
    }
}
