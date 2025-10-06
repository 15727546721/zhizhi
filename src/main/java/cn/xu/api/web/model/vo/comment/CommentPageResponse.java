package cn.xu.api.web.model.vo.comment;

import cn.xu.domain.user.model.entity.UserEntity;
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
public class CommentPageResponse {
    private Long id;
    private String content;
    private UserEntity user;
    private UserEntity replyUser;
    private Long likeCount;
    private Long replyCount;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    private Boolean isLiked;
    private List<CommentPageResponse> replyList;
}