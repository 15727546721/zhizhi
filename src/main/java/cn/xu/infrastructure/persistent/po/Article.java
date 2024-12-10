package cn.xu.infrastructure.persistent.po;


import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 存储文章信息的表
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
     * 文章的唯一标识符
     */
    private Long id;

    /**
     * 文章的标题
     */
    private String title;

    /**
     * 文章的简介
     */
    private String introduction;

    /**
     * 文章的内容
     */
    private String content;

    /**
     * 文章封面图片的URL
     */
    private String coverUrl;

    /**
     * 文章作者的ID
     */
    private Long userId;

    /**
     * 文章的状态：0:草稿、1:发布、2:下架、3:待审核、4:审核不通过
     */
    private String status;

    /**
     * 文章的阅读次数
     */
    private Long viewCount;

    /**
     * 文章的收藏次数
     */
    private Long favoritesCount;

    /**
     * 文章的点赞次数
     */
    private Long likeCount;

    /**
     * 文章的创建时间
     */
    private LocalDateTime createTime;

    /**
     * 文章的最后更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;

}