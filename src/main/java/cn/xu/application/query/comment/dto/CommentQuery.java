package cn.xu.application.query.comment.dto;

import lombok.Data;

@Data
public class CommentQuery {
    private Integer targetType;
    private Long targetId;
    private Integer pageNo = 1;
    private Integer pageSize = 10;
    private Integer previewSize = 2; // 默认预览2条回复
}
