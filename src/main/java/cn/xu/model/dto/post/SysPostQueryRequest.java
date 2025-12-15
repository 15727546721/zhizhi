package cn.xu.model.dto.post;

import cn.xu.common.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

/**
 * 帖子查询请求参数
 */
@Data
@Schema(description = "帖子查询请求参数")
public class SysPostQueryRequest extends PageRequest {

    /**
     * 帖子标题
     */
    @Schema(description = "帖子标题")
    private String title;

    /**
     * 标签ID
     */
    @Schema(description = "标签ID")
    private Long tagId;

    /**
     * 帖子状态：0-草稿，1-发布中，2-已归档
     */
    @Schema(description = "帖子状态：0-草稿，1-发布中，2-已归档")
    private Integer status;

    /**
     * 用户ID
     */
    @Schema(description = "用户ID")
    private Long userId;

    /**
     * 排序方式
     * newest: 最新
     * hottest: 最热
     * most_commented: 评论最多
     * most_bookmarked: 被收藏最多
     * most_liked: 被点赞最多
     * popular: 综合热度
     */
    @Schema(description = "排序方式")
    private String sortBy = "hottest";

    /**
     * 偏移量
     */
    @Schema(description = "偏移量")
    private Integer offset;
}
