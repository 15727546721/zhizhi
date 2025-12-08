package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.integration.mail.EmailService;
import cn.xu.model.dto.user.*;
import cn.xu.model.entity.User;
import cn.xu.model.entity.UserSettings;
import cn.xu.model.vo.user.*;
import cn.xu.service.security.PasswordResetService;
import cn.xu.service.security.VerificationCodeService;
import cn.xu.service.user.IUserService;
import cn.xu.service.user.UserProfileService;
import cn.xu.service.user.UserServiceImpl;
import cn.xu.service.user.UserSettingsService;
import cn.xu.support.exception.BusinessException;
import cn.xu.support.util.IpUtils;
import cn.xu.support.util.LoginUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Collections;
import java.util.List;

/**
 * 用户控制器
 * <p>
 * 职责：用户认证、资料管理、账户安全
 * <ul>
 *   <li>认证：注册、登录、退出</li>
 *   <li>资料：查看、编辑、头像上传</li>
 *   <li>设置：隐私设置、通知设置</li>
 *   <li>安全：修改密码、重置密码、注销账户</li>
 * </ul>
 *
 */
@Slf4j
@RequestMapping("api/user")
@RestController
@Tag(name = "用户接口", description = "用户认证、资料管理、账户安全")
public class UserController {

    @Resource(name = "userService")
    private IUserService userService;

    @Resource
    private UserSettingsService userSettingsService;

    @Resource
    private UserProfileService userProfileService;

    @Resource
    private EmailService emailService;

    @Resource
    private VerificationCodeService verificationCodeService;

    @Resource
    private PasswordResetService passwordResetService;

