package cn.xu.api.dto.article;

import cn.xu.api.dto.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "文章查询请求参数")
public class ArticleRequest extends PageRequest {

    @Schema(description = "文章标题")
    private String title;

    @Schema(description = "分类ID")
    private Long categoryId;

    @Schema(description = "标签ID")
    private Long tagId;
}
