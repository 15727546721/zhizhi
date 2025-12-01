package cn.xu.model.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@Schema(description = "回复评论请求")
public class ReplyCommentRequest {
    @NotNull(message = "评论类型不能为空")
    @Schema(description = "评论类型（1-文章评论；2-话题评论）")
    private Integer targetType;

    @NotNull(message = "评论目标ID不能为空")
    @Schema(description = "评论目标ID（文章ID或话题ID）")
    private Long targetId;

    @NotNull(message = "父评论ID不能为空")
    @Schema(description = "父评论ID（回复评论时使用）")
    private Long commentId;

    @NotNull(message = "被回复用户ID不能为空")
    @Schema(description = "被回复用户ID")
    private Long replyUserId;

    @NotBlank(message = "评论内容不能为空")
    @Size(max = 1000, message = "评论内容不能超过1000字")
    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论图片")
    private List<String> imagesUrl;
}