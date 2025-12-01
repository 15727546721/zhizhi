package cn.xu.controller.admin.model.dto.post;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "发布帖子请求")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishPostRequest {
    
    @Schema(description = "帖子标题")
    private String title;
    
    @Schema(description = "帖子封面URL")
    private String coverUrl;
    
    @Schema(description = "帖子内容")
    private String content;
    
    @Schema(description = "帖子描述")
    private String description;

    @Schema(description = "标签ID列表")
    private List<Long> tagIds;

    @Schema(description = "帖子类型")
    private String type;
    
    @Schema(description = "帖子状态，DRAFT表示草稿，PUBLISHED表示发布")
    private String status;
}