package cn.xu.controller.web;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.response.PageResponse;
import cn.xu.common.response.ResponseEntity;
import cn.xu.model.dto.report.ReportRequest;
import cn.xu.model.vo.report.ReportVO;
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

/**
 * 举报控制器（用户端）
 *
 * @author xu
 * @since 2025-12-08
 */
@Slf4j
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Tag(name = "举报管理", description = "用户举报相关接口")
public class ReportController {

    private final ReportService reportService;

    /**
     * 提交举报
     */
    @PostMapping
    @SaCheckLogin
    @Operation(summary = "提交举报")
    @ApiOperationLog(description = "提交举报")
    public ResponseEntity<Long> submitReport(@RequestBody @Validated ReportRequest request) {
        Long userId = LoginUserUtil.getLoginUserId();
        Long reportId = reportService.submitReport(userId, request);
        return ResponseEntity.<Long>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(reportId)
                .info("举报提交成功，我们会尽快处理")
                .build();
    }

    /**
     * 获取我的举报记录
     */
    @GetMapping("/mine")
    @SaCheckLogin
    @Operation(summary = "我的举报记录")
    @ApiOperationLog(description = "我的举报记录")
    public ResponseEntity<PageResponse<List<ReportVO>>> getMyReports(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
            @Parameter(description = "每页数量") @RequestParam(defaultValue = "10") int size) {
        Long userId = LoginUserUtil.getLoginUserId();
        List<ReportVO> list = reportService.getMyReports(userId, page, size);
        long total = reportService.countMyReports(userId);
        return ResponseEntity.<PageResponse<List<ReportVO>>>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(PageResponse.of(page, size, total, list))
                .build();
    }

    /**
     * 获取举报详情
     */
    @GetMapping("/{id}")
    @SaCheckLogin
    @Operation(summary = "举报详情")
    @ApiOperationLog(description = "举报详情")
    public ResponseEntity<ReportVO> getReportDetail(
            @Parameter(description = "举报ID") @PathVariable Long id) {
        Long userId = LoginUserUtil.getLoginUserId();
        ReportVO vo = reportService.getReportDetail(userId, id);
        return ResponseEntity.<ReportVO>builder()
                .code(ResponseCode.SUCCESS.getCode())
                .data(vo)
                .build();
    }
}
