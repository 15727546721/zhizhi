package cn.xu.model.vo.column;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专栏文章视图对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnPostVO implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 帖子ID */
    private Long postId;
    
    /** 标题 */
    private String title;
    
    /** 描述 */
    private String description;
    
    /** 封面图URL */
    private String coverUrl;
    
    /** 浏览数 */
    private Integer viewCount;
    
    /** 点赞数 */
    private Integer likeCount;
    
    /** 评论数 */
    private Integer commentCount;
    
    /** 排序值 */
    private Integer sort;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    // ==================== 导航信息 ====================
    
    /** 上一篇帖子ID */
    private Long previousPostId;
    
    /** 上一篇帖子标题 */
    private String previousPostTitle;
    
    /** 下一篇帖子ID */
    private Long nextPostId;
    
    /** 下一篇帖子标题 */
    private String nextPostTitle;
}
