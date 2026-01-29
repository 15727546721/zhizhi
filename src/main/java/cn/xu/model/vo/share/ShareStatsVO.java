package cn.xu.model.vo.share;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * 分享统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分享统计")
public class ShareStatsVO {

    @Schema(description = "帖子ID")
    private Long postId;

    @Schema(description = "总分享数")
    private Long totalCount;

    @Schema(description = "各平台分享数")
    private Map<String, Long> platformStats;

    @Schema(description = "复制链接数")
    private Long copyCount;

    @Schema(description = "微博分享数")
    private Long weiboCount;

    @Schema(description = "QQ分享数")
    private Long qqCount;

    @Schema(description = "微信分享数")
    private Long wechatCount;
}
