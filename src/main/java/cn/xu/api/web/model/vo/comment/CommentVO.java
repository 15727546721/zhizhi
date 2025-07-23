package cn.xu.api.web.model.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论VO")
public class CommentVO {

    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "评论类型（1-文章，2-话题）")
    private Integer type;

    @Schema(description = "评论目标ID")
    private Long targetId;

    @Schema(description = "父评论ID，若为顶级则为 null")
    private Long parentId;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "评论用户昵称")
    private String nickname;

    @Schema(description = "评论用户头像")
    private String avatar;

    @Schema(description = "被回复用户ID")
    private Long replyUserId;

    @Schema(description = "被回复用户昵称")
    private String replyNickname;

    @Schema(description = "被回复用户头像")
    private String replyAvatar;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "图片列表")
    private List<String> imageUrls;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
