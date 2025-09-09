package cn.xu.api.web.model.request.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 收藏文章到收藏夹请求对象
 */
@Data
@Schema(description = "收藏文章到收藏夹请求")
public class CollectArticleToFolderRequest {

    @Schema(description = "收藏夹ID", example = "1")
    @NotNull(message = "收藏夹ID不能为空")
    private Long folderId;

    @Schema(description = "文章ID", example = "1")
    @NotNull(message = "文章ID不能为空")
    private Long articleId;
}