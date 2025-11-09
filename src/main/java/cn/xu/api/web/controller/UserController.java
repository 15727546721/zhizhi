package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.converter.UserProfileVOConverter;
import cn.xu.api.web.model.converter.UserVOConverter;
import cn.xu.api.web.model.dto.user.UpdateUserRequest;
import cn.xu.api.web.model.dto.user.UpdateUserProfileRequest;
import cn.xu.api.web.model.dto.user.UserLoginRequest;
import cn.xu.api.web.model.dto.user.UserRegisterRequest;
import cn.xu.api.web.model.vo.user.UserLoginResponse;
import cn.xu.api.web.model.vo.user.UserProfileVO;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.api.web.model.vo.user.UserRankingResponse;
import cn.xu.api.web.model.vo.user.UserResponse;
import cn.xu.domain.user.model.entity.UserEntity;
import cn.xu.domain.user.service.IUserService;
import cn.xu.domain.user.service.UserProfileApplicationService;
import cn.xu.domain.user.service.UserValidationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.List;

@Slf4j
@RequestMapping("api/user")
@RestController
@Tag(name = "用户接口", description = "用户相关接口")
public class UserController {

    @Resource
    private IUserService userService;
    
    @Resource
    private UserValidationService userValidationService;
    
    @Resource
    private UserVOConverter userVOConverter;
    
    @Resource
    private UserProfileApplicationService userProfileApplicationService;
    
    @Resource
    private UserProfileVOConverter userProfileVOConverter;

    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册接口")
    @ApiOperationLog(description = "用户注册")
    public ResponseEntity<Void> register(@RequestBody UserRegisterRequest registerRequest) {
        // 参数校验
        userValidationService.validateRegisterParams(
            registerRequest.getUsername(),
            registerRequest.getPassword(),
            registerRequest.getConfirmPassword(),
            registerRequest.getEmail()
        );

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
    public ResponseEntity<UserLoginResponse> login(@RequestBody UserLoginRequest loginRequest) {
        // 参数校验
        userValidationService.validateLoginParams(
            loginRequest.getEmail(),
            loginRequest.getPassword()
        );
        
        UserEntity user = userService.login(loginRequest);
        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }
        StpUtil.login(user.getId());
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

    @GetMapping("/info/{id}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户信息")
    @ApiOperationLog(description = "获取用户信息")
    public ResponseEntity<UserResponse> getUserInfo(@Parameter(description = "用户ID") @PathVariable Long id) {
        log.info("获取用户信息，用户ID：{}", id);
        UserEntity user = userService.getUserInfo(id);
        UserResponse userResponse = userVOConverter.convertToUserResponse(user);
        return ResponseEntity.<UserResponse>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(userResponse)
                .build();
    }

    @PostMapping("/update")
    @ApiOperationLog(description = "更新用户信息")
    @SaCheckLogin
    @Operation(summary = "更新用户信息", description = "更新当前登录用户的信息")
    @Deprecated
    public ResponseEntity<Void> updateUser(@RequestBody UpdateUserRequest user) {
        long userId = StpUtil.getLoginIdAsLong();
        if (userId != user.getId()) {
            throw new BusinessException("只能更新自己的信息");
        }
        userService.update(user);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("用户信息更新成功")
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
            UserEntity updatedUser = userService.getUserInfo(userId);
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
        List<UserEntity> users = userService.findUserRanking(sortType, page, size);
        
        // 转换为VO（符合DDD原则，领域实体不直接暴露给前端）
        List<UserRankingResponse> userRankingResponses = userVOConverter.convertToUserRankingResponseList(users);
        
        // 统计总数
        Long total = userService.countAllUsers();
        
        // 构建分页响应
        PageResponse<List<UserRankingResponse>> pageResponse = PageResponse.ofList(page, size, total, userRankingResponses);
        
        return ResponseEntity.<PageResponse<List<UserRankingResponse>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(pageResponse)
                .build();
    }
    
    @GetMapping("/profile/{userId}")
    @Operation(summary = "获取个人主页", description = "获取指定用户的个人主页数据，包含基本信息、统计数据等")
    @ApiOperationLog(description = "获取个人主页")
    public ResponseEntity<UserProfileVO> getUserProfile(
            @Parameter(description = "用户ID") @PathVariable Long userId) {
        log.info("获取个人主页，用户ID：{}", userId);
        
        try {
            // 获取当前登录用户ID（可能为null，表示未登录）
            Long currentUserId = null;
            try {
                if (StpUtil.isLogin()) {
                    currentUserId = StpUtil.getLoginIdAsLong();
                }
            } catch (Exception e) {
                // 未登录，忽略
                log.debug("用户未登录，currentUserId为null");
            }
            
            // 调用应用服务获取个人主页数据
            UserProfileApplicationService.UserProfileData profileData = 
                    userProfileApplicationService.getUserProfile(userId, currentUserId);
            
            // 转换为VO
            UserProfileVO profileVO = userProfileVOConverter.convertToUserProfileVO(profileData);
            
            return ResponseEntity.<UserProfileVO>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(profileVO)
                    .info("获取个人主页成功")
                    .build();
                    
        } catch (BusinessException e) {
            log.warn("获取个人主页失败，用户ID：{}，错误：{}", userId, e.getMessage());
            return ResponseEntity.<UserProfileVO>builder()
                    .code(e.getCode() != null ? e.getCode() : ResponseCode.UN_ERROR.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("获取个人主页失败，用户ID：{}", userId, e);
            return ResponseEntity.<UserProfileVO>builder()
                    .code(ResponseCode.SYSTEM_ERROR.getCode())
                    .info("获取个人主页失败：" + e.getMessage())
                    .build();
        }
    }
}