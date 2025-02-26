package cn.xu.api.web.model.dto.article;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发布文章请求")
public class PublishOrDraftArticleRequest {
    @Schema(description = "文章ID")
    private Long id;

    @Schema(description = "文章标题")
    private String title;
    
    @Schema(description = "文章封面URL")
    private String coverUrl;
    
    @Schema(description = "文章内容")
    private String content;
    
    @Schema(description = "文章描述")
    private String description;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Schema(description = "文章状态，0表示草稿，1表示发布")
    private Integer status;
}