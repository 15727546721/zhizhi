package cn.xu.api.web.dto.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 收藏状态响应DTO
 */
@Data
@AllArgsConstructor
public class FavoriteCountResponse {
    
    @Schema(description = "目标ID")
    private Long targetId;
    
    @Schema(description = "目标类型")
    private String targetType;
    
    @Schema(description = "收藏数量，可能为null")
    private Integer count;
    
    @Schema(description = "当前用户是否已收藏")
    private Boolean isFavorited;
}