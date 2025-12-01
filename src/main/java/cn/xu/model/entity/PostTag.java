package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 帖子标签表
 *
 * @TableName post_tag
 */
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class PostTag implements Serializable {
    /**
     * ID
     */
    private Long id;

    /**
     * 帖子ID
     */
    private Long postId;

    /**
     * 标签ID
     */
    private Long tagId;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    private static final long serialVersionUID = 1L;
}