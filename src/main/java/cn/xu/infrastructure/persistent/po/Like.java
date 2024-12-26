package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName like
 */
@Data
public class Like implements Serializable {
    /**
     * 点赞表主键
     */
    private Long id;

    /**
     * 用户id
     */
    private Long userId;

    /**
     * 目标id
     */
    private Long targetId;

    /**
     * 点赞类型：1-文章，2-帖子，3-评论等
     */
    private Integer type;

    /**
     * 点赞时间
     */
    private Date createdTime;

    /**
     * 更新点赞时间
     */
    private Date updatedTime;

    /**
     * 是否点赞，1-点赞，0-取消点赞
     */
    private Integer value;

}