package cn.xu.api.web.model.vo.comment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "评论DTO")
public class CommentVO {
    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "评论类型")
    private Integer type;

    @Schema(description = "评论目标ID")
    private Long targetId;

    @Schema(description = "父评论ID")
    private Long parentId;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "评论用户昵称")
    private String nickname;

    @Schema(description = "评论用户头像")
    private String avatar;

    @Schema(description = "被回复用户ID")
    private Long replyUserId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 