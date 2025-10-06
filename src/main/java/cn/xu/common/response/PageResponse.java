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
 * 标准分页响应体
 * 用于封装分页查询的统一响应格式
 *
 * @param <T> 分页数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "标准分页响应体")
public class PageResponse<T> {
    @Schema(description = "当前页码（从1开始）", example = "1")
    private Integer pageNo;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize;

    @Schema(description = "总记录数", example = "100")
    private Long total;

    @Schema(description = "分页数据内容")
    private T data;

    // ========== 静态构造方法 ==========

    /**
     * 通用分页构造方法
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
     * 专门处理列表分页的构造方法
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
     * 空列表分页响应
     */
    public static <T> PageResponse<List<T>> emptyList(int pageNo, int pageSize) {
        return ofList(pageNo, pageSize, 0L, Collections.emptyList());
    }

    /**
     * 从Spring Page转换
     */
    public static <T> PageResponse<List<T>> fromSpringPage(Page<T> page) {
        return ofList(
                page.getNumber() + 1,
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