package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import cn.xu.service.security.IpBlockService;
import cn.xu.service.security.LoginSecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import java.util.Set;

/**
 * 后台安全管理控制器
 *
 * <p>提供IP封禁管理、账户解锁等安全管理功能</p>
 
 */
@Slf4j
@RestController
@RequestMapping("/api/system/security")
@Tag(name = "安全管理", description = "后台安全管理接口")
@RequiredArgsConstructor
public class SysSecurityController {

    private final IpBlockService ipBlockService;
    private final LoginSecurityService loginSecurityService;

    // ==================== IP封禁管理 ====================

    /**
     * 获取封禁IP列表
     *
     * <p>获取所有被永久封禁的IP列表
     * <p>需要system:security:ip权限
     *
     * @return 封禁的IP列表
     */
    @GetMapping("/ip/blocked")
    @Operation(summary = "获取封禁IP列表", description = "获取所有被封禁的IP")
    @SaCheckLogin
    @SaCheckPermission("system:security:ip")
    @ApiOperationLog(description = "获取封禁IP列表")
    public ResponseEntity<Set<String>> getBlockedIps() {
        Set<String> blockedIps = ipBlockService.getBlockedIps();
        return ResponseEntity.<Set<String>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(blockedIps)
                .info("获取成功")
                .build();
    }

    /**
     * 封禁IP
     *
     * <p>将指定IP加入黑名单
     * <p>需要system:security:ip权限
     *
     * @param ip IP地址
     * @param duration 封禁时长（秒），为空则永久封禁
     * @param reason 封禁原因
     * @return 操作结果
     */
    @PostMapping("/ip/block")
    @Operation(summary = "封禁IP", description = "将IP加入黑名单")
    @SaCheckLogin
    @SaCheckPermission("system:security:ip")
    @ApiOperationLog(description = "封禁IP")
    public ResponseEntity<Void> blockIp(
            @Parameter(description = "IP地址") @RequestParam String ip,
            @Parameter(description = "封禁时长（秒），为空则永久封禁") @RequestParam(required = false) Long duration,
            @Parameter(description = "封禁原因") @RequestParam(required = false, defaultValue = "管理员手动封禁") String reason) {

        if (duration != null && duration > 0) {
            ipBlockService.blockIp(ip, duration, reason);
            log.info("[安全管理] 管理员封禁IP - ip: {}, duration: {}s, reason: {}", ip, duration, reason);
        } else {
            ipBlockService.permanentBlockIp(ip, reason);
            log.info("[安全管理] 管理员永久封禁IP - ip: {}, reason: {}", ip, reason);
        }

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("IP已封禁")
                .build();
    }

    /**
     * 解封IP
     *
     * <p>将指定IP从黑名单移除
     * <p>需要system:security:ip权限
     *
     * @param ip IP地址
     * @return 操作结果
     */
    @PostMapping("/ip/unblock")
    @Operation(summary = "解封IP", description = "将IP从黑名单移除")
    @SaCheckLogin
    @SaCheckPermission("system:security:ip")
    @ApiOperationLog(description = "解封IP")
    public ResponseEntity<Void> unblockIp(@Parameter(description = "IP地址") @RequestParam String ip) {
        ipBlockService.unblockIp(ip);
        log.info("[安全管理] 管理员解封IP - ip: {}", ip);

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("IP已解封")
                .build();
    }

    /**
     * 检查IP状态
     *
     * <p>检查指定IP是否被封禁
     * <p>需要system:security:ip权限
     *
     * @param ip IP地址
     * @return IP封禁状态信息
     */
    @GetMapping("/ip/status")
    @Operation(summary = "检查IP状态", description = "检查IP是否被封禁")
    @SaCheckLogin
    @SaCheckPermission("system:security:ip")
    @ApiOperationLog(description = "检查IP状态")
    public ResponseEntity<IpStatusVO> checkIpStatus(@Parameter(description = "IP地址") @RequestParam String ip) {
        boolean isBlocked = ipBlockService.isBlocked(ip);
        boolean isWhitelisted = ipBlockService.isInWhitelist(ip);
        String blockInfo = ipBlockService.getBlockInfo(ip);

        IpStatusVO vo = new IpStatusVO();
        vo.setIp(ip);
        vo.setBlocked(isBlocked);
        vo.setWhitelisted(isWhitelisted);
        vo.setBlockInfo(blockInfo);

        return ResponseEntity.<IpStatusVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(vo)
                .build();
    }

