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
@Schema(description = "评论列表DTO")
public class CommentListVO {
    @Schema(description = "评论ID")
    private Long id;

    @Schema(description = "评论用户ID")
    private Long userId;

    @Schema(description = "被回复用户ID")
    private Long replyUserId;

    @Schema(description = "评论内容")
    private String content;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "评论用户信息")
    private UserEntity userInfo;

    @Schema(description = "子评论列表")
    private List<CommentListVO> replyComment;

}
