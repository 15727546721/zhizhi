package cn.xu.api.dto.common;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PageResponse<T> {
    /**
     * 当前页码
     */
    private Integer pageNum;

    /**
     * 每页数量
     */
    private Integer pageSize;

    /**
     * 总记录数
     */
    private Long total;

    /**
     * 总页数
     */
    private Integer pages;

    /**
     * 分页数据
     */
    private T records;

    /**
     * 是否有下一页
     */
    private Boolean hasNext;

    /**
     * 是否有上一页
     */
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
