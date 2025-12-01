package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.integration.mail.EmailService;
import cn.xu.model.converter.UserProfileVOConverter;
import cn.xu.model.converter.UserVOConverter;
import cn.xu.model.dto.user.*;
import cn.xu.model.entity.User;
import cn.xu.model.entity.UserSettings;
import cn.xu.model.vo.user.*;
import cn.xu.service.user.UserProfileService;
import cn.xu.service.user.UserService;
import cn.xu.service.user.UserSettingsService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

/**
 * 用户相关接口
 * <p>提供用户注册、登录、退出、修改密码等功能。</p>
 * @author xu
 * @since 2025-11-25
 */
@Slf4j
@RequestMapping("api/user")
@RestController
@Tag(name = "用户接口", description = "用户操作相关接口")
public class UserController {

    @Resource(name = "userService")
    private UserService userService;
    
    @Resource
    private UserSettingsService userSettingsService;
    
    @Resource
    private UserProfileService userProfileService;
    
    @Resource
    private UserProfileVOConverter userProfileVOConverter;
    
    @Resource
    private UserVOConverter userVOConverter;
    
    @Resource
    private EmailService emailService;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    @ApiOperationLog(description = "用户注册")
    public ResponseEntity<Void> register(@RequestBody @Valid UserRegisterRequest registerRequest) {
        // 参数校验（基本校验通过@Valid完成）
        if (StringUtils.isBlank(registerRequest.getEmail())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱不能为空");
        }
        if (StringUtils.isBlank(registerRequest.getPassword())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "密码不能为空");
        }

