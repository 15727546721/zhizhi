package cn.xu.api.web.dto.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

/**
 * 收藏请求DTO
 */
@Data
public class FavoriteRequest {
    
    @Schema(description = "目标ID")
    @NotNull(message = "目标ID不能为空")
    private Long targetId;
    
    @Schema(description = "目标类型")
    @NotBlank(message = "目标类型不能为空")
    private String targetType;
    
    @Schema(description = "收藏夹ID，可选")
    private Long folderId;
}