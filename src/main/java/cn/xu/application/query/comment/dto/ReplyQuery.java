package cn.xu.application.query.comment.dto;

import lombok.Data;

@Data
public class ReplyQuery {
    private Long commentId;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
}
