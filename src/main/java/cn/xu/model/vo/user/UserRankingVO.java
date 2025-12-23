package cn.xu.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * 用户排行榜VO
 * <p>用于返回用户排行榜信息</p>
 
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户排行榜VO")
public class UserRankingVO implements Serializable {
    
    @Schema(description = "用户ID")
    private Long id;
    
    @Schema(description = "用户名")
    private String username;
    
    @Schema(description = "昵称")
    private String nickname;
    
    @Schema(description = "头像")
    private String avatar;
    
    @Schema(description = "个人简介")
    private String description;
    
    @Schema(description = "粉丝数")
    private Long fansCount;
    
    @Schema(description = "关注数")
    private Long followCount;
    
    @Schema(description = "点赞数")
    private Long likeCount;
    
    @Schema(description = "帖子数")
    private Long postCount;
    
    @Schema(description = "排名")
    private Integer rank;
    
    @Schema(description = "排名变化")
    private Integer rankChange;
}