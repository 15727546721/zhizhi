package cn.xu.domain.essay.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "创建话题分类命令")
public class CreateTopicCommand {

    @Schema(description = "分类名称")
    private String name;

}