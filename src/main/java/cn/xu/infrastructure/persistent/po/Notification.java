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
     * 通知类型：1-系统通知 2-点赞通知 3-收藏通知 4-评论通知  5-回复通知 6-关注通知
     */
    private Integer type;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 发送者类型：1-系统 2-用户
     */
    private Integer senderType;

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
     * 业务类型：1-文章 2-话题 3-用户
     */
    private Integer notificationBusinessType;

    /**
     * 业务ID
     */
    private Long businessId;

    /**
     * 额外信息（JSON格式）
     */
    private Map<String, Object> extraInfo;

    /**
     * 是否已读：0-未读 1-已读
     */
    private Integer read;

    /**
     * 状态：1-有效 0-已删除
     */
    private Integer status;

    /**
     * 创建时间
     */
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    private LocalDateTime updatedTime;
} 