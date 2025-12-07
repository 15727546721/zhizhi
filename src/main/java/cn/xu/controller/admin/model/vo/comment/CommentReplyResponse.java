package cn.xu.controller.admin.model.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 评论回复的响应数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论回复Response")
public class CommentReplyResponse {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "评论用户昵称")
    private String nickName;

    @Schema(description = "评论用户头像")
    private String avatar;

    @Schema(description = "被回复的用户ID")
    private Long replyUserId;

    @Schema(description = "被回复的用户昵称")
    private String replyNickname;

    @Schema(description = "被回复的用户头像")
    private String replyAvatar;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
