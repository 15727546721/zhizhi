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
 * @author xu
 * @since 2025-12-01
 */
@Slf4j
@RestController
@RequestMapping("/api/system/jobLog")
@Tag(name = "任务日志管理", description = "任务日志管理相关接口")
public class SysJobLogController {

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
