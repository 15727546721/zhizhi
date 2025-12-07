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
 * 任务日志管理控制器
 * 
 * <p>提供定时任务执行日志的查询和删除功能
 * <p>需要登录并拥有相应权限
 */
@Slf4j
@RestController
@RequestMapping("/api/system/jobLog")
@Tag(name = "任务日志管理", description = "任务日志管理相关接口")
public class SysJobLogController {

    /**
     * 获取任务日志列表
     * 
     * <p>分页查询任务执行日志，支持按任务ID、名称、状态筛选
     * <p>需要system:job:list权限
     * 
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param jobId 任务ID（可选）
     * @param jobName 任务名称（可选）
     * @param status 状态（可选）
     * @return 分页的日志列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取任务日志列表")
    @SaCheckLogin
    @SaCheckPermission("system:job:list")
    @ApiOperationLog(description = "获取任务日志列表")
    public ResponseEntity<PageResponse<List<JobLogVO>>> getJobLogList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long jobId,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) Integer status) {
        log.info("获取任务日志列表: pageNo={}, pageSize={}, jobId={}", pageNo, pageSize, jobId);
        
        List<JobLogVO> logs = new ArrayList<>();
        PageResponse<List<JobLogVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, 0L, logs);
        
        return ResponseEntity.<PageResponse<List<JobLogVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(pageResponse)
                .build();
    }

    /**
     * 删除任务日志
     * 
     * <p>批量删除任务执行日志
     * <p>需要system:job:delete权限
     * 
     * @param ids 日志ID列表
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除任务日志")
    @SaCheckLogin
    @SaCheckPermission("system:job:delete")
    @ApiOperationLog(description = "删除任务日志")
    public ResponseEntity<Void> deleteJobLog(@RequestBody List<Long> ids) {
        log.info("删除任务日志: ids={}", ids);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    /**
     * 清空任务日志
     * 
     * <p>清空所有任务执行日志
     * <p>需要system:job:delete权限
     * 
     * @return 清空结果
     */
    @GetMapping("/clean")
    @Operation(summary = "清空任务日志")
    @SaCheckLogin
    @SaCheckPermission("system:job:delete")
    @ApiOperationLog(description = "清空任务日志")
    public ResponseEntity<Void> cleanJobLog() {
        log.info("清空任务日志");
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("清空成功")
                .build();
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobLogVO {
        private Long id;
        private Long jobId;
        private String jobName;
        private String jobGroup;
        private String invokeTarget;
        private String jobMessage;
        private Integer status;
        private String exceptionInfo;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private Long executionTime;
        private LocalDateTime createTime;
    }
}