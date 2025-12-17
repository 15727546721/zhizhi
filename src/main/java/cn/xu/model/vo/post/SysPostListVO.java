package cn.xu.model.vo.post;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 后台帖子列表VO（不包含content字段，优化列表查询性能）
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SysPostListVO {
    
    /**
     * 帖子ID
     */
    private Long id;
    
    /**
     * 作者ID
     */
    private Long userId;
    
    /**
     * 作者昵称
     */
    private String nickname;
    
    /**
     * 标题
     */
    private String title;
    
    /**
     * 描述/摘要
     */
    private String description;
    
    /**
     * 封面图URL
     */
    private String coverUrl;
    
    /**
     * 状态: 0-草稿 1-已发布 2-已删除
     */
    private Integer status;
    
    /**
     * 是否精选: 0-否 1-是
     */
    private Integer isFeatured;
    
    /**
     * 浏览数
     */
    private Long viewCount;
    
    /**
     * 点赞数
     */
    private Long likeCount;
    
    /**
     * 收藏数
     */
    private Long favoriteCount;
    
    /**
     * 评论数
     */
    private Long commentCount;
    
    /**
     * 分享数
     */
    private Long shareCount;
    
    /**
     * 标签名称（逗号分隔）
     */
    private String tagNames;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}
