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
    private Integer id;

    /**
     * 文章的标题
     */
    private String title;

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
    private Integer authorId;

    /**
     * 文章的创建时间
     */
    private Date createdTime;

    /**
     * 文章的最后更新时间
     */
    private Date updatedTime;

    /**
     * 逻辑删除标志，0表示未删除，1表示已删除
     */
    private Integer deleted;

    /**
     * 文章的状态：草稿、发布、下架
     */
    private Object status;

    /**
     * 是否允许评论，0表示不允许，1表示允许
     */
    private Integer commentsEnabled;

    /**
     * 是否置顶，0表示未置顶，1表示已置顶
     */
    private Integer isTop;

    /**
     * 文章分类的ID
     */
    private Integer categoryId;

    /**
     * 文章的阅读次数
     */
    private Integer viewCount;

    private static final long serialVersionUID = 1L;

}