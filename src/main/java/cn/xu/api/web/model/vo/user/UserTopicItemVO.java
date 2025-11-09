package cn.xu.api.web.model.vo.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户话题项视图对象
 * 用于个人主页展示用户参与的话题信息
 * 
 * @author zhizhi
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "用户话题项视图对象")
public class UserTopicItemVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @Schema(description = "话题ID")
    private Long topicId;
    
    @Schema(description = "话题名称")
    private String topicName;
    
    @Schema(description = "话题描述")
    private String topicDescription;
    
    @Schema(description = "参与次数（该用户在该话题下发布的帖子数）")
    private Long postCount;
    
    @Schema(description = "最后参与时间")
    private LocalDateTime lastPostTime;
    
    @Schema(description = "话题使用总数（所有用户在该话题下的帖子总数）")
    private Long totalUsageCount;
}

