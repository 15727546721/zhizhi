package cn.xu.model.dto.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 收藏帖子到收藏夹请求对象
 */
@Data
@Schema(description = "收藏帖子到收藏夹请求")
public class CollectPostToFolderRequest {

    @Schema(description = "收藏夹ID", example = "1")
    @NotNull(message = "收藏夹ID不能为空")
    private Long folderId;

    @Schema(description = "帖子ID", example = "1")
    @NotNull(message = "帖子ID不能为空")
    private Long postId;
}