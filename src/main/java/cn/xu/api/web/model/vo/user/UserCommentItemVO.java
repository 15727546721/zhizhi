package cn.xu.api.web.model.vo.user;

import cn.xu.api.web.model.vo.comment.CommentResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户评论列表项VO
 * 包含评论信息和被评论的目标信息
 * 
 * @author zhizhi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户评论列表项VO")
public class UserCommentItemVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "评论信息")
    private CommentResponse comment;
    
    @Schema(description = "被评论的目标ID（帖子ID等）")
    private Long targetId;
    
    @Schema(description = "被评论的目标标题")
    private String targetTitle;
    
    @Schema(description = "被评论的目标类型（1-帖子，2-随笔等）")
    private Integer targetType;
    
    @Schema(description = "目标链接")
    private String targetUrl;
}

