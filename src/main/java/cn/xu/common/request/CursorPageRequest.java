package cn.xu.common.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 游标分页请求参数
 * <p>
 * 游标分页原理：使用上一页最后一条记录的排序字段值作为游标，
 * 查询时使用 WHERE 条件过滤，避免 OFFSET 带来的性能问题。
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "游标分页请求参数")
public class CursorPageRequest {

    @Schema(description = "游标值（首次请求为空，后续请求传入上一页返回的 nextCursor）")
    private String cursor;

    @Schema(description = "每页记录数", example = "10")
    private Integer pageSize = 10;

    @Schema(description = "排序方式：latest(最新), hot(热门)", example = "latest")
    private String sortBy = "latest";

    @Schema(description = "标签ID筛选")
    private Long tagId;

    /**
     * 获取安全的页面大小（限制在 1-100 之间）
     */
    public int getSafePageSize() {
        if (pageSize == null || pageSize < 1) {
            return 10;
        }
        return Math.min(pageSize, 100);
    }

    /**
     * 解析游标为 Long 类型（用于 ID 游标）
     */
    public Long getCursorAsLong() {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(cursor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析游标为 Double 类型（用于热度分数游标）
     */
    public Double getCursorAsDouble() {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(cursor);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 解析复合游标（格式：score_id）
     * 用于热门排序时同时使用分数和ID作为游标
     */
    public CursorPair getCursorPair() {
        if (cursor == null || cursor.isEmpty()) {
            return null;
        }
        String[] parts = cursor.split("_");
        if (parts.length != 2) {
            return null;
        }
        try {
            double score = Double.parseDouble(parts[0]);
            long id = Long.parseLong(parts[1]);
            return new CursorPair(score, id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    /**
     * 复合游标对
     */
    @Data
    public static class CursorPair {
        private final double score;
        private final long id;

        public CursorPair(double score, long id) {
            this.score = score;
            this.id = id;
        }
    }
}
