package cn.xu.api.web.model.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "顶层评论")
public class TopCommentResponse {
    @Schema(description = "评论ID")
    private Long id;
    @Schema(description = "评论内容")
    private String content;
    @Schema(description = "评论时间")
    private String time;
    @Schema(description = "评论用户ID")
    private Long userId;
    @Schema(description = "评论用户昵称")
    private String nickname;
    @Schema(description = "评论用户头像")
    private String avatar;
    @Schema(description = "评论点赞数")
    private Integer likeCount;
    @Schema(description = "评论子评论数")
    private Integer subCommentCount;
}