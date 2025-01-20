package cn.xu.api.web.controller.comment.request;

import cn.xu.api.web.model.dto.common.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * 评论请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "评论请求参数")
public class CommentRequest extends PageRequest {

    @NotNull(message = "评论类型不能为空")
    @Schema(description = "评论类型（1-文章评论；2-话题评论）")
    private Integer type;

    @NotNull(message = "评论目标ID不能为空")
    @Schema(description = "评论目标ID（文章ID或话题ID）")
    private Long targetId;

    @Schema(description = "父评论ID（回复评论时使用）")
    private Long parentId;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "被回复用户ID")
    private Long replyUserId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000字")
    @Schema(description = "评论内容")
    private String content;
}
