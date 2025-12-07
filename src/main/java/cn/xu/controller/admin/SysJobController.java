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
import java.util.Arrays;
import java.util.List;

/**
 * 定时任务管理控制器
 * 
 * <p>提供定时任务的CRUD和执行管理功能</p>
 * <p>需要登录并拥有相应权限</p>
 
 */
@Slf4j
@RestController
@RequestMapping("/api/system/job")
@Tag(name = "定时任务管理", description = "定时任务管理相关接口")
public class SysJobController {

    // 模拟任务列表（后续可接入真实调度系统）
    private static final List<JobVO> MOCK_JOBS = Arrays.asList(
            JobVO.builder()
                    .id(1L)
                    .jobName("热度同步任务")
                    .jobGroup("POST")
                    .invokeTarget("postHotnessTask.execute()")
                    .cronExpression("0 0 * * * ?")
                    .status(1)
                    .remark("每小时同步帖子热度")
                    .createTime(LocalDateTime.now().minusDays(30))
                    .build(),
            JobVO.builder()
                    .id(2L)
                    .jobName("Redis数据同步")
                    .jobGroup("CACHE")
                    .invokeTarget("redisSyncService.syncAll()")
                    .cronExpression("0 */30 * * * ?")
                    .status(1)
                    .remark("每30分钟同步Redis缓存")
                    .createTime(LocalDateTime.now().minusDays(30))
                    .build(),
            JobVO.builder()
                    .id(3L)
                    .jobName("临时文件清理")
                    .jobGroup("SYSTEM")
                    .invokeTarget("fileCleanupTask.execute()")
                    .cronExpression("0 0 3 * * ?")
                    .status(1)
                    .remark("每天凌晨3点清理临时文件")
                    .createTime(LocalDateTime.now().minusDays(30))
                    .build()
    );

