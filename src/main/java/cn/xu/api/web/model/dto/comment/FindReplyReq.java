package cn.xu.api.web.model.dto.comment;

import cn.xu.infrastructure.common.request.PageRequest;
import lombok.Data;

@Data
public class FindReplyReq extends PageRequest {
    private Long commentId;
}
