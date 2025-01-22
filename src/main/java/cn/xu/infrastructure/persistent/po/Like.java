package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 点赞持久化对象
 *
 * @TableName like
 */
@Data
public class Like implements Serializable {
    /**
     * 点赞ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 点赞类型：1-文章，2-话题，3-评论等
     */
    private Integer type;

    /**
     * 是否点赞，1-点赞，0-取消点赞
     */
    private Integer status;

    /**
     * 点赞时间
     */
    private LocalDateTime createTime;

    /**
     * 更新点赞时间
     */
    private LocalDateTime updateTime;
} 