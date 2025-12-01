package cn.xu.model.dto.comment;

import cn.xu.common.request.PageRequest;
import lombok.Data;

@Data
public class CommentQueryRequest extends PageRequest {
    private Integer targetType;
    private Long targetId;
    private String sortType;
}
