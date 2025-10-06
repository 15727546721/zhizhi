package cn.xu.api.web.model.dto.essay;

import cn.xu.common.request.PageRequest;
import lombok.Data;

@Data
public class TopicQueryRequest extends PageRequest {
    private String name;
}
