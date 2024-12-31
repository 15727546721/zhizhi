package cn.xu.api.controller.web.comment;

import cn.xu.domain.user.model.entity.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
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
    private Date createTime;
    private UserEntity userInfo;
    private List<CommentListDTO> replyComment;
}
