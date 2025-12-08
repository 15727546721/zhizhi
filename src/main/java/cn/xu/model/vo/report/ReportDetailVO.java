package cn.xu.model.vo.report;

import cn.xu.model.entity.Report;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 举报详情VO（管理端）
 *
 * @author xu
 * @since 2025-12-08
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "举报详情")
public class ReportDetailVO {

    @Schema(description = "举报ID")
    private Long id;

    // ==================== 举报人信息 ====================

    @Schema(description = "举报人ID")
    private Long reporterId;

    @Schema(description = "举报人昵称")
    private String reporterNickname;

    @Schema(description = "举报人头像")
    private String reporterAvatar;

    // ==================== 举报目标 ====================

    @Schema(description = "目标类型: 1-帖子 2-评论 3-用户")
    private Integer targetType;

    @Schema(description = "目标类型名称")
    private String targetTypeName;

    @Schema(description = "目标ID")
    private Long targetId;

    @Schema(description = "目标内容（帖子标题/评论内容/用户名）")
    private String targetContent;

    @Schema(description = "被举报用户ID")
    private Long targetUserId;

    @Schema(description = "被举报用户昵称")
    private String targetUserNickname;

    @Schema(description = "被举报用户头像")
    private String targetUserAvatar;

    // ==================== 举报内容 ====================

    @Schema(description = "举报原因")
    private Integer reason;

    @Schema(description = "举报原因名称")
    private String reasonName;

    @Schema(description = "详细说明")
    private String description;

    @Schema(description = "截图证据URL列表")
    private List<String> evidenceUrls;

    // ==================== 处理信息 ====================

    @Schema(description = "状态: 0-待处理 1-已通过 2-已驳回 3-已忽略")
    private Integer status;

    @Schema(description = "状态名称")
    private String statusName;

    @Schema(description = "处理人ID")
    private Long handlerId;

    @Schema(description = "处理人昵称")
    private String handlerNickname;

    @Schema(description = "处理结果说明")
    private String handleResult;

    @Schema(description = "处罚措施")
    private Integer handleAction;

    @Schema(description = "处罚措施名称")
    private String handleActionName;

    @Schema(description = "处理时间")
    private LocalDateTime handleTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    /**
     * 从Entity转换（基础信息，不含用户信息）
     */
    public static ReportDetailVO from(Report report) {
        if (report == null) return null;
        return ReportDetailVO.builder()
                .id(report.getId())
                .reporterId(report.getReporterId())
                .targetType(report.getTargetType())
                .targetTypeName(report.getTargetTypeName())
                .targetId(report.getTargetId())
                .targetUserId(report.getTargetUserId())
                .reason(report.getReason())
                .reasonName(report.getReasonName())
                .description(report.getDescription())
                .status(report.getStatus())
                .statusName(report.getStatusName())
                .handlerId(report.getHandlerId())
                .handleResult(report.getHandleResult())
                .handleAction(report.getHandleAction())
                .handleActionName(report.getHandleActionName())
                .handleTime(report.getHandleTime())
                .createTime(report.getCreateTime())
                .build();
    }
}
