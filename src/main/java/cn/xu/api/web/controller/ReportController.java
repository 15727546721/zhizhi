package cn.xu.api.web.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.stp.StpUtil;
import cn.xu.api.web.model.dto.report.HandleReportRequestDTO;
import cn.xu.api.web.model.dto.report.ReportRequestDTO;
import cn.xu.api.web.model.vo.report.ReportResponse;
import cn.xu.common.ResponseCode;
import cn.xu.common.annotation.ApiOperationLog;
import cn.xu.common.exception.BusinessException;
import cn.xu.common.response.ResponseEntity;
import cn.xu.domain.report.model.entity.ReportEntity;
import cn.xu.domain.report.service.ReportDomainService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/report")
@Tag(name = "举报接口", description = "举报相关接口")
@RequiredArgsConstructor
public class ReportController {
    
    private final ReportDomainService reportDomainService;
    
    @PostMapping("/create")
    @SaCheckLogin
    @Operation(summary = "创建举报")
    @ApiOperationLog(description = "创建举报")
    public ResponseEntity<Long> createReport(@RequestBody @Valid ReportRequestDTO requestDTO) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 创建举报
            ReportEntity report = reportDomainService.createReport(
                    userId,
                    requestDTO.getTargetType(),
                    requestDTO.getTargetId(),
                    requestDTO.getReason(),
                    requestDTO.getDetail()
            );
            
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(report.getId())
                    .info("举报成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("创建举报失败: {}", e.getMessage());
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("创建举报异常", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("举报失败，请稍后重试")
                    .build();
        }
    }
    
    @PostMapping("/handle")
    @SaCheckRole("admin")
    @Operation(summary = "处理举报")
    @ApiOperationLog(description = "处理举报")
    public ResponseEntity<Boolean> handleReport(@RequestBody @Valid HandleReportRequestDTO requestDTO) {
        try {
            Long userId = StpUtil.getLoginIdAsLong();
            
            // 处理举报
            ReportEntity report = reportDomainService.handleReport(
                    requestDTO.getReportId(),
                    userId,
                    requestDTO.getHandleResult(),
                    requestDTO.getStatus() == 1 // 1表示已处理，其他表示已忽略
            );
            
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(true)
                    .info("处理成功")
                    .build();
        } catch (BusinessException e) {
            log.warn("处理举报失败: {}", e.getMessage());
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                    .info(e.getMessage())
                    .build();
        } catch (Exception e) {
            log.error("处理举报异常", e);
            return ResponseEntity.<Boolean>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("处理失败，请稍后重试")
                    .build();
        }
    }
    
    @GetMapping("/list")
    @SaCheckRole("admin")
    @Operation(summary = "获取举报列表")
    @ApiOperationLog(description = "获取举报列表")
    public ResponseEntity<List<ReportResponse>> getReportList(
            @Parameter(description = "页码，默认为0") @RequestParam(defaultValue = "0") Integer page,
            @Parameter(description = "每页数量，默认为10") @RequestParam(defaultValue = "10") Integer size) {
        try {
            // 获取举报列表
            List<ReportEntity> reports = reportDomainService.getReportsByPage(page, size);
            
            // 转换为VO
            List<ReportResponse> reportVOs = reports.stream()
                    .map(ReportResponse::from)
                    .collect(Collectors.toList());
            
            return ResponseEntity.<List<ReportResponse>>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(reportVOs)
                    .build();
        } catch (Exception e) {
            log.error("获取举报列表异常", e);
            return ResponseEntity.<List<ReportResponse>>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取举报列表失败")
                    .build();
        }
    }
    
    @GetMapping("/pending/count")
    @SaCheckRole("admin")
    @Operation(summary = "获取未处理举报数量")
    @ApiOperationLog(description = "获取未处理举报数量")
    public ResponseEntity<Long> getPendingReportCount() {
        try {
            long count = reportDomainService.getPendingReportCount();
            
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(count)
                    .build();
        } catch (Exception e) {
            log.error("获取未处理举报数量异常", e);
            return ResponseEntity.<Long>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取未处理举报数量失败")
                    .build();
        }
    }
    
    @GetMapping("/{reportId}")
    @SaCheckRole("admin")
    @Operation(summary = "获取举报详情")
    @ApiOperationLog(description = "获取举报详情")
    public ResponseEntity<ReportResponse> getReportDetail(@Parameter(description = "举报ID") @PathVariable Long reportId) {
        try {
            Optional<ReportEntity> reportOpt = reportDomainService.getReportById(reportId);
            
            if (!reportOpt.isPresent()) {
                return ResponseEntity.<ReportResponse>builder()
                        .code(ResponseCode.ILLEGAL_PARAMETER.getCode())
                        .info("举报信息不存在")
                        .build();
            }
            
            ReportResponse reportVO = ReportResponse.from(reportOpt.get());
            
            return ResponseEntity.<ReportResponse>builder()
                    .code(ResponseCode.SUCCESS.getCode())
                    .data(reportVO)
                    .build();
        } catch (Exception e) {
            log.error("获取举报详情异常", e);
            return ResponseEntity.<ReportResponse>builder()
                    .code(ResponseCode.UN_ERROR.getCode())
                    .info("获取举报详情失败")
                    .build();
        }
    }
}