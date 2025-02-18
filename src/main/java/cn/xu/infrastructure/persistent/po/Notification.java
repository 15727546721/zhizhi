package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    /**
     * 主键ID
     */
    private Long id;

    /**
     * 通知类型：0-系统通知 1-点赞通知 2-收藏通知 3-评论通知 4-回复通知 5-关注通知
     */
    private Integer type;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 标题
     */
    private String title;

    /**
     * 内容
     */
    private String content;

    /**
     * 业务类型：0-系统 1-文章 2-话题 3-用户
     */
    private Integer notificationBusinessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer isRead;

    /**
     * 状态：1-有效 0-已删除
     */
    private Integer status;

    /**
     * 阅读时间
     */
    private LocalDateTime readTime;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
} 