        // 注册用户
        userService.register(registerRequest);

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("用户注册成功")
                .build();
    }

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录接口")
    @ApiOperationLog(description = "用户登录")
    public ResponseEntity<UserLoginResponse> login(@RequestBody @Valid UserLoginRequest loginRequest) {
        // 参数校验（基本校验通过@Valid完成）
        if (StringUtils.isBlank(loginRequest.getEmail())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱不能为空");
        }
        if (StringUtils.isBlank(loginRequest.getPassword())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "密码不能为空");
        }
        
        User user = userService.login(loginRequest);
        if (user == null) {
            throw new BusinessException("登录失败");
        }
        // login方法内部已经调用了StpUtil.login，不需要重复调用
        log.info("用户登录成功，用户ID：{}", user.getId());
        return ResponseEntity.<UserLoginResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(UserLoginResponse.builder()
                        .userInfo(userVOConverter.convertToUserResponse(user))
                        .token(StpUtil.getTokenValue())
                        .build())
                .info("用户登录成功")
                .build();
    }

    @PostMapping("/logout")
    @Operation(summary = "用户退出", description = "用户退出接口")
    @ApiOperationLog(description = "用户退出")
    public ResponseEntity<Void> logout() {
        long useId = StpUtil.getLoginIdAsLong();
        StpUtil.logout();
        log.info("用户退出成功: {}", useId);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("退出成功")
                .build();
    }

    @PostMapping("/change-password")
    @Operation(summary = "修改密码", description = "用户修改自己的密码")
    @ApiOperationLog(description = "修改密码")
    @SaCheckLogin
    public ResponseEntity<Void> changePassword(@RequestBody @Valid ChangePasswordRequest request) {
        // 验证新密码和确认密码是否一致
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "两次输入的密码不一致");
        }
        
        // 从token中获取当前登录用户ID
        Long userId = StpUtil.getLoginIdAsLong();
        log.info("用户修改密码，用户ID：{}", userId);
        
        // 调用服务层修改密码
        userService.changePassword(userId, request.getOldPassword(), request.getNewPassword());
        
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("密码修改成功")
                .build();
    }

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
            String url = userService.uploadAvatar(file);
            
            // 更新用户头像
            Long userId = StpUtil.getLoginIdAsLong();
            userService.updateUserAvatar(userId, url);
            
            log.info("用户头像上传成功 - userId: {}, url: {}", userId, url);
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(url)
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

    @GetMapping("/info/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    @ApiOperationLog(description = "获取用户信息")
    public ResponseEntity<UserResponse> getUserInfo(@Parameter(description = "用户ID") @PathVariable Long id) {
        log.info("获取用户信息，用户ID：{}", id);
        User user = userService.getUserInfo(id);
        UserResponse userResponse = userVOConverter.convertToUserResponse(user);
        return ResponseEntity.<UserResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(userResponse)
                .build();
    }
    
    /**
     * 更新用户资料
     * 只允许用户修改自己的资料，用户ID从token中获取，防止越权
     * 
     * @param request 更新用户资料请求
     * @return 响应结果
     */
    @PostMapping("/profile/update")
    @ApiOperationLog(description = "更新用户资料")
    @SaCheckLogin
    @Operation(summary = "更新用户资料", description = "更新当前登录用户的资料信息，用户ID从token中获取，防止越权")
    public ResponseEntity<UserResponse> updateUserProfile(@RequestBody @Valid UpdateUserProfileRequest request) {
        try {
            // 从token中获取当前登录用户ID，确保安全性
            Long userId = StpUtil.getLoginIdAsLong();
            log.info("更新用户资料，用户ID：{}", userId);
            
            // 调用服务层更新用户资料
            userService.updateUserProfile(userId, request);
            
            // 获取更新后的用户信息并返回
            User updatedUser = userService.getUserInfo(userId);
            UserResponse userResponse = userVOConverter.convertToUserResponse(updatedUser);
            
            return ResponseEntity.<UserResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(userResponse)
                    .info("用户资料更新成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("更新用户资料失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("更新用户资料异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "更新用户资料失败");
        }
    }
    
    @GetMapping("/ranking")
    @Operation(summary = "获取用户排行榜", description = "根据排序类型获取用户排行榜")
    @ApiOperationLog(description = "获取用户排行榜")
    public ResponseEntity<PageResponse<List<UserRankingResponse>>> getUserRanking(
            @Parameter(description = "时间范围：week(7天)、month(30天)") @RequestParam(required = false, defaultValue = "week") String timeRange,
            @Parameter(description = "排序类型：fans(粉丝数)、likes(获赞数)、posts(帖子数)、comprehensive(综合)") @RequestParam(required = false, defaultValue = "fans") String sortType,
            @Parameter(description = "页码") @RequestParam(required = false, defaultValue = "1") Integer page,
            @Parameter(description = "每页数量") @RequestParam(required = false, defaultValue = "10") Integer size) {
        log.info("获取用户排行榜 - timeRange: {}, sortType: {}, page: {}, size: {}", timeRange, sortType, page, size);
        
        // 参数校验
        if (page < 1) {
            page = 1;
        }
        if (size < 1) {
            size = 10;
        }
        if (size > 100) {
            size = 100;
        }
        
        // 查询用户排行榜（领域实体）
        List<User> users = userService.findUserRanking(sortType, page, size);
        
        // 转换为VO（符合DDD原则，领域实体不直接暴露给前端）
        List<UserRankingResponse> userRankingResponses = userVOConverter.convertToUserRankingResponseList(users);
        
        // 统计总数
        Long total = userService.countAllUsers();
        
        // 构造分页信息
        PageResponse<List<UserRankingResponse>> pageResponse = PageResponse.ofList(page, size, total, userRankingResponses);
        
        return ResponseEntity.<PageResponse<List<UserRankingResponse>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(pageResponse)
                .build();
    }
    
    @GetMapping("/profile/{userId}")
    @Operation(summary = "获取个人主页", description = "获取指定用户的个人主页数据，包括个人资料和互动信息")
    @ApiOperationLog(description = "获取个人主页")
    public ResponseEntity<UserProfileVO> getUserProfile(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("获取个人主页，用户ID：{}", userId);
        
        try {
            // 获取当前登录用户ID（如果有登录）
            Long currentUserId = null;
            try {
                if (StpUtil.isLogin()) {
                    currentUserId = StpUtil.getLoginIdAsLong();
                }
            } catch (Exception e) {
                log.debug("用户未登录，currentUserId = null");
            }
            
            // 调用服务层获取用户个人主页数据
            UserProfileService.UserProfileData profileData = 
                    userProfileService.getUserProfile(userId, currentUserId);
            
            // 转换为VO
            UserProfileVO profileVO = userProfileVOConverter.convertToUserProfileVO(profileData);
            
            return ResponseEntity.<UserProfileVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(profileVO)
                    .info("获取个人主页成功")
                    .build();
                    
        } catch (BusinessException e) {
            log.warn("获取个人主页失败，用户ID：{}，失败原因：{}", userId, e.getMessage());
            return ResponseEntity.<UserProfileVO>builder()
                    .code(e.getCode() != null ? e.getCode() : ResponseCode.UN_ERROR.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("获取个人主页异常，用户ID：{}", userId, e);
            return ResponseEntity.<UserProfileVO>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取个人主页失败: " + e.getMessage())
                    .build();
        }
    }
    
    // ==================== 用户设置相关接口 ====================
    
    @GetMapping("/settings")
    @Operation(summary = "获取用户设置", description = "获取当前登录用户的设置信息")
    @ApiOperationLog(description = "获取用户设置")
    @SaCheckLogin
    public ResponseEntity<UserSettingsVO> getUserSettings() {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            log.info("获取用户设置，用户ID：{}", userId);
            
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
    
    @PutMapping("/settings/privacy")
    @Operation(summary = "更新隐私设置", description = "更新当前登录用户的隐私设置")
    @ApiOperationLog(description = "更新隐私设置")
    @SaCheckLogin
    public ResponseEntity<Void> updatePrivacySettings(@RequestBody @Valid UpdatePrivacySettingsRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
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
    
    @PutMapping("/settings/notification")
    @Operation(summary = "更新通知设置", description = "更新当前登录用户的通知设置")
    @ApiOperationLog(description = "更新通知设置")
    @SaCheckLogin
    public ResponseEntity<Void> updateNotificationSettings(@RequestBody @Valid UpdateNotificationSettingsRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
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
    
    @PostMapping("/settings/email/send-verify")
    @Operation(summary = "发送邮箱验证邮件", description = "向指定邮箱发送验证邮件")
    @ApiOperationLog(description = "发送邮箱验证邮件")
    @SaCheckLogin
    public ResponseEntity<Void> sendEmailVerify(@RequestBody @Valid SendEmailVerifyRequest request) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            log.info("发送邮箱验证邮件，用户ID：{}，邮箱：{}", userId, request.getEmail());
            
            // 验证邮箱是否属于当前用户
            User user = userService.getUserInfo(userId);
            if (user == null) {
                throw new BusinessException(ResponseCode.USER_NOT_FOUND.getCode(), "用户不存在");
            }
            if (!user.getEmail().equals(request.getEmail())) {
                throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "邮箱不属于当前用户");
            }
            
            // 生成验证令牌（24小时有效）
            String token = java.util.UUID.randomUUID().toString().replace("-", "");
            java.time.LocalDateTime expiration = java.time.LocalDateTime.now().plusHours(24);
            
            // 发送邮箱验证邮件
            emailService.sendVerificationEmail(request.getEmail(), token, expiration);
            
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("邮箱验证邮件已发送")
                    .build();
        } catch (BusinessException e) {
            log.warn("发送邮箱验证邮件失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("发送邮箱验证邮件异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "发送邮箱验证邮件失败");
        }
    }
}
