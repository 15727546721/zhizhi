package cn.xu.api.web.model.dto.post;

import cn.xu.common.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "帖子分页查询请求参数")
public class PostPageQueryRequest extends PageRequest {
    /**
     * 分类ID
     */
    @Schema(description = "分类ID")
    private Long categoryId;

    /**
     * 排序方式
     */
    @Schema(description = "排序方式")
    private String sortBy = "hottest";
    
    /**
     * 帖子类型
     */
    @Schema(description = "帖子类型")
    private String type;
    
    /**
     * 话题ID
     */
    @Schema(description = "话题ID")
    private Long topicId;
}