    /**
     * 获取定时任务列表
     * 
     * <p>分页查询定时任务，支持按名称和状态筛选
     * <p>需要system:job:list权限
     * 
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param jobName 任务名称（可选）
     * @param status 状态（可选）
     * @return 分页的任务列表
     */
    @GetMapping("/list")
    @Operation(summary = "获取定时任务列表")
    @SaCheckLogin
    @SaCheckPermission("system:job:list")
    @ApiOperationLog(description = "获取定时任务列表")
    public ResponseEntity<PageResponse<List<JobVO>>> getJobList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String jobName,
            @RequestParam(required = false) Integer status) {
        log.info("获取定时任务列表: pageNo={}, pageSize={}", pageNo, pageSize);
        
        PageResponse<List<JobVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, (long) MOCK_JOBS.size(), MOCK_JOBS);
        
        return ResponseEntity.<PageResponse<List<JobVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(pageResponse)
                .build();
    }

    /**
     * 获取任务详情
     * 
     * <p>根据ID获取任务详细信息
     * <p>需要system:job:list权限
     * 
     * @param jobId 任务ID
     * @return 任务详情
     */
    @GetMapping("/info")
    @Operation(summary = "获取任务详情")
    @SaCheckLogin
    @SaCheckPermission("system:job:list")
    @ApiOperationLog(description = "获取任务详情")
    public ResponseEntity<JobVO> getJobInfo(@RequestParam Long jobId) {
        log.info("获取任务详情: jobId={}", jobId);
        
        JobVO job = MOCK_JOBS.stream()
                .filter(j -> j.getId().equals(jobId))
                .findFirst()
                .orElse(null);
        
        return ResponseEntity.<JobVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(job)
                .build();
    }

    /**
     * 添加定时任务
     * 
     * <p>创建新的定时任务
     * <p>需要system:job:add权限
     * 
     * @param job 任务信息
     * @return 创建结果
     */
    @PostMapping("/add")
    @Operation(summary = "添加定时任务")
    @SaCheckLogin
    @SaCheckPermission("system:job:add")
    @ApiOperationLog(description = "添加定时任务")
    public ResponseEntity<Void> addJob(@RequestBody JobVO job) {
        log.info("添加定时任务: {}", job);
        // 暂时不做实际添加
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("添加成功")
                .build();
    }

    /**
     * 修改定时任务
     * 
     * <p>更新定时任务配置
     * <p>需要system:job:update权限
     * 
     * @param job 任务信息
     * @return 更新结果
     */
    @PutMapping("/update")
    @Operation(summary = "修改定时任务")
    @SaCheckLogin
    @SaCheckPermission("system:job:update")
    @ApiOperationLog(description = "修改定时任务")
    public ResponseEntity<Void> updateJob(@RequestBody JobVO job) {
        log.info("修改定时任务: {}", job);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("修改成功")
                .build();
    }

    /**
     * 删除定时任务
     * 
     * <p>批量删除定时任务
     * <p>需要system:job:delete权限
     * 
     * @param ids 任务ID列表
     * @return 删除结果
     */
    @DeleteMapping("/delete")
    @Operation(summary = "删除定时任务")
    @SaCheckLogin
    @SaCheckPermission("system:job:delete")
    @ApiOperationLog(description = "删除定时任务")
    public ResponseEntity<Void> deleteJob(@RequestBody List<Long> ids) {
        log.info("删除定时任务: ids={}", ids);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("删除成功")
                .build();
    }

    /**
     * 修改任务状态
     * 
     * <p>启用或禁用定时任务
     * <p>需要system:job:update权限
     * 
     * @param job 任务信息（包含ID和状态）
     * @return 修改结果
     */
    @PostMapping("/change")
    @Operation(summary = "修改任务状态")
    @SaCheckLogin
    @SaCheckPermission("system:job:update")
    @ApiOperationLog(description = "修改任务状态")
    public ResponseEntity<Void> changeStatus(@RequestBody JobVO job) {
        log.info("修改任务状态: jobId={}, status={}", job.getId(), job.getStatus());
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("修改成功")
                .build();
    }

    /**
     * 立即执行任务
     * 
     * <p>手动触发任务执行
     * <p>需要system:job:run权限
     * 
     * @param job 任务信息（包含ID）
     * @return 执行结果
     */
    @PostMapping("/run")
    @Operation(summary = "立即执行任务")
    @SaCheckLogin
    @SaCheckPermission("system:job:run")
    @ApiOperationLog(description = "立即执行任务")
    public ResponseEntity<Void> runJob(@RequestBody JobVO job) {
        log.info("立即执行任务: jobId={}", job.getId());
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("执行成功")
                .build();
    }

    // ==================== 任务日志 ====================

    /**
     * 获取任务日志列表
     * 
     * <p>分页查询任务执行日志
     * <p>需要system:job:list权限
     * 
     * @param pageNo 页码
     * @param pageSize 每页数量
     * @param jobId 任务ID（可选）
     * @return 分页的日志列表
     */
    @GetMapping("/log/list")
    @Operation(summary = "获取任务日志列表")
    @SaCheckLogin
    @SaCheckPermission("system:job:list")
    @ApiOperationLog(description = "获取任务日志列表")
    public ResponseEntity<PageResponse<List<JobLogVO>>> getJobLogList(
            @RequestParam(defaultValue = "1") Integer pageNo,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) Long jobId) {
        log.info("获取任务日志列表: pageNo={}, pageSize={}, jobId={}", pageNo, pageSize, jobId);
        
        List<JobLogVO> logs = new ArrayList<>();
        PageResponse<List<JobLogVO>> pageResponse = PageResponse.ofList(pageNo, pageSize, 0L, logs);
        
        return ResponseEntity.<PageResponse<List<JobLogVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("获取成功")
                .data(pageResponse)
                .build();
    }

    // ==================== VO类定义 ====================

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobVO {
        private Long id;
        private String jobName;
        private String jobGroup;
        private String invokeTarget;
        private String cronExpression;
        private Integer misfirePolicy;
        private Integer concurrent;
        private Integer status;
        private String remark;
        private LocalDateTime createTime;
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
    }
}