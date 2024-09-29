package cn.xu.infrastructure.persistent.po;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;
import java.util.Date;

/**
 * 存储文章信息的表
 * @TableName article
 */
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
    private String description;

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
    private Long authorId;

    /**
     * 文章分类的ID
     */
    private Long categoryId;

    /**
     * 逻辑删除标志，0表示未删除，1表示已删除
     */
    private String deleted;

    /**
     * 文章的状态：0草稿、1发布、2下架、3待审核、4审核不通过
     */
    private String status;

    /**
     * 是否允许评论，0表示不允许，1表示允许
     */
    private String commentsEnabled;

    /**
     * 是否置顶，0表示未置顶，1表示已置顶
     */
    private String isTop;

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
    private Date createTime;

    /**
     * 文章的最后更新时间
     */
    private Date updateTime;

    private static final long serialVersionUID = 1L;

}