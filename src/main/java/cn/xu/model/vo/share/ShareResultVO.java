package cn.xu.model.vo.share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 分享结果VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分享结果")
public class ShareResultVO {

    @Schema(description = "分享记录ID")
    private Long shareId;

    @Schema(description = "是否增加了分享计数（1小时内重复分享不增加）")
    private Boolean countIncreased;

    @Schema(description = "当前分享总数")
    private Long totalShareCount;
}
