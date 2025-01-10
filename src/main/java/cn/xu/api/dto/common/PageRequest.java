package cn.xu.api.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "分页请求参数")
public class PageRequest {

    @Schema(description = "页码", example = "1")
    private Integer pageNo = 1;

    @Schema(description = "每页大小", example = "10")
    private Integer pageSize = 10;

    /**
     * 获取分页起始位置
     */
    public int getOffset() {
        return (pageNo - 1) * pageSize;
    }
}

