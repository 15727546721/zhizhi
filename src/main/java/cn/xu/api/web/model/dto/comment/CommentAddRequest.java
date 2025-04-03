package cn.xu.api.web.model.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;


@Data
public class CommentAddRequest {
    @Schema(description = "评论类型（1-文章评论；2-话题评论）")
    private Integer targetType;

    @Schema(description = "评论目标ID（文章ID或话题ID）")
    private Long targetId;

    @Schema(description = "评论内容")
    private String content;
}
