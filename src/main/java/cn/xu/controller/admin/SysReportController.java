package cn.xu.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckPermission;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.report.HandleReportRequest;
import cn.xu.model.dto.report.ReportQueryRequest;
import cn.xu.model.vo.report.ReportDetailVO;
import cn.xu.service.report.ReportService;
import cn.xu.support.util.LoginUserUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 举报管理控制器（管理端）
 *
 * @author xu
 * @since 2025-12-08
 */
@Slf4j
@RestController
@RequestMapping("/api/system/reports")
@RequiredArgsConstructor
@Tag(name = "举报管理（管理端）", description = "举报审核处理接口")
public class SysReportController {

    private final ReportService reportService;

    /**
     * 分页查询举报列表
     */
    @GetMapping
    @SaCheckLogin
    @Operation(summary = "举报列表")
    @ApiOperationLog(description = "查询举报列表")
    public ResponseEntity<PageResponse<List<ReportDetailVO>>> list(ReportQueryRequest request) {
        List<ReportDetailVO> list = reportService.queryReports(request);
        long total = reportService.countReports(request);
        return ResponseEntity.<PageResponse<List<ReportDetailVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(PageResponse.of(request.getPageNo(), request.getPageSize(), total, list))
                .build();
    }

    /**
     * 获取举报详情
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "举报详情")
    @ApiOperationLog(description = "获取举报详情")
    public ResponseEntity<ReportDetailVO> getDetail(
            @Parameter(description = "举报ID") @PathVariable Long id) {
        ReportDetailVO vo = reportService.getReportDetailForAdmin(id);
        return ResponseEntity.<ReportDetailVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(vo)
                .build();
    }

    /**
     * 处理举报
     */
    @PutMapping("/{id}/handle")
    @SaCheckLogin
    @Operation(summary = "处理举报")
    @ApiOperationLog(description = "处理举报")
    public ResponseEntity<Void> handleReport(
            @Parameter(description = "举报ID") @PathVariable Long id,
            @RequestBody @Validated HandleReportRequest request) {
        Long handlerId = LoginUserUtil.getLoginUserId();
        reportService.handleReport(handlerId, id, request);
        return ResponseEntity.<Void>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .info("处理成功")
                .build();
    }

    /**
     * 批量忽略举报
     */
    @PutMapping("/batch-ignore")
    @SaCheckLogin
    @Operation(summary = "批量忽略")
    @ApiOperationLog(description = "批量忽略举报")
    public ResponseEntity<Integer> batchIgnore(@RequestBody List<Long> ids) {
        Long handlerId = LoginUserUtil.getLoginUserId();
        int count = reportService.batchIgnore(handlerId, ids);
        return ResponseEntity.<Integer>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(count)
                .info("已忽略" + count + "条举报")
                .build();
    }

    /**
     * 获取举报统计
     */
    @GetMapping("/stats")
    @SaCheckLogin
    @Operation(summary = "举报统计")
    @ApiOperationLog(description = "获取举报统计")
    public ResponseEntity<Map<String, Object>> getStats() {
        Map<String, Object> stats = reportService.getStats();
        return ResponseEntity.<Map<String, Object>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(stats)
                .build();
    }
}
