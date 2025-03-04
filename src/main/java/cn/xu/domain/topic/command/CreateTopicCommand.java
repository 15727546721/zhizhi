package cn.xu.domain.topic.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建话题命令")
public class CreateTopicCommand {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "话题标题")
    private String title;

    @Schema(description = "话题内容")
    private String content;

    @Schema(description = "话题图片URL列表")
    private List<String> images;

    @Schema(description = "话题分类ID", required = false)
    private Long categoryId;
} 