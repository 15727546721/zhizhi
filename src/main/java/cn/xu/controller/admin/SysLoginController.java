package cn.xu.controller.admin;

import cn.dev33.satoken.stp.StpUtil;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.user.LoginFormRequest;
import cn.xu.model.entity.User;
import cn.xu.model.enums.UserType;
import cn.xu.service.user.UserService;
import cn.xu.support.exception.BusinessException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 后台管理系统登录控制器
 *
 * <p>提供后台管理系统的登录、退出功能</p>
 * <p>仅允许管理员用户登录</p>
 
 */
@Slf4j
@RestController
@RequestMapping("/api/system")
@Tag(name = "后台登录", description = "后台管理系统登录接口")
public class SysLoginController {

    @Resource(name = "userService")
    private UserService userService;

    /**
     * 后台管理登录
     *
     * <p>验证用户名密码并检查管理员权限</p>
     * <p>公开接口，无需登录</p>
     *
     * @param loginRequest 登录请求，包含用户名和密码
     * @return 登录成功返回Token
     * @throws BusinessException 当用户名/密码错误或无管理权限时抛出
     */
    @PostMapping("/login")
    @Operation(summary = "后台管理登录")
    @ApiOperationLog(description = "后台管理登录")
    public ResponseEntity<String> login(@RequestBody LoginFormRequest loginRequest) {
        log.info("后台管理登录，用户名：{}", loginRequest.getUsername());

        // 参数校验
        if (StringUtils.isBlank(loginRequest.getUsername())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "用户名不能为空");
        }
        if (StringUtils.isBlank(loginRequest.getPassword())) {
            throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "密码不能为空");
        }

        try {
            // 调用用户服务进行登录验证
            User user = userService.loginByUsername(loginRequest.getUsername(), loginRequest.getPassword());

            if (user == null) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "用户名或密码错误");
            }

            // 检查用户是否有后台管理权限（官方账号及以上）
            if (!UserType.hasAdminAccess(user.getUserType())) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "您没有后台管理权限");
            }

            // 检查用户状态
            if (user.getStatus() != null && user.getStatus() == User.STATUS_DISABLED) {
                throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "账户已被禁用");
            }

            // Sa-Token登录
            StpUtil.login(user.getId());
            String token = StpUtil.getTokenValue();

            log.info("后台管理登录成功，用户ID：{}，用户名：{}", user.getId(), user.getUsername());

            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(token)
                    .info("登录成功")
                    .build();

        } catch (BusinessException e) {
            log.warn("后台管理登录失败：{}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("后台管理登录异常", e);
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "登录失败");
        }
    }

    /**
     * 后台管理退出
     *
     * <p>清除当前用户的登录状态</p>
     * <p>公开接口</p>
     *
     * @return 退出结果
     */
    @GetMapping("/logout")
    @Operation(summary = "后台管理退出")
    @ApiOperationLog(description = "后台管理退出")
    public ResponseEntity<Void> logout() {
        try {
            if (StpUtil.isLogin()) {
                long userId = StpUtil.getLoginIdAsLong();
                StpUtil.logout();
                log.info("后台管理退出成功，用户ID：{}", userId);
            }
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("退出成功")
                    .build();
        } catch (Exception e) {
            log.error("后台管理退出异常", e);
            return ResponseEntity.<Void>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .info("退出成功")
                    .build();
        }
    }

    /**
     * 初始化管理员账号
     *
     * <p>创建或重置默认管理员账号</p>
     * <p><strong>注意：生产环境应删除此接口或添加额外安全验证</strong></p>
     *
     * @return 初始化结果，包含默认用户名和密码
     */
    @PostMapping("/init-admin")
    @Operation(summary = "初始化管理员账号")
    public ResponseEntity<String> initAdmin() {
        try {
            User admin = userService.initAdminUser();
            log.info("管理员账号初始化成功，用户名：{}", admin.getUsername());
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data("admin / AdminPassword123!")
                    .info("管理员账号初始化成功")
                    .build();
        } catch (Exception e) {
            log.error("初始化管理员账号失败", e);
            return ResponseEntity.<String>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("初始化失败：" + e.getMessage())
                    .build();
        }
    }
}
