package cn.xu.api.controller.web.comment.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 评论请求参数
 */
@Data
@Schema(description = "评论请求参数")
public class CommentRequest {

    @Schema(description = "评论类型（1-文章评论；2-话题评论）")
    private Integer type;

    @Schema(description = "评论目标ID（文章ID或话题ID）")
    private Long targetId;

    @Schema(description = "父评论ID（回复评论时使用）")
    private Long parentId;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "被回复用户ID")
    private Long replyToUserId;

    @Schema(description = "评论内容")
    private String content;
}
