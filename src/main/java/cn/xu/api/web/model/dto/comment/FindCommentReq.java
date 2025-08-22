package cn.xu.api.web.model.dto.comment;

import cn.xu.infrastructure.common.request.PageRequest;
import lombok.Data;

@Data
public class FindCommentReq extends PageRequest {
    private Long targetId;
    private Integer targetType;
    private int page;
    private String sortType;
}
