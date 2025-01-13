package cn.xu.api.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.experimental.SuperBuilder;

import java.util.List;

@Data
@SuperBuilder
@Schema(description = "分页响应对象")
public class PageResponse<T> {
    
    @Schema(description = "当前页码")
    private Integer pageNum;
    
    @Schema(description = "每页数量")
    private Integer pageSize;
    
    @Schema(description = "总记录数")
    private Long total;
    
    @Schema(description = "总页数")
    private Integer pages;
    
    @Schema(description = "分页数据")
    private T records;
    
    @Schema(description = "是否有下一页")
    private Boolean hasNext;
    
    @Schema(description = "是否有上一页")
    private Boolean hasPrevious;
    
    public static <T> PageResponse<T> of(Integer pageNum, Integer pageSize, Long total, T records) {
        int pages = (int) Math.ceil((double) total / pageSize);
        return PageResponse.<T>builder()
                .pageNum(pageNum)
                .pageSize(pageSize)
                .total(total)
                .pages(pages)
                .records(records)
                .hasNext(pageNum < pages)
                .hasPrevious(pageNum > 1)
                .build();
    }
}
