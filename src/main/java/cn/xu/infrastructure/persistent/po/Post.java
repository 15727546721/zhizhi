package cn.xu.infrastructure.persistent.po;


import lombok.*;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子表
 *
 * @TableName post
 */
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Post implements Serializable {
    /**
     * 帖子ID
     */
    private Long id;

    /**
     * 帖子标题
     */
    private String title;

    /**
     * 帖子简介
     */
    private String description;

    /**
     * 帖子内容
     */
    private String content;

    /**
     * 帖子封面图片URL
     */
    private String coverUrl;

    /**
     * 作者ID
     */
    private Long userId;

    /**
     * 分类ID
     */
    private Long categoryId;

    /**
     * 帖子类型
     */
    private String type;

    /**
     * 是否加精：0-否，1-是
     */
    private Integer isFeatured;

    /**
     * 被采纳的回答ID（仅用于问答帖）
     */
    private Long acceptedAnswerId;

    /**
     * 阅读次数
     */
    private Long viewCount;

    /**
     * 收藏次数
     */
    private Long collectCount;

    /**
     * 评论次数
     */
    private Long commentCount;

    /**
     * 点赞次数
     */
    private Long likeCount;
    
    /**
     * 分享次数
     */
    private Long shareCount;

    /**
     * 帖子状态：0-草稿，1-已发布，2-已删除，3-已归档
     */
    private Integer status;

    /**
     * 发布时间
     */
    private LocalDateTime publishTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 帖子更新时间
     */
    private LocalDateTime updateTime;

    private static final long serialVersionUID = 1L;
}