    /**
     * 添加IP到白名单
     *
     * <p>将IP添加到白名单，白名单IP不会被封禁
     * <p>需要system:security:ip权限
     *
     * @param ip IP地址
     * @return 操作结果
     */
    @PostMapping("/ip/whitelist/add")
    @Operation(summary = "添加白名单", description = "将IP添加到白名单")
    @SaCheckLogin
    @SaCheckPermission("system:security:ip")
    @ApiOperationLog(description = "添加IP到白名单")
    public ResponseEntity<Void> addToWhitelist(@Parameter(description = "IP地址") @RequestParam String ip) {
        ipBlockService.addToWhitelist(ip);
        log.info("[安全管理] 管理员添加IP到白名单 - ip: {}", ip);

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("已添加到白名单")
                .build();
    }

    /**
     * 从白名单移除IP
     *
     * <p>将IP从白名单移除
     * <p>需要system:security:ip权限
     *
     * @param ip IP地址
     * @return 操作结果
     */
    @PostMapping("/ip/whitelist/remove")
    @Operation(summary = "移除白名单", description = "将IP从白名单移除")
    @SaCheckLogin
    @SaCheckPermission("system:security:ip")
    @ApiOperationLog(description = "从白名单移除IP")
    public ResponseEntity<Void> removeFromWhitelist(@Parameter(description = "IP地址") @RequestParam String ip) {
        ipBlockService.removeFromWhitelist(ip);
        log.info("[安全管理] 管理员从白名单移除IP - ip: {}", ip);

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("已从白名单移除")
                .build();
    }

    // ==================== 账户安全管理 ====================

    /**
     * 解锁账户
     *
     * <p>解锁被登录失败次数过多锁定的账户
     * <p>需要system:security:account权限
     *
     * @param identifier 账户标识（邮箱或用户名）
     * @return 操作结果
     */
    @PostMapping("/account/unlock")
    @Operation(summary = "解锁账户", description = "解锁被锁定的账户")
    @SaCheckLogin
    @SaCheckPermission("system:security:account")
    @ApiOperationLog(description = "解锁账户")
    public ResponseEntity<Void> unlockAccount(@Parameter(description = "账户标识（邮箱或用户名）") @RequestParam String identifier) {
        loginSecurityService.unlockAccount(identifier);
        log.info("[安全管理] 管理解锁账户 - identifier: {}", identifier);

        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("账户已解锁")
                .build();
    }

    /**
     * 检查账户锁定状态
     *
     * <p>检查指定账户是否被锁定
     * <p>需要system:security:account权限
     *
     * @param identifier 账户标识（邮箱或用户名）
     * @return 账户锁定状态
     */
    @GetMapping("/account/status")
    @Operation(summary = "检查账户状态", description = "检查账户是否被锁定")
    @SaCheckLogin
    @SaCheckPermission("system:security:account")
    @ApiOperationLog(description = "检查账户状态")
    public ResponseEntity<AccountStatusVO> checkAccountStatus(
            @Parameter(description = "账户标识（邮箱或用户名）") @RequestParam String identifier) {

        boolean isLocked = loginSecurityService.isAccountLocked(identifier);
        int remainingAttempts = loginSecurityService.getRemainingAttempts(identifier);

        AccountStatusVO vo = new AccountStatusVO();
        vo.setIdentifier(identifier);
        vo.setLocked(isLocked);
        vo.setRemainingAttempts(remainingAttempts);

        return ResponseEntity.<AccountStatusVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(vo)
                .build();
    }

    // ==================== 内部VO ====================

    /**
     * IP状态VO
     */
    @lombok.Data
    public static class IpStatusVO {
        private String ip;
        private boolean blocked;
        private boolean whitelisted;
        private String blockInfo;
    }

    /**
     * 账户状态VO
     */
    @lombok.Data
    public static class AccountStatusVO {
        private String identifier;
        private boolean locked;
        private int remainingAttempts;
    }
}