    /**
     * 用户注册（邮箱+密码，username可选自动生成）
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "邮箱+密码注册，username可选")
    @ApiOperationLog(description = "用户注册")
    public ResponseEntity<Void> register(@RequestBody @Valid UserRegisterRequest registerRequest) {
        if (StringUtils.isBlank(registerRequest.getEmail())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱不能为空");
        }
        if (StringUtils.isBlank(registerRequest.getPassword())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "密码不能为空");
        }
        userService.register(registerRequest);
        return ResponseEntity.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info("用户注册成功").build();
    }

    /**
     * 用户登录（含IP安全检查：账户锁定、失败次数限制）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "邮箱+密码登录，含安全检查")
    @ApiOperationLog(description = "用户登录")
    public ResponseEntity<UserLoginVO> login(@RequestBody @Valid UserLoginRequest loginRequest,
                                             HttpServletRequest request) {
        if (StringUtils.isBlank(loginRequest.getEmail())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱不能为空");
        }
        if (StringUtils.isBlank(loginRequest.getPassword())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "密码不能为空");
        }
        String clientIp = IpUtils.getClientIp(request);
        User user = ((UserServiceImpl) userService).loginWithIp(loginRequest, clientIp);
        if (user == null) {
            throw new BusinessException("登录失败");
        }
        log.info("用户登录成功 - userId: {}, ip: {}", user.getId(), clientIp);
        return ResponseEntity.<UserLoginVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(UserLoginVO.builder().userInfo(convertToUserDetailVO(user)).token(StpUtil.getTokenValue()).build())
                .info("用户登录成功").build();
    }

    /**
     * 验证码登录
     */
    @PostMapping("/login-with-code")
    @Operation(summary = "验证码登录", description = "邮箱+验证码登录")
    @ApiOperationLog(description = "验证码登录")
    public ResponseEntity<UserLoginVO> loginWithCode(@RequestBody @Valid CodeLoginRequest loginRequest,
                                                     HttpServletRequest request) {
        if (StringUtils.isBlank(loginRequest.getEmail())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱不能为空");
        }
        if (StringUtils.isBlank(loginRequest.getVerifyCode())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "验证码不能为空");
        }
        String clientIp = IpUtils.getClientIp(request);
        User user = ((UserServiceImpl) userService).loginWithCode(loginRequest.getEmail(), loginRequest.getVerifyCode(), clientIp);
        if (user == null) {
            throw new BusinessException("登录失败");
        }
        log.info("验证码登录成功 - userId: {}, ip: {}", user.getId(), clientIp);
        return ResponseEntity.<UserLoginVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(UserLoginVO.builder().userInfo(convertToUserDetailVO(user)).token(StpUtil.getTokenValue()).build())
                .info("登录成功").build();
    }

    /**
     * 用户退出登录
     */
    @PostMapping("/logout")
    @Operation(summary = "用户退出")
    @ApiOperationLog(description = "用户退出")
    @SaCheckLogin
    public ResponseEntity<Void> logout() {
        long userId = LoginUserUtil.getLoginUserId();
        StpUtil.logout();
        log.info("用户退出 - userId: {}", userId);
        return ResponseEntity.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info("退出成功").build();
    }

    /**
     * 修改密码（需验证旧密码）
     */
    @PostMapping("/change-password")
    @Operation(summary = "修改密码")
    @ApiOperationLog(description = "修改密码")
    @SaCheckLogin
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "两次输入的密码不一致");
        }
        Long userId = LoginUserUtil.getLoginUserId();
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        log.info("密码修改成功 - userId: {}", userId);
        return ResponseEntity.<Void>builder().code(ResponseCode.SUCCESS.getCode()).info("密码修改成功").build();
    }

    /**
     * 上传头像（支持jpg/png/gif，最大2MB）
     */
    @PostMapping("/avatar/upload")
    @Operation(summary = "上传用户头像", description = "上传并更新当前登录用户的头像")
    @ApiOperationLog(description = "上传用户头像")
    @SaCheckLogin
    public ResponseEntity<String> uploadAvatar(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        try {
            if (file == null || file.isEmpty()) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件不能为空");
            }

            // 验证文件类型
            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件类型不支持");
            }

            // 验证文件大小
            if (file.getSize() > 2 * 1024 * 1024) {
                throw new BusinessException(ResponseCode.PARAM_ERROR.getCode(), "文件大小不能超过2MB");
            }

            // 上传头像
            String avatarUrl = userService.uploadAvatar(file);

            // 更新用户头像
            Long userId = LoginUserUtil.getLoginUserId();
            UpdateUserProfileRequest updateRequest = new UpdateUserProfileRequest();
            updateRequest.setAvatar(avatarUrl);
            userService.updateUserProfile(userId, updateRequest);

            log.info("用户头像上传成功 - userId: {}, url: {}", userId, avatarUrl);
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(avatarUrl)
                    .info("头像上传成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("上传头像失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("上传头像异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "上传头像失败");
        }
    }

    /**
     * 获取用户信息（公开接口）
     */
    @GetMapping("/info/{id}")
    @Operation(summary = "获取用户信息")
    @ApiOperationLog(description = "获取用户信息")
    public ResponseEntity<UserDetailVO> getUserInfo(@PathVariable Long id) {
        User user = userService.getUserInfo(id);
        return ResponseEntity.<UserDetailVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(convertToUserDetailVO(user))
                .build();
    }

    /**
     * 更新用户资料（仅能修改自己，ID从token获取防越权）
     */
    @PostMapping("/profile/update")
    @Operation(summary = "更新用户资料")
    @ApiOperationLog(description = "更新用户资料")
    @SaCheckLogin
    public ResponseEntity<UserDetailVO> updateUserProfile(@RequestBody @Valid UpdateUserProfileRequest request) {
        Long userId = LoginUserUtil.getLoginUserId();
        userService.updateUserProfile(userId, request);
        User updatedUser = userService.getUserInfo(userId);
        log.info("用户资料更新成功 - userId: {}", userId);
        return ResponseEntity.<UserDetailVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(convertToUserDetailVO(updatedUser))
                .info("用户资料更新成功")
                .build();
    }

    /**
     * 获取用户排行榜（支持fans/likes/posts/comprehensive排序）
     */
    @GetMapping("/ranking")
    @Operation(summary = "获取用户排行榜")
    @ApiOperationLog(description = "获取用户排行榜")
    public ResponseEntity<PageResponse<List<UserRankingVO>>> getUserRanking(
            @RequestParam(defaultValue = "week") String timeRange,
            @RequestParam(defaultValue = "fans") String sortType,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        page = Math.max(1, page);
        size = Math.min(100, Math.max(1, size));
        List<User> users = userService.findUserRanking(sortType, page, size);
        Long total = userService.countAllUsers();
        return ResponseEntity.<PageResponse<List<UserRankingVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(PageResponse.ofList(page, size, total, convertToUserRankingVOList(users)))
                .build();
    }

    /**
     * 获取个人主页（含基本信息、统计、关注关系）
     */
    @GetMapping("/profile/{userId}")
    @Operation(summary = "获取个人主页")
    @ApiOperationLog(description = "获取个人主页")
    public ResponseEntity<UserProfileVO> getUserProfile(@PathVariable Long userId) {
        Long currentUserId = LoginUserUtil.getLoginUserIdOptional().orElse(null);
        UserProfileService.UserProfileData profileData = userProfileService.getUserProfile(userId, currentUserId);
        return ResponseEntity.<UserProfileVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(convertToUserProfileVO(profileData))
                .build();
    }

    // ==================== 用户设置相关接口 ====================

    /**
     * 获取用户设置（隐私、通知、邮箱验证状态）
     */
    @GetMapping("/settings")
    @Operation(summary = "获取用户设置")
    @ApiOperationLog(description = "获取用户设置")
    @SaCheckLogin
    public ResponseEntity<UserSettingsVO> getUserSettings() {
        try {
            Long userId = LoginUserUtil.getLoginUserId();

            UserSettings settings = userSettingsService.getSettings(userId);

            UserSettingsVO vo = UserSettingsVO.builder()
                    .privacySettings(UserSettingsVO.PrivacySettingsVO.builder()
                            .profileVisibility(settings.getProfileVisibility())
                            .showOnlineStatus(settings.getShowOnlineStatusBool())
                            .build())
                    .notificationSettings(UserSettingsVO.NotificationSettingsVO.builder()
                            .emailNotification(settings.getEmailNotificationBool())
                            .browserNotification(settings.getBrowserNotificationBool())
                            .soundNotification(settings.getSoundNotificationBool())
                            .build())
                    .emailVerified(settings.getEmailVerifiedBool())
                    .build();

            return ResponseEntity.<UserSettingsVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(vo)
                    .info("获取设置成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("获取用户设置失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("获取用户设置异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "获取设置失败");
        }
    }

    /**
     * 更新隐私设置
     */
    @PutMapping("/settings/privacy")
    @Operation(summary = "更新隐私设置")
    @ApiOperationLog(description = "更新隐私设置")
    @SaCheckLogin
    public ResponseEntity<Void> updatePrivacySettings(@RequestBody @Valid UpdatePrivacySettingsRequest request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            log.info("更新隐私设置，用户ID：{}", userId);

            userSettingsService.updatePrivacySettings(
                    userId,
                    request.getProfileVisibility(),
                    request.getShowOnlineStatus());

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("隐私设置更新成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("更新隐私设置失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("更新隐私设置异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新隐私设置失败");
        }
    }

    /**
     * 更新通知设置
     */
    @PutMapping("/settings/notification")
    @Operation(summary = "更新通知设置")
    @ApiOperationLog(description = "更新通知设置")
    @SaCheckLogin
    public ResponseEntity<Void> updateNotificationSettings(@RequestBody @Valid UpdateNotificationSettingsRequest request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            log.info("更新通知设置，用户ID：{}", userId);

            userSettingsService.updateNotificationSettings(
                    userId,
                    request.getEmailNotification(),
                    request.getBrowserNotification(),
                    request.getSoundNotification());

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("通知设置更新成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("更新通知设置失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("更新通知设置异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新通知设置失败");
        }
    }

    /**
     * 发送邮箱验证邮件
     */
    @PostMapping("/settings/email/send-verify")
    @Operation(summary = "发送邮箱验证邮件")
    @ApiOperationLog(description = "发送邮箱验证邮件")
    @SaCheckLogin
    public ResponseEntity<Void> sendEmailVerify(@RequestBody @Valid SendEmailVerifyRequest request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            log.info("发送邮箱验证邮件，用户ID：{}，邮箱：{}", userId, request.getEmail());

            // 验证邮箱地址是否属于当前用户
            User user = userService.getUserInfo(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }
            if (!user.getEmail().equals(request.getEmail())) {
                throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱地址无效");
            }

            // 生成验证令牌，有效期24小时
            String token = java.util.UUID.randomUUID().toString().replace("-", "");
            java.time.LocalDateTime expiration = java.time.LocalDateTime.now().plusHours(24);

            // 发送验证邮件
            emailService.sendVerificationEmail(request.getEmail(), token, expiration);

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("邮箱验证邮件已发送，请查收邮件")
                    .build();
        } catch (BusinessException e) {
            log.warn("发送邮箱验证邮件失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("发送邮箱验证邮件异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "发送邮箱验证邮件失败");
        }
    }

    // ==================== 账户安全相关接口 ====================

    /**
     * 发送验证码
     */
    @PostMapping("/send-verify-code")
    @Operation(summary = "发送验证码")
    @ApiOperationLog(description = "发送验证码")
    public ResponseEntity<Void> sendVerifyCode(@RequestBody @Valid SendVerifyCodeRequest request) {
        try {
            // 解析场景
            VerificationCodeService.CodeScene scene = VerificationCodeService.CodeScene.GENERAL;
            if ("register".equalsIgnoreCase(request.getScene())) {
                scene = VerificationCodeService.CodeScene.REGISTER;
            } else if ("forgot".equalsIgnoreCase(request.getScene())) {
                scene = VerificationCodeService.CodeScene.FORGOT_PASSWORD;
            } else if ("bind".equalsIgnoreCase(request.getScene())) {
                scene = VerificationCodeService.CodeScene.BIND_EMAIL;
            }

            verificationCodeService.sendVerifyCode(request.getEmail(), scene);

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("验证码已发送，请查收邮件")
                    .build();
        } catch (BusinessException e) {
            log.warn("发送验证码失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("发送验证码异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "发送验证码失败");
        }
    }

    /**
     * 忘记密码 - 发送重置验证码
     *
     * <p>向注册邮箱发送密码重置验证码
     * <p>每天最多申请10次密码重置
     *
     * @param request 忘记密码请求，包含注册邮箱
     * @return 发送结果
     * @throws BusinessException 当邮箱不存在或发送失败时抛出
     */
    @PostMapping("/forgot-password")
    @Operation(summary = "忘记密码", description = "发送密码重置验证码到注册邮箱")
    @ApiOperationLog(description = "忘记密码")
    public ResponseEntity<Void> forgotPassword(@RequestBody @Valid ForgotPasswordRequest request) {
        try {
            passwordResetService.sendResetCode(request.getEmail());

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("密码重置验证码已发送，请查收邮件")
                    .build();
        } catch (BusinessException e) {
            log.warn("忘记密码失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("忘记密码异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "发送重置邮件失败");
        }
    }

    /**
     * 重置密码
     *
     * <p>通过邮箱验证码重置密码
     * <p>验证码有效期5分钟
     *
     * @param request 重置密码请求，包含邮箱、验证码、新密码
     * @return 重置结果
     * @throws BusinessException 当验证码错误或已过期时抛出
     */
    @PostMapping("/reset-password")
    @Operation(summary = "重置密码", description = "通过验证码重置密码")
    @ApiOperationLog(description = "重置密码")
    public ResponseEntity<Void> resetPassword(@RequestBody @Valid ResetPasswordRequest request) {
        try {
            // 验证两次密码是否一致
            if (!request.getPassword().equals(request.getConfirmPassword())) {
                throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "两次输入的密码不一致");
            }

            // 判断使用token还是验证码方式
            if (request.getToken() != null && !request.getToken().isEmpty()) {
                // 使用令牌方式重置
                passwordResetService.resetPasswordByToken(request.getToken(), request.getPassword());
            } else {
                // 需要传入邮箱和验证码
                throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "重置令牌不能为空");
            }

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("密码重置成功，请使用新密码登录")
                    .build();
        } catch (BusinessException e) {
            log.warn("重置密码失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("重置密码异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "密码重置失败");
        }
    }

    /**
     * 通过验证码重置密码
     *
     * <p>使用邮箱+验证码方式重置密码
     *
     * @param email 邮箱
     * @param code 验证码
     * @param newPassword 新密码
     * @param confirmPassword 确认密码
     * @return 重置结果
     */
    @PostMapping("/reset-password-by-code")
    @Operation(summary = "通过验证码重置密码", description = "使用邮箱验证码重置密码")
    @ApiOperationLog(description = "通过验证码重置密码")
    public ResponseEntity<Void> resetPasswordByCode(
            @Parameter(description = "邮箱") @RequestParam String email,
            @Parameter(description = "验证码") @RequestParam String code,
            @Parameter(description = "新密码") @RequestParam String newPassword,
            @Parameter(description = "确认密码") @RequestParam String confirmPassword) {
        try {
            // 验证两次密码是否一致
            if (!newPassword.equals(confirmPassword)) {
                throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "两次输入的密码不一致");
            }

            passwordResetService.resetPasswordByCode(email, code, newPassword);

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("密码重置成功，请使用新密码登录")
                    .build();
        } catch (BusinessException e) {
            log.warn("重置密码失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("重置密码异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "密码重置失败");
        }
    }

    // ==================== 用户搜索相关接口 ====================

    /**
     * 搜索用户
     *
     * <p>根据关键词搜索用户，用于@提及自动补全等场景
     * <p>搜索范围：用户名、昵称
     * <p>公开接口，无需登录
     *
     * @param keyword 搜索关键词
     * @param limit 返回数量限制，默认10，最大50
     * @return 匹配的用户列表
     */
    @GetMapping("/search")
    @Operation(summary = "搜索用户", description = "根据关键词搜索用户，用于@提及等场景")
    @ApiOperationLog(description = "搜索用户")
    public ResponseEntity<List<UserDetailVO>> searchUsers(
            @Parameter(description = "搜索关键词") @RequestParam String keyword,
            @Parameter(description = "返回数量限制") @RequestParam(defaultValue = "10") Integer limit) {
        try {
            if (keyword == null || keyword.trim().isEmpty()) {
                return ResponseEntity.<List<UserDetailVO>>builder()
                        .code(ResponseCode.SUCCESS.getCode())
                        .data(Collections.emptyList())
                        .build();
            }

            // 限制最大返回数量
            if (limit > 50) {
                limit = 50;
            }

            List<User> users = userService.searchUsers(keyword.trim(), limit);
            List<UserDetailVO> voList = users.stream()
                    .map(this::convertToUserDetailVO)
                    .collect(java.util.stream.Collectors.toList());

            return ResponseEntity.<List<UserDetailVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(voList)
                    .build();
        } catch (Exception e) {
            log.error("搜索用户异常", e);
            return ResponseEntity.<List<UserDetailVO>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(Collections.emptyList())
                    .build();
        }
    }

    /**
     * 验证邮箱
     *
     * <p>通过验证令牌验证邮箱
     * <p>需要登录后才能访问
     *
     * @param request 验证请求，包含验证令牌
     * @return 验证结果
     */
    @PostMapping("/settings/email/verify")
    @Operation(summary = "验证邮箱", description = "通过令牌验证邮箱")
    @ApiOperationLog(description = "验证邮箱")
    @SaCheckLogin
    public ResponseEntity<Void> verifyEmail(@RequestBody @Valid VerifyEmailRequest request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            log.info("验证邮箱，用户ID：{}，Token: {}", userId, request.getToken());

            // 验证token并更新用户邮箱验证状态
            userSettingsService.verifyEmail(request.getToken());

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("邮箱验证成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("验证邮箱失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("验证邮箱异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "邮箱验证失败");
        }
    }

    /**
     * 注销账户
     *
     * <p>永久删除用户账户，需要验证密码
     * <p>此操作不可逆，会删除用户所有数据
     * <p>需要登录后才能访问
     *
     * @param request 注销请求，包含用户密码
     * @return 注销结果
     * @throws BusinessException 当密码错误时抛出
     */
    @PostMapping("/delete-account")
    @Operation(summary = "注销账户", description = "永久删除用户账户")
    @ApiOperationLog(description = "注销账户")
    @SaCheckLogin
    public ResponseEntity<Void> deleteAccount(@RequestBody @Valid DeleteAccountRequest request) {
        try {
            Long userId = LoginUserUtil.getLoginUserId();
            log.warn("用户申请注销账户，用户ID：{}", userId);

            // 验证密码并删除用户账号
            userService.deleteAccount(userId, request.getPassword());

            // 退出登录
            StpUtil.logout();

            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("账户已注销")
                    .build();
        } catch (BusinessException e) {
            log.warn("注销账户失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("注销账户异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "账户注销失败");
        }
    }

    // ==================== 私有转换方法 ====================

    /**
     * User转换为UserDetailVO
     */
    private UserDetailVO convertToUserDetailVO(User user) {
        if (user == null) {
            return null;
        }
        return UserDetailVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .email(user.getEmail())
                .gender(user.getGender())
                .phone(user.getPhone())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                .description(user.getDescription())
                .status(user.getStatus())
                .followCount(user.getFollowCount())
                .fansCount(user.getFansCount())
                .likeCount(user.getLikeCount())
                .createTime(user.getCreateTime())
                .updateTime(user.getUpdateTime())
                .build();
    }

    /**
     * 批量转换User为UserRankingVO
     */
    private List<UserRankingVO> convertToUserRankingVOList(List<User> users) {
        if (users == null) {
            return null;
        }
        return users.stream()
                .map(user -> UserRankingVO.builder()
                        .id(user.getId())
                        .username(user.getUsername())
                        .nickname(user.getNickname())
                        .avatar(user.getAvatar())
                        .followCount(user.getFollowCount() != null ? user.getFollowCount() : 0L)
                        .fansCount(user.getFansCount() != null ? user.getFansCount() : 0L)
                        .likeCount(user.getLikeCount() != null ? user.getLikeCount() : 0L)
                        .postCount(user.getPostCount() != null ? user.getPostCount() : 0L)
                        .build())
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * UserProfileData转换为UserProfileVO
     */
    private UserProfileVO convertToUserProfileVO(UserProfileService.UserProfileData profileData) {
        if (profileData == null || profileData.getUser() == null) {
            return null;
        }
        User user = profileData.getUser();
        UserProfileService.UserProfileStats stats = profileData.getStats();

        UserProfileVO.UserBasicInfoVO basicInfo = UserProfileVO.UserBasicInfoVO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .nickname(user.getNickname())
                .avatar(user.getAvatar())
                .gender(user.getGender())
                .description(user.getDescription())
                .phone(user.getPhone())
                .email(user.getEmail())
                .region(user.getRegion())
                .birthday(user.getBirthday())
                .createTime(user.getCreateTime())
                .build();

        UserProfileVO.UserProfileStatsVO statsVO = UserProfileVO.UserProfileStatsVO.builder()
                .postCount(stats != null ? stats.getPostCount() : 0L)
                .followCount(stats != null ? stats.getFollowCount() : 0L)
                .fansCount(stats != null ? stats.getFansCount() : 0L)
                .likeCount(stats != null ? stats.getLikeCount() : 0L)
                .commentCount(stats != null ? stats.getCommentCount() : 0L)
                .collectionCount(stats != null ? stats.getCollectionCount() : 0L)
                .build();

        return UserProfileVO.builder()
                .basicInfo(basicInfo)
                .stats(statsVO)
                .isOwnProfile(profileData.getIsOwnProfile())
                .isFollowing(profileData.getIsFollowing())
                .isFollowedBy(profileData.getIsFollowedBy())
                .build();
    }
}