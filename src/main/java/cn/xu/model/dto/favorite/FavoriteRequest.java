package cn.xu.model.dto.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * 收藏请求DTO
 * 用于表示用户收藏操作的请求数据。
 */
@Data
@Schema(description = "收藏请求DTO")
public class FavoriteRequest {

    /** 目标ID
     * - 表示被收藏的目标对象ID
     */
    @Schema(description = "目标ID")
    @NotNull(message = "目标ID不能为空")
    private Long targetId;

    /** 目标类型
     */
    @Schema(description = "目标类型，Post-帖子，Comment-评论")
    @NotBlank(message = "目标类型不能为空")
    private String targetType;

}
