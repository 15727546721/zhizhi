package cn.xu.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户点赞列表项VO
 * 包含点赞信息和被点赞的目标信息
 * 
 * @author zhizhi
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "用户点赞列表项VO")
public class UserLikeItemVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "点赞ID")
    private Long likeId;
    
    @Schema(description = "被点赞的目标ID（帖子ID、评论ID等）")
    private Long targetId;
    
    @Schema(description = "被点赞的目标标题")
    private String targetTitle;
    
    @Schema(description = "被点赞的目标类型（1-帖子，2-随笔，3-评论）")
    private Integer targetType;
    
    @Schema(description = "目标类型名称")
    private String targetTypeName;
    
    @Schema(description = "目标链接")
    private String targetUrl;
    
    @Schema(description = "点赞时间")
    private LocalDateTime likeTime;
}

