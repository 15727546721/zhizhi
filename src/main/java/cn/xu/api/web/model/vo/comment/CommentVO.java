package cn.xu.api.web.model.vo.comment;

import cn.xu.domain.user.model.entity.UserEntity;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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

    @Schema(description = "评论用户")
    private UserEntity user;

    @Schema(description = "回复用户")
    private UserEntity replyUser;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "图片列表")
    private List<String> imageUrls;

    @Schema(description = "点赞数量")
    private Integer likeCount;

    @Schema(description = "回复数量")
    private Integer replyCount;

    @Schema(description = "子评论列表")
    private List<CommentVO> children;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
