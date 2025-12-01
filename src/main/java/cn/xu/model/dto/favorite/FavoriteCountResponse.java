package cn.xu.model.dto.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 收藏状态响应DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收藏状态响应")
public class FavoriteCountResponse {
    
    @Schema(description = "目标ID")
    private Long targetId;
    
    @Schema(description = "目标类型")
    private String targetType;
    
    @Schema(description = "收藏数量")
    private Integer count;
    
    @Schema(description = "当前用户是否已收藏")
    private Boolean isFavorited;
}
