package cn.xu.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "举报请求DTO")
public class ReportRequestDTO {
    
    @Schema(description = "举报类型 1-文章 2-评论 3-用户 4-话题")
    @NotNull(message = "举报类型不能为空")
    private Integer targetType;
    
    @Schema(description = "被举报的目标ID")
    @NotNull(message = "被举报目标ID不能为空")
    private Long targetId;
    
    @Schema(description = "举报原因")
    @NotBlank(message = "举报原因不能为空")
    private String reason;
    
    @Schema(description = "举报详情")
    private String detail;
}