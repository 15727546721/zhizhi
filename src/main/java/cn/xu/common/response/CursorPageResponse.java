package cn.xu.common.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;

/**
 * 游标分页响应封装
 * <p>
 * 相比传统 OFFSET 分页的优势：
 * <ul>
 *   <li>性能稳定：无论翻到第几页，查询性能一致</li>
 *   <li>数据一致：避免翻页时数据重复或遗漏</li>
 *   <li>适合无限滚动场景</li>
 * </ul>
 *
 * @param <T> 分页数据类型
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "游标分页响应封装")
public class CursorPageResponse<T> {

    @Schema(description = "分页数据内容")
    private List<T> data;

    @Schema(description = "下一页游标（为空表示没有更多数据）")
    private String nextCursor;

    @Schema(description = "是否有更多数据")
    private Boolean hasMore;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize;

    @Schema(description = "总记录数（可选，首次请求时返回）")
    private Long total;

    // ========== 辅助方法 ==========

    /**
     * 构建游标分页响应
     *
     * @param data       数据列表
     * @param nextCursor 下一页游标
     * @param hasMore    是否有更多
     * @param pageSize   每页大小
     */
    public static <T> CursorPageResponse<T> of(List<T> data, String nextCursor, boolean hasMore, int pageSize) {
        return CursorPageResponse.<T>builder()
                .data(data != null ? data : Collections.emptyList())
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .pageSize(pageSize)
                .build();
    }

    /**
     * 构建游标分页响应（带总数）
     */
    public static <T> CursorPageResponse<T> of(List<T> data, String nextCursor, boolean hasMore, int pageSize, Long total) {
        return CursorPageResponse.<T>builder()
                .data(data != null ? data : Collections.emptyList())
                .nextCursor(nextCursor)
                .hasMore(hasMore)
                .pageSize(pageSize)
                .total(total)
                .build();
    }

    /**
     * 返回空的游标分页响应
     */
    public static <T> CursorPageResponse<T> empty(int pageSize) {
        return CursorPageResponse.<T>builder()
                .data(Collections.emptyList())
                .nextCursor(null)
                .hasMore(false)
                .pageSize(pageSize)
                .total(0L)
                .build();
    }
}
