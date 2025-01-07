package cn.xu.domain.article.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 文章详情DTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {
    /**
     * 文章ID
     */
    private Long id;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 作者昵称
     */
    private String authorName;

    /**
     * 作者头像
     */
    private String authorAvatar;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 描述
     */
    private String description;

    /**
     * 封面URL
     */
    private String coverUrl;

    /**
     * 浏览量
     */
    private Long viewCount;

    /**
     * 点赞数
     */
    private Long likeCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 