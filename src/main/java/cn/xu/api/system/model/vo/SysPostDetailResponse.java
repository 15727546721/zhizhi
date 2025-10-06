package cn.xu.api.system.model.vo;

import cn.xu.domain.post.model.aggregate.PostAndAuthorAggregate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "系统帖子详情Response")
public class SysPostDetailResponse {
    @Schema(description = "帖子和作者聚合信息")
    PostAndAuthorAggregate postAndAuthorAggregate;
    
    @Schema(description = "分类名称")
    private String categoryName;
    
    @Schema(description = "标签列表")
    private List<String> tags;
}