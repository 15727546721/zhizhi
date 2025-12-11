package cn.xu.controller.admin.model.vo;

import cn.xu.model.entity.Post;
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
    @Schema(description = "帖子信息")
    private Post post;
    
    @Schema(description = "标签名称列表")
    private List<String> tagNames;
    
    @Schema(description = "标签ID列表")
    private List<Long> tagIds;
}