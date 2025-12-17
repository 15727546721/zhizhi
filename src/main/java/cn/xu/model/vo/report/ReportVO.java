package cn.xu.model.vo.report;

import cn.xu.model.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 举报VO（用户端）
 *
 * @author xu
 * @since 2025-12-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "举报信息")
public class ReportVO {

    @Schema(description = "举报ID")
    private Long id;

    @Schema(description = "目标类型: 1-帖子 2-评论 3-用户")
    private Integer targetType;

    @Schema(description = "目标类型名称")
    private String targetTypeName;

    @Schema(description = "目标ID")
    private Long targetId;

    @Schema(description = "目标简介（帖子标题/评论内容/用户名）")
    private String targetSummary;

    @Schema(description = "举报原因")
    private Integer reason;

    @Schema(description = "举报原因名称")
    private String reasonName;

    @Schema(description = "详细说明")
    private String description;

    @Schema(description = "状态: 0-待处理 1-已通过 2-已驳回 3-已忽略")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "处理结果说明")
    private String handleResult;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 从Entity转换
     */
    public static ReportVO from(Report report) {
        if (report == null) return null;
        return ReportVO.builder()
                .id(report.getId())
                .targetType(report.getTargetType())
                .targetTypeName(report.getTargetTypeName())
                .targetId(report.getTargetId())
                .reason(report.getReason())
                .reasonName(report.getReasonName())
                .description(report.getDescription())
                .status(report.getStatus())
                .statusName(report.getStatusName())
                .handleResult(report.getHandleResult())
                .handleTime(report.getHandleTime())
                .createTime(report.getCreateTime())
                .build();
    }
}
