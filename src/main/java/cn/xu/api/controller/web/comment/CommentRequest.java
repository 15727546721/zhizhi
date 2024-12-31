package cn.xu.api.controller.web.comment;

import lombok.Data;

@Data
public class CommentRequest {
    private Integer type;
    private Long parentId;
    private String content;
    private Long userId;
    private Long replyToUserId;
    private Long targetId;
}
