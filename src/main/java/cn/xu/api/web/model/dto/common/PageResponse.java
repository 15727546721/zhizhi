package cn.xu.api.web.model.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应")
public class PageResponse<T> {
    @Schema(description = "当前页码")
    private Integer pageNo;

    @Schema(description = "每页数量")
    private Integer pageSize;

    @Schema(description = "总记录数")
    private Long total;

    @Schema(description = "数据列表")
    private T data;

    public static <T> PageResponse<T> of(Integer pageNo, Integer pageSize, Long total, T data) {
        return PageResponse.<T>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .total(total)
                .data(data)
                .build();
    }
}
