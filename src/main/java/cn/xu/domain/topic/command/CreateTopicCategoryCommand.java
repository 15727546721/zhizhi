package cn.xu.domain.topic.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建话题分类命令")
public class CreateTopicCategoryCommand {
    
    @Schema(description = "分类名称")
    private String name;
    
    @Schema(description = "分类描述")
    private String description;
    
    @Schema(description = "排序序号")
    private Integer sort;
} 