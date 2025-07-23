package cn.xu.application.query.comment.dto;

import cn.xu.application.query.user.UserDTO;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentDTO {
    private Long id;
    private String content;
    private Long likeCount;
    private LocalDateTime createTime;
    private UserDTO user;
    private UserDTO replyUser;
}
