package cn.xu.api.web.model.dto.comment;

import cn.xu.infrastructure.common.request.PageRequest;
import lombok.Data;

@Data
public class FindReplyReq extends PageRequest {
    private Long parentId; // 父评论ID
    private String sortType; // 排序类型(HOT, TIME)
}
