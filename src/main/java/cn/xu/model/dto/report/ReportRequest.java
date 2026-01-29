package cn.xu.model.dto.report;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * 提交举报请求
 *
 * @author xu
 * @since 2025-12-08
 */
@Data
@Schema(description = "提交举报请求")
public class ReportRequest {

    @NotNull(message = "目标类型不能为空")
    @Min(value = 1, message = "目标类型无效")
    @Max(value = 3, message = "目标类型无效")
    @Schema(description = "目标类型: 1-帖子 2-评论 3-用户", example = "1")
    private Integer targetType;

    @NotNull(message = "目标ID不能为空")
    @Schema(description = "目标ID", example = "1")
    private Long targetId;

    @NotNull(message = "举报原因不能为空")
    @Min(value = 1, message = "举报原因无效")
    @Max(value = 6, message = "举报原因无效")
    @Schema(description = "举报原因: 1-垃圾广告 2-违法违规 3-色情低俗 4-人身攻击 5-抄袭侵权 6-其他", example = "1")
    private Integer reason;

    @Size(max = 500, message = "详细说明不能超过500字")
    @Schema(description = "详细说明")
    private String description;

    @Schema(description = "截图证据URL列表")
    private List<String> evidenceUrls;
}
