package cn.xu.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 举报查询请求
 *
 * @author xu
 * @since 2025-12-08
 */
@Data
@Schema(description = "举报查询请求")
public class ReportQueryRequest {

    @Schema(description = "状态: 0-待处理 1-已通过 2-已驳回 3-已忽略")
    private Integer status;

    @Schema(description = "目标类型: 1-帖子 2-评论 3-用户")
    private Integer targetType;

    @Schema(description = "举报原因")
    private Integer reason;

    @Schema(description = "举报人ID")
    private Long reporterId;

    @Schema(description = "被举报用户ID")
    private Long targetUserId;

    @Schema(description = "关键词搜索（举报说明）")
    private String keyword;

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页数量", example = "10")
    private Integer pageSize = 10;
}
