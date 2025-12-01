package cn.xu.model.dto.comment;

import cn.xu.common.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * 评论请求参数
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "评论请求参数")
public class CommentCreateRequest extends PageRequest {
    @Schema(description = "评论类型（1-帖子评论）")
    private Integer type;

    @Schema(description = "评论目标ID（帖子ID）")
    private Long targetId;

    @Schema(description = "父评论ID（回复评论时使用）")
    private Long parentId;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "被回复用户ID")
    private Long replyUserId;

    @Schema(description = "评论内容")
    private String content;
    
    @Schema(description = "评论图片URL列表")
    private List<String> imageUrls;
    
    @Schema(description = "@提及的用户ID列表")
    private List<Long> mentionUserIds;
}