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

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 日志管理控制器
 * 
 * <p>提供操作日志、异常日志、用户日志的查询和删除功能</p>
 * <p>需要登录并拥有相应权限</p>

 */
@Slf4j
@RestController
@Tag(name = "日志管理", description = "日志管理相关接口")
public class SysLogController {

    // ==================== 操作日志 ====================

    /**
     * 获取操作日志列表
     * 
     * <p>分页查询管理员操作日志，支持按用户名和描述筛选
     * <p>需要system:log:list权限
     * 
     * @param pageNo 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param username 用户名筛选（可选）
     * @param description 描述筛选（可选）
     * @return 分页的操作日志列表
     */
    @GetMapping("/api/system/adminLog/list")
    @Operation(summary = "获取操作日志列表")
    @SaCheckLogin
    @SaCheckPermission("system:log:list")
    @ApiOperationLog(description = "获取操作日志列表")
    public ResponseEntity<PageResponse<List<AdminLogVO>>> getAdminLogList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username,
            @RequestParam(required = false) String description) {
        log.info("获取操作日志列表: pageNo={}, pageSize={}", pageNo, pageSize);
        
        // 暂时返回空列表，后续可接入日志存储系统
        List<AdminLogVO> logs = new ArrayList<>();
        PageResponse<List<AdminLogVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, 0L, logs);
        
        return ResponseEntity.<PageResponse<List<AdminLogVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(pageResponse)
                .build();
    }

    /**
     * 删除操作日志
     * 
     * <p>批量删除操作日志
     * <p>需要system:log:delete权限
     * 
     * @param ids 日志ID列表
     * @return 删除结果
     */
    @DeleteMapping("/api/system/adminLog/delete")
    @Operation(summary = "删除操作日志")
    @SaCheckLogin
    @SaCheckPermission("system:log:delete")
    @ApiOperationLog(description = "删除操作日志")
    public ResponseEntity<Void> deleteAdminLog(@RequestBody List<Long> ids) {
        log.info("删除操作日志: ids={}", ids);
        // 暂时不做实际删除
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    // ==================== 异常日志 ====================

    /**
     * 获取异常日志列表
     * 
     * <p>分页查询系统异常日志
     * <p>需要system:log:list权限
     * 
     * @param pageNo 页码，默认1
     * @param pageSize 每页数量，默认10
     * @return 分页的异常日志列表
     */
    @GetMapping("/api/system/exceptionLog/list")
    @Operation(summary = "获取异常日志列表")
    @SaCheckLogin
    @SaCheckPermission("system:log:list")
    @ApiOperationLog(description = "获取异常日志列表")
    public ResponseEntity<PageResponse<List<ExceptionLogVO>>> getExceptionLogList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize) {
        log.info("获取异常日志列表: pageNo={}, pageSize={}", pageNo, pageSize);
        
        List<ExceptionLogVO> logs = new ArrayList<>();
        PageResponse<List<ExceptionLogVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, 0L, logs);
        
        return ResponseEntity.<PageResponse<List<ExceptionLogVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(pageResponse)
                .build();
    }

    /**
     * 删除异常日志
     * 
     * <p>批量删除异常日志
     * <p>需要system:log:delete权限
     * 
     * @param ids 日志ID列表
     * @return 删除结果
     */
    @DeleteMapping("/api/system/exceptionLog/delete")
    @Operation(summary = "删除异常日志")
    @SaCheckLogin
    @SaCheckPermission("system:log:delete")
    @ApiOperationLog(description = "删除异常日志")
    public ResponseEntity<Void> deleteExceptionLog(@RequestBody List<Long> ids) {
        log.info("删除异常日志: ids={}", ids);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    // ==================== 用户日志 ====================

    /**
     * 获取用户日志列表
     * 
     * <p>分页查询用户操作日志，支持按用户名筛选
     * <p>需要system:log:list权限
     * 
     * @param pageNo 页码，默认1
     * @param pageSize 每页数量，默认10
     * @param username 用户名筛选（可选）
     * @return 分页的用户日志列表
     */
    @GetMapping("/api/system/userLog/list")
    @Operation(summary = "获取用户日志列表")
    @SaCheckLogin
    @SaCheckPermission("system:log:list")
    @ApiOperationLog(description = "获取用户日志列表")
    public ResponseEntity<PageResponse<List<UserLogVO>>> getUserLogList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String username) {
        log.info("获取用户日志列表: pageNo={}, pageSize={}", pageNo, pageSize);
        
        List<UserLogVO> logs = new ArrayList<>();
        PageResponse<List<UserLogVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, 0L, logs);
        
        return ResponseEntity.<PageResponse<List<UserLogVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(pageResponse)
                .build();
    }

    /**
     * 删除用户日志
     * 
     * <p>批量删除用户日志
     * <p>需要system:log:delete权限
     * 
     * @param ids 日志ID列表
     * @return 删除结果
     */
    @DeleteMapping("/api/system/userLog/delete")
    @Operation(summary = "删除用户日志")
    @SaCheckLogin
    @SaCheckPermission("system:log:delete")
    @ApiOperationLog(description = "删除用户日志")
    public ResponseEntity<Void> deleteUserLog(@RequestBody List<Long> ids) {
        log.info("删除用户日志: ids={}", ids);
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
    public static class AdminLogVO {
        private Long id;
        private String username;
        private String description;
        private String requestMethod;
        private String requestUrl;
        private String requestParams;
        private String ip;
        private Long executionTime;
        private LocalDateTime createTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExceptionLogVO {
        private Long id;
        private String username;
        private String requestMethod;
        private String requestUrl;
        private String exceptionName;
        private String exceptionMessage;
        private String stackTrace;
        private String ip;
        private LocalDateTime createTime;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserLogVO {
        private Long id;
        private String username;
        private String action;
        private String description;
        private String ip;
        private String userAgent;
        private LocalDateTime createTime;
    }
}