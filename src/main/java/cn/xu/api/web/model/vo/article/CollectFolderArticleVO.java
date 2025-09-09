package cn.xu.api.web.model.vo.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹文章VO对象
 */
@Data
@Schema(description = "收藏夹文章信息")
public class CollectFolderArticleVO {

    @Schema(description = "主键ID")
    private Long id;

    @Schema(description = "收藏夹ID")
    private Long folderId;

    @Schema(description = "文章ID")
    private Long articleId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "收藏时间")
    private LocalDateTime createTime;
}