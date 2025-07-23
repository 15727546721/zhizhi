package cn.xu.api.web.model.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "添加评论请求对象")
public class CommentAddRequest {

    @Schema(description = "评论类型（1-文章评论；2-话题评论）", required = true)
    private Integer targetType;

    @Schema(description = "评论目标ID（文章ID或话题ID）", required = true)
    private Long targetId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论附带的图片地址列表（最多9张）")
    private List<String> imageUrls;
}
