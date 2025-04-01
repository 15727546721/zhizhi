package cn.xu.domain.essay.command;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "创建随笔命令")
public class CreateEssayCommand {

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "随笔内容")
    private String content;

    @Schema(description = "图片URL列表")
    private List<String> images;

    @Schema(description = "话题", required = false)
    private List<String> topics;
} 