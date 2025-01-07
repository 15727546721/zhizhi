package cn.xu.api.controller.web.comment.dto;

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
public class CommentListDTO {
    private Long id;
    private Long userId;
    private Long replyToUserId;
    private String content;
    private LocalDateTime createTime;
    private UserEntity userInfo;
    private List<CommentListDTO> replyComment;
}
