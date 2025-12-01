package cn.xu.model.dto.comment;

import cn.xu.common.request.PageRequest;
import lombok.Data;

@Data
public class FindReplyRequest extends PageRequest {
    private Long parentId; // 父评论ID
    private String sortType; // 排序类型(HOT, TIME)
}