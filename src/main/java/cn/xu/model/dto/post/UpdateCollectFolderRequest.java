package cn.xu.model.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;

/**
 * 更新收藏夹请求对象
 */
@Data
@Schema(description = "更新收藏夹请求")
public class UpdateCollectFolderRequest {

    @Schema(description = "收藏夹名称", example = "技术帖子")
    @Size(max = 100, message = "收藏夹名称不能超过100个字符")
    private String name;

    @Schema(description = "收藏夹描述", example = "收集技术相关的帖子")
    @Size(max = 255, message = "收藏夹描述不能超过255个字符")
    private String description;

    @Schema(description = "是否公开（0-私密，1-公开）", example = "1")
    private Integer isPublic;
}