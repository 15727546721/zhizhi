package cn.xu.api.web.model.vo.post;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 帖子搜索响应VO
 * 用于搜索接口返回，将领域对象转换为前端友好的扁平化格式
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "帖子搜索响应数据")
public class PostSearchResponse {
    
    @Schema(description = "帖子ID")
    private Long id;
    
    @Schema(description = "帖子类型")
    private String type;
    
    @Schema(description = "标题")
    private String title;
    
    @Schema(description = "描述")
    private String description;
    
    @Schema(description = "内容（摘要）")
    private String content;
    
    @Schema(description = "封面图片")
    private String coverUrl;
    
    @Schema(description = "作者ID")
    private Long userId;
    
    @Schema(description = "作者昵称")
    private String authorName;
    
    @Schema(description = "作者头像")
    private String avatar;
    
    @Schema(description = "分类ID")
    private Long categoryId;
    
    @Schema(description = "浏览量")
    private Long viewCount;
    
    @Schema(description = "点赞数")
    private Long likeCount;
    
    @Schema(description = "评论数")
    private Long commentCount;
    
    @Schema(description = "收藏数")
    private Long favoriteCount;
    
    @Schema(description = "分享数")
    private Long shareCount;
    
    @Schema(description = "是否加精")
    private Boolean isFeatured;
    
    @Schema(description = "创建时间")
    private LocalDateTime createTime;
    
    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
    
    @Schema(description = "发布时间")
    private LocalDateTime publishTime;
}

