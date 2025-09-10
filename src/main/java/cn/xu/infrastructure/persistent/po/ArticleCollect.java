package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 文章收藏表
 * @TableName article_collect
 */
@Data
public class ArticleCollect implements Serializable {
    /**
     * 收藏文章ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 文章ID
     */
    private Long articleId;

    /**
     * 收藏状态：1-收藏，0-未收藏
     */
    private Integer status;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

}