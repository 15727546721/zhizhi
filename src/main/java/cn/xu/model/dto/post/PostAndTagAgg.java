package cn.xu.model.dto.post;

import lombok.Data;

/**
 * 帖子与标签聚合信息
 * <p>用于展示一个帖子与其相关标签的聚合信息</p>
 
 */
@Data
public class PostAndTagAgg {
    
    /**
     * 帖子ID
     */
    private Long postId;
    
    /**
     * 标签信息（逗号分隔）
     */
    private String tags;
}