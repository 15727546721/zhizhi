package cn.xu.model.dto.comment;

import cn.xu.common.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "查找评论请求")
public class FindCommentRequest extends PageRequest {
    @Schema(description = "目标ID")
    private Long targetId;
    
    @Schema(description = "目标类型")
    private Integer targetType;
    
    @Schema(description = "排序类型")
    private String sortType;
}