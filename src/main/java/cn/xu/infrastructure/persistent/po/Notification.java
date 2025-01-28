package cn.xu.infrastructure.persistent.po;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 通知表持久化对象
 *
 * @author xuhh
 * @date 2024/03/20
 */
@Data
public class Notification {
    
    /**
     * 通知ID
     */
    private Long id;
    
    /**
     * 通知类型：1-系统通知 2-点赞通知 3-收藏通知 4-评论通知 5-关注通知 6-回复通知
     */
    private Integer type;
    
    /**
     * 接收用户ID
     */
    private Long receiverId;
    
    /**
     * 发送者ID（系统通知时为空）
     */
    private Long senderId;
    
    /**
     * 发送者类型：1-系统 2-用户
     */
    private Integer senderType;
    
    /**
     * 通知标题
     */
    private String title;
    
    /**
     * 通知内容
     */
    private String content;
    
    /**
     * 业务类型：1-文章 2-评论 3-用户
     */
    private Integer businessType;
    
    /**
     * 业务ID
     */
    private Long businessId;
    
    /**
     * 额外参数JSON格式
     */
    private String extra;
    
    /**
     * 状态：1-有效 2-已删除
     */
    private Integer status;
    
    /**
     * 是否已读：0-未读 1-已读
     */
    private Boolean isRead;
    
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