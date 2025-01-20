package cn.xu.api.web.model.dto.topic;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "话题DTO")
public class TopicDTO {

    @Schema(description = "话题ID")
    private Long id;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "话题内容")
    private String content;

    @Schema(description = "话题图片URL列表")
    private List<String> images;

    @Schema(description = "话题分类ID")
    private Long categoryId;

    @Schema(description = "话题分类名称")
    private String categoryName;

    @Schema(description = "浏览数")
    private Integer viewCount;

    @Schema(description = "点赞数")
    private Integer likeCount;

    @Schema(description = "评论数")
    private Integer commentCount;

    @Schema(description = "收藏数")
    private Integer collectCount;

    @Schema(description = "状态（0：草稿，1：已发布，2：已下架）")
    private Integer status;

    @Schema(description = "是否置顶")
    private Boolean isTop;

    @Schema(description = "是否精华")
    private Boolean isEssence;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
} 