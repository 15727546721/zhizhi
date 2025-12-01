package cn.xu.controller.admin.model.dto.post;

import cn.xu.common.request.PageRequest;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "系统帖子查询请求参数")
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
     * 帖子状态: 0-草稿 1-已发布 2-已删除
     */
    @Schema(description = "帖子状态: 0-草稿 1-已发布 2-已删除")
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
     * most_commented: 最多评论
     * most_bookmarked: 最多收藏
     * most_liked: 最多点赞
     * popular: 最受欢迎（浏览量）
     */
    @Schema(description = "排序方式")
    private String sortBy = "hottest";
    
    /**
     * 偏移量
     */
    @Schema(description = "偏移量")
    private Integer offset;
}