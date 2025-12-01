package cn.xu.model.dto.post;

import cn.xu.common.request.PageRequest;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "帖子分页查询请求参数")
public class PostPageQueryRequest extends PageRequest {

    /**
     * 排序方式
     */
    @Schema(description = "排序方式")
    private String sortBy = "hottest";
    
}