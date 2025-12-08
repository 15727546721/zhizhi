package cn.xu.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 处理举报请求
 *
 * @author xu
 * @since 2025-12-08
 */
@Data
@Schema(description = "处理举报请求")
public class HandleReportRequest {

    @NotNull(message = "处理状态不能为空")
    @Min(value = 1, message = "处理状态无效")
    @Max(value = 3, message = "处理状态无效")
    @Schema(description = "处理状态: 1-通过 2-驳回 3-忽略", example = "1")
    private Integer status;

    @Min(value = 0, message = "处罚措施无效")
    @Max(value = 5, message = "处罚措施无效")
    @Schema(description = "处罚措施: 0-无 1-删除内容 2-警告 3-禁言7天 4-禁言30天 5-永久封号", example = "1")
    private Integer handleAction;

    @Size(max = 500, message = "处理说明不能超过500字")
    @Schema(description = "处理结果说明")
    private String handleResult;
}
