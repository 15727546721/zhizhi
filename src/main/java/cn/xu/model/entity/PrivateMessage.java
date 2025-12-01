package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息实体
 * 
 * <p>对应数据库表：private_message
 * 
 * @author xu
 * @since 2025-11-28
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ========== 消息状态常量 ==========
    
    /** 已送达（对方可见） */
    public static final int STATUS_DELIVERED = 1;
    
    /** 待回复（仅发送方可见，防骚扰） */
    public static final int STATUS_PENDING = 2;
    
    /** 被屏蔽（仅发送方可见） */
    public static final int STATUS_BLOCKED = 3;
    
    // ========== 数据库字段 ==========
    
    /** 消息ID */
    private Long id;
    
    /** 发送者ID */
    private Long senderId;
    
    /** 接收者ID */
    private Long receiverId;
    
    /** 消息内容 */
    private String content;
    
    /** 是否已读：0-未读 1-已读 */
    private Integer isRead;
    
    /** 消息状态：1-已送达 2-待回复 3-被屏蔽 */
    private Integer status;
    
    /** 阅读时间 */
    private LocalDateTime readTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    // ========== 运行时字段（不存数据库） ==========
    
    /** 发送者信息 */
    private transient User sender;
    
    /** 接收者信息 */
    private transient User receiver;
    
    // ========== 工厂方法 ==========
    
    /**
     * 创建私信消息
     */
    public static PrivateMessage create(Long senderId, Long receiverId, String content, int status) {
        return PrivateMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .isRead(0)
                .status(status)
                .createTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建已送达的私信
     */
    public static PrivateMessage createDelivered(Long senderId, Long receiverId, String content) {
        return create(senderId, receiverId, content, STATUS_DELIVERED);
    }
    
    /**
     * 创建待回复的私信（防骚扰）
     */
    public static PrivateMessage createPending(Long senderId, Long receiverId, String content) {
        return create(senderId, receiverId, content, STATUS_PENDING);
    }
    
    // ========== 业务方法 ==========
    
    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = 1;
        this.readTime = LocalDateTime.now();
    }
    
    /**
     * 判断是否未读
     */
    public boolean isUnread() {
        return isRead == null || isRead == 0;
    }
    
    /**
     * 判断是否已读
     */
    public boolean hasRead() {
        return isRead != null && isRead == 1;
    }
    
    /**
     * 判断用户是否是消息的参与者
     */
    public boolean isParticipant(Long userId) {
        return userId.equals(senderId) || userId.equals(receiverId);
    }
    
    /**
     * 获取对话的另一方用户ID
     */
    public Long getOtherParticipant(Long userId) {
        return userId.equals(senderId) ? receiverId : senderId;
    }
    
    /**
     * 判断是否为已送达状态
     */
    public boolean isDelivered() {
        return status != null && status == STATUS_DELIVERED;
    }
    
    /**
     * 判断是否为待回复状态
     */
    public boolean isPending() {
        return status != null && status == STATUS_PENDING;
    }
    
    /**
     * 判断是否为被屏蔽状态
     */
    public boolean isBlocked() {
        return status != null && status == STATUS_BLOCKED;
    }
    
    /**
     * 更新状态为已送达
     */
    public void deliver() {
        this.status = STATUS_DELIVERED;
    }
    
    /**
     * 更新状态
     */
    public void updateStatus(int newStatus) {
        this.status = newStatus;
    }
    
    /**
     * 验证消息
     */
    public void validate() {
        if (this.senderId == null) {
            throw new IllegalArgumentException("发送者不能为空");
        }
        if (this.receiverId == null) {
            throw new IllegalArgumentException("接收者不能为空");
        }
        if (this.content == null || this.content.trim().isEmpty()) {
            throw new IllegalArgumentException("消息内容不能为空");
        }
    }
}
