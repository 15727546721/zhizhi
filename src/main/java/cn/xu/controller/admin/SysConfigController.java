package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 系统配置管理控制器
 * 
 * @author xu
 * @since 2025-12-01
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
        systemConfig.put("siteName", "知知社区");
        systemConfig.put("siteDescription", "一个技术交流社区");
        systemConfig.put("siteKeywords", "技术,编程,社区");
        systemConfig.put("icp", "");
        systemConfig.put("copyright", "© 2025 知知社区");
        systemConfig.put("uploadMaxSize", 10); // MB
        systemConfig.put("allowedFileTypes", "jpg,png,gif,pdf,doc,docx");
        
        // 初始化网站配置
        webConfig.put("logo", "");
        webConfig.put("favicon", "");
        webConfig.put("footer", "知知社区 - 技术交流平台");
        webConfig.put("enableComment", true);
        webConfig.put("enableRegister", true);
    }

    // ==================== 系统配置 ====================

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

    @PutMapping("/api/system/config/update")
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

    @PutMapping("/api/system/webConfig/update")
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

    // ==================== 反馈管理 ====================

    @GetMapping("/api/system/feedback/list")
    @Operation(summary = "获取反馈列表")
    @SaCheckLogin
    @ApiOperationLog(description = "获取反馈列表")
    public ResponseEntity<PageResponse<List<FeedbackVO>>> getFeedbackList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取反馈列表");
        
        List<FeedbackVO> feedbacks = new ArrayList<>();
        PageResponse<List<FeedbackVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, 0L, feedbacks);
        
        return ResponseEntity.<PageResponse<List<FeedbackVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(pageResponse)
                .build();
    }

    @PutMapping("/api/system/feedback/update")
    @Operation(summary = "更新反馈状态")
    @SaCheckLogin
    @SaCheckPermission("system:feedback:update")
    @ApiOperationLog(description = "更新反馈状态")
    public ResponseEntity<Void> updateFeedback(@RequestBody FeedbackVO feedback) {
        log.info("更新反馈: {}", feedback);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("更新成功")
                .build();
    }

    @DeleteMapping("/api/system/feedback/delete")
    @Operation(summary = "删除反馈")
    @SaCheckLogin
    @SaCheckPermission("system:feedback:delete")
    @ApiOperationLog(description = "删除反馈")
    public ResponseEntity<Void> deleteFeedback(@RequestBody List<Long> ids) {
        log.info("删除反馈: {}", ids);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    // ==================== VO类定义 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FeedbackVO {
        private Long id;
        private String content;
        private String contact;
        private Integer status;
        private String reply;
        private String createTime;
    }
}
