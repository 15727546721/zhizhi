package cn.xu.api.web.model.dto.essay;

import cn.xu.infrastructure.common.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
public class EssayQueryRequest extends PageRequest {
    @Schema(description = "话题")
    private String topic;
    @Schema(description = "查询类型(最新，热门，精华)")
    private String type;
}
