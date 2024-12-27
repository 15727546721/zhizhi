package cn.xu.infrastructure.persistent.po;


import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章表
 *
 * @TableName article
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Article implements Serializable {
    /**
     * 文章ID
     */
    private Long id;

    /**
     * 文章标题
     */
    private String title;

    /**
     * 文章简介
     */
    private String description;

    /**
     * 文章内容
     */
    private String content;

    /**
     * 文章封面图片URL
     */
    private String coverUrl;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 阅读次数
     */
    private Long viewCount;

    /**
     * 收藏次数
     */
    private Long collectCount;

    /**
     * 点赞次数
     */
    private Long likeCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 文更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;

}