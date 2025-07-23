package cn.xu.application.query.comment.dto;

import lombok.Data;

@Data
public class CommentCountDTO {
    private Long parentId;
    private Integer count;
}

