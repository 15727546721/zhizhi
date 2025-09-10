package cn.xu.api.web.model.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

@Data
public class ReplyCommentRequest {
    @Schema(description = "评论类型（1-文章评论；2-话题评论）")
    private Integer targetType;

    @Schema(description = "评论目标ID（文章ID或话题ID）")
    private Long targetId;

    @Schema(description = "父评论ID（回复评论时使用）")
    private Long commentId;

    @Schema(description = "被回复用户ID")
    private Long replyUserId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论图片")
    private List<String> imagesUrl;
}
