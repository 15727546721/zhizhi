package cn.xu.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Collections;
import java.util.List;

/**
 * 分页响应封装
 * 用于返回分页查询的结果数据结构
 *
 * @param <T> 分页数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "分页响应封装")
public class PageResponse<T> {

    @Schema(description = "当前页码，从1开始", example = "1")
    private Integer pageNo;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "分页数据内容")
    private T data;

    // ========== 辅助方法 ==========

    /**
     * 用于构建分页响应对象
     */
    public static <T> PageResponse<T> of(Integer pageNo, Integer pageSize, Long total, T data) {
        return PageResponse.<T>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .total(total)
                .data(data)
                .build();
    }

    /**
     * 处理列表数据的分页响应
     */
    public static <T> PageResponse<List<T>> ofList(Integer pageNo, Integer pageSize, Long total, List<T> list) {
        return PageResponse.<List<T>>builder()
                .pageNo(pageNo)
                .pageSize(pageSize)
                .total(total)
                .data(list != null ? list : Collections.emptyList())
                .build();
    }

    /**
     * 返回一个空的分页列表
     */
    public static <T> PageResponse<List<T>> emptyList(int pageNo, int pageSize) {
        return ofList(pageNo, pageSize, 0L, Collections.emptyList());
    }

    /**
     * 将Spring Page转换为分页响应对象
     */
    public static <T> PageResponse<List<T>> fromSpringPage(Page<T> page) {
        return ofList(
                page.getNumber() + 1,  // Page索引从0开始，所以加1
                page.getSize(),
                page.getTotalElements(),
                page.getContent()
        );
    }

    // ========== 计算属性 ==========

    @Schema(description = "总页数", accessMode = Schema.AccessMode.READ_ONLY)
    public int getTotalPages() {
        if (pageSize == null || pageSize == 0 || total == null) return 0;
        return (int) Math.ceil((double) total / pageSize);
    }

    @Schema(description = "是否有下一页", accessMode = Schema.AccessMode.READ_ONLY)
    public boolean hasNext() {
        if (pageNo == null || pageSize == null || total == null) return false;
        return (long) pageNo * pageSize < total;
    }
}