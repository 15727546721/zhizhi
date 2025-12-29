package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

/**
 * 系统配置管理控制器
 *
 * <p>提供系统配置、网站配置、反馈管理等功能</p>
 * <p>需要登录并拥有相应权限</p>

 */
@Slf4j
@RestController
@Tag(name = "系统配置管理", description = "系统配置管理相关接口")
public class SysConfigController {

    // 模拟系统配置数据（后续可接入数据库）
    private static Map<String, Object> systemConfig = new HashMap<>();
    private static Map<String, Object> webConfig = new HashMap<>();

    static {
        // 初始化系统配置
        systemConfig.put("siteName", "知识社区");
        systemConfig.put("siteDescription", "一个技术交流社区");
        systemConfig.put("siteKeywords", "技术编程,社区");
        systemConfig.put("icp", "");
        systemConfig.put("copyright", "© 2025 知识社区");
        systemConfig.put("uploadMaxSize", 10); // MB
        systemConfig.put("allowedFileTypes", "jpg,png,gif,pdf,doc,docx");

        // 初始化网站配置
        webConfig.put("logo", "");
        webConfig.put("favicon", "");
        webConfig.put("footer", "知识社区 - 技术交流平台");
        webConfig.put("enableComment", true);
        webConfig.put("enableRegister", true);
    }

    // ==================== 系统配置 ====================

    /**
     * 获取系统配置
     *
     * <p>返回系统基础配置，包括站点名称、描述、ICP等
     * <p>需要登录后才能访问
     *
     * @return 系统配置集合
     */
    @GetMapping("/api/system/config/getConfig")
    @Operation(summary = "获取系统配置")
    @SaCheckLogin
    @ApiOperationLog(description = "获取系统配置")
    public ResponseEntity<Map<String, Object>> getSystemConfig() {
        log.info("获取系统配置");
        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(systemConfig)
                .build();
    }

    /**
     * 更新系统配置
     *
     * <p>更新系统基础配置
     * <p>需要system:config:update权限
     *
     * @param config 配置集合
     * @return 更新结果
     */
    @PostMapping("/api/system/config/update")
    @Operation(summary = "更新系统配置")
    @SaCheckLogin
    @SaCheckPermission("system:config:update")
    @ApiOperationLog(description = "更新系统配置")
    public ResponseEntity<Void> updateSystemConfig(@RequestBody Map<String, Object> config) {
        log.info("更新系统配置: {}", config);
        systemConfig.putAll(config);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新成功")
                .build();
    }

    // ==================== 网站配置 ====================

    /**
     * 获取网站配置
     *
     * <p>返回网站前台配置，包括logo、页脚、功能开关等
     * <p>需要登录后才能访问
     *
     * @return 网站配置集合
     */
    @GetMapping("/api/system/webConfig/")
    @Operation(summary = "获取网站配置")
    @SaCheckLogin
    @ApiOperationLog(description = "获取网站配置")
    public ResponseEntity<Map<String, Object>> getWebConfig() {
        log.info("获取网站配置");
        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(webConfig)
                .build();
    }

    /**
     * 更新网站配置
     *
     * <p>更新网站前台配置
     * <p>需要system:config:update权限
     *
     * @param config 配置集合
     * @return 更新结果
     */
    @PostMapping("/api/system/webConfig/update")
    @Operation(summary = "更新网站配置")
    @SaCheckLogin
    @SaCheckPermission("system:config:update")
    @ApiOperationLog(description = "更新网站配置")
    public ResponseEntity<Void> updateWebConfig(@RequestBody Map<String, Object> config) {
        log.info("更新网站配置: {}", config);
        webConfig.putAll(config);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新成功")
                .build();
    }
}