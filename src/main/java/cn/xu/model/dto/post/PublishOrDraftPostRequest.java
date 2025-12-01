package cn.xu.model.dto.post;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "发布帖子请求")
public class PublishOrDraftPostRequest {
    @Schema(description = "帖子ID")
    private Long id;

    @NotBlank(message = "帖子标题不能为空")
    @Size(min = 1, max = 100, message = "帖子标题长度必须在1-100个字符之间")
    @Schema(description = "帖子标题")
    private String title;
    
    @Schema(description = "帖子封面URL")
    private String coverUrl;
    
    @NotBlank(message = "帖子内容不能为空")
    @Size(min = 1, max = 10000, message = "帖子内容长度必须在1-10000个字符之间")
    @Schema(description = "帖子内容")
    private String content;
    
    @Size(max = 500, message = "帖子描述长度不能超过500个字符")
    @Schema(description = "帖子描述")
    private String description;
    
    @Schema(description = "标签ID列表（至少1个，最多3个）")
    private List<Long> tagIds;

    @Schema(description = "帖子状态，DRAFT表示草稿，PUBLISHED表示发布")
    private String status;
}