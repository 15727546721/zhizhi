package cn.xu.api.web.model.vo.report;

import cn.xu.domain.report.model.entity.ReportEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "举报响应对象")
public class ReportResponse {
    
    @Schema(description = "举报ID")
    private Long id;
    
    @Schema(description = "举报类型 1-文章 2-评论 3-用户 4-话题")
    private Integer targetType;
    
    @Schema(description = "被举报的目标ID")
    private Long targetId;
    
    @Schema(description = "举报人ID")
    private Long reporterId;
    
    @Schema(description = "举报人昵称")
    private String reporterNickname;
    
    @Schema(description = "举报原因")
    private String reason;
    
    @Schema(description = "举报详情")
    private String detail;
    
    @Schema(description = "举报状态 0-待处理 1-已处理 2-已忽略")
    private Integer status;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "处理时间")
    private LocalDateTime handleTime;
    
    @Schema(description = "处理人ID")
    private Long handlerId;
    
    @Schema(description = "处理人昵称")
    private String handlerNickname;
    
    @Schema(description = "处理结果")
    private String handleResult;
    
    public static ReportResponse from(ReportEntity reportEntity) {
        ReportResponse response = new ReportResponse();
        response.setId(reportEntity.getId());
        response.setTargetType(reportEntity.getTargetType());
        response.setTargetId(reportEntity.getTargetId());
        response.setReporterId(reportEntity.getReporterId());
        response.setReason(reportEntity.getReason());
        response.setDetail(reportEntity.getDetail());
        response.setStatus(reportEntity.getStatus());
        response.setCreateTime(reportEntity.getCreateTime());
        response.setHandleTime(reportEntity.getHandleTime());
        response.setHandlerId(reportEntity.getHandlerId());
        response.setHandleResult(reportEntity.getHandleResult());
        return response;
    }
}