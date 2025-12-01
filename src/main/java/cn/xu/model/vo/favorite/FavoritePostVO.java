package cn.xu.model.vo.favorite;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏的帖子VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "收藏的帖子信息")
public class FavoritePostVO {
    
    @Schema(description = "帖子ID")
    private Long id;
    
    @Schema(description = "帖子标题")
    private String title;
    
    @Schema(description = "帖子描述")
    private String description;
    
    @Schema(description = "封面图片")
    private String coverUrl;
    
    @Schema(description = "作者ID")
    private Long userId;
    
    @Schema(description = "作者昵称")
    private String nickname;
    
    @Schema(description = "作者头像")
    private String avatar;
    
    @Schema(description = "浏览数")
    private Long viewCount;
    
    @Schema(description = "点赞数")
    private Long likeCount;
    
    @Schema(description = "评论数")
    private Long commentCount;
    
    @Schema(description = "收藏数")
    private Long favoriteCount;
    
    @Schema(description = "收藏时间")
    private LocalDateTime favoriteTime;
    
    @Schema(description = "发布时间")
    private LocalDateTime createTime;
}
