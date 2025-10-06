package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 点赞持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like {
    /**
     * 主键ID
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
}