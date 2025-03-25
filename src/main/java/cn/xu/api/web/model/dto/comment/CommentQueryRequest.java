package cn.xu.api.web.model.dto.comment;

import cn.xu.infrastructure.common.request.PageRequest;
import lombok.Data;

@Data
public class CommentQueryRequest extends PageRequest {
    private Integer targetType;
    private Long targetId;
    private String sortType;
}
