package cn.xu.infrastructure.persistent.po;

import java.io.Serializable;
import java.util.Date;

import lombok.Builder;
import lombok.Data;

/**
 * 评论图片表
 * @TableName comment_image
 */
@Data
@Builder
public class CommentImage implements Serializable {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 关联的评论ID
     */
    private Long commentId;

    /**
     * 图片URL地址
     */
    private String imageUrl;

    /**
     * 排序字段，越小越靠前
     */
    private Integer sortOrder;

    /**
     * 上传时间
     */
    private Date createTime;
}