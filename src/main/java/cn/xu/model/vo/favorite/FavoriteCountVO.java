package cn.xu.model.vo.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏统计VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收藏统计VO")
public class FavoriteCountVO {

    /**
     * 目标ID
     */
    @Schema(description = "目标ID")
    private Long targetId;

    /**
     * 目标类型
     */
    @Schema(description = "目标类型")
    private String targetType;

    /**
     * 收藏的数量
     */
    @Schema(description = "收藏次数")
    private Integer count;

    /**
     * 当前用户是否已经收藏
     */
    @Schema(description = "当前用户是否收藏")
    private Boolean isFavorited;
}
