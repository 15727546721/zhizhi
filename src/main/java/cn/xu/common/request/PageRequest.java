package cn.xu.common.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * 分页请求参数
 * 用于接收分页查询的请求参数
 *
 *
 */
@Data
@Schema(description = "分页请求参数")
public class PageRequest {
    @Schema(description = "页码", defaultValue = "1")
    @Min(value = 1, message = "页码不能小于1")
    private Integer pageNo = 1;

    @Schema(description = "每页记录数", defaultValue = "10")
    @Min(value = 1, message = "每页记录数不能小于1")
    @Max(value = 100, message = "每页记录数不能大于100")
    private Integer pageSize = 10;

    /**
     * 构造分页请求对象
     */
    public static PageRequest of(int pageNo, int pageSize) {
        PageRequest request = new PageRequest();
        request.setPageNo(pageNo);
        request.setPageSize(pageSize);
        return request;
    }
}
