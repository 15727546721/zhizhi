package cn.xu.api.web.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@Schema(description = "处理举报请求DTO")
public class HandleReportRequestDTO {
    
    @Schema(description = "举报ID")
    @NotNull(message = "举报ID不能为空")
    private Long reportId;
    
    @Schema(description = "处理结果")
    @NotBlank(message = "处理结果不能为空")
    private String handleResult;
    
    @Schema(description = "是否处理 1-已处理 2-已忽略")
    @NotNull(message = "处理状态不能为空")
    private Integer status;
}