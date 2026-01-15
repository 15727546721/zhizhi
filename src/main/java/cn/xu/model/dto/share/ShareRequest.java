package cn.xu.model.dto.share;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 分享请求DTO
 */
@Data
@Schema(description = "分享请求")
public class ShareRequest {

    @NotNull(message = "帖子ID不能为空")
    @Schema(description = "帖子ID", required = true, example = "1")
    private Long postId;

    @Schema(description = "分享平台: copy/weibo/qq/wechat/other", example = "weibo")
    private String platform;
}
