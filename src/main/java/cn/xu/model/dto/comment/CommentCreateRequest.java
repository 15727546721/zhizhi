package cn.xu.model.dto.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.List;

/**
 * 评论创建请求参数
 */
@Data
@Schema(description = "评论创建请求参数")
public class CommentCreateRequest {

    @Schema(description = "评论类型", required = true)
    private Integer type;

    @Schema(description = "评论对象ID", required = true)
    private Long targetId;

    @Schema(description = "父评论ID，顶级评论为null")
    private Long parentId;

    @Schema(description = "被回复的用户ID")
    private Long replyUserId;

    @Schema(description = "评论内容", required = true)
    private String content;

    @Schema(description = "评论附带的图片URL列表")
    private List<String> imageUrls;

    @Schema(description = "@提及的用户ID列表")
    private List<Long> mentionUserIds;
}
