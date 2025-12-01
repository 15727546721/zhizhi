package cn.xu.model.dto.post;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.Size;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "草稿请求")
public class DraftRequest {
    @Schema(description = "帖子ID")
    private Long id;
    
    @Size(max = 100, message = "帖子标题长度不能超过100个字符")
    @Schema(description = "帖子标题")
    private String title;
    
    @Size(max = 10000, message = "帖子内容长度不能超过10000个字符")
    @Schema(description = "帖子内容")
    private String content;
    
    @Schema(description = "帖子描述")
    private String description;
    
    @Schema(description = "标签ID列表")
    private List<Long> tagIds;
    
    @Schema(description = "封面图URL")
    private String coverUrl;
}