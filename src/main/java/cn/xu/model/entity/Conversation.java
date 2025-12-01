package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信会话实体
 *
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation implements Serializable {
    
    // ========== 数据库字段 ==========
    
    /** 对话关系ID */
    private Long id;
    
    /** 用户1 ID（较小的ID） */
    private Long userId1;
    
    /** 用户2 ID（较大的ID） */
    private Long userId2;
    
    /** 发起者ID（首次发送消息的用户） */
    private Long initiatorId;
    
    /** 对话状态：0-待回复（防骚扰） 1-已建立对话 */
    private Integer status;
    
    /** 最后一条消息时间 */
    private LocalDateTime lastMessageTime;
    
    /** 最后消息预览 */
    private String lastMessageContent;
    
    /** 用户1未读数 */
    private Integer unreadCount1;
    
    /** 用户2未读数 */
    private Integer unreadCount2;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ========== 状态常量 ==========
    
    /** 对话状态：待回复（防骚扰机制） */
    public static final int STATUS_PENDING = 0;
    
    /** 对话状态：已建立对话 */
    public static final int STATUS_ESTABLISHED = 1;
    
    // ========== 业务方法 ==========
    
    /**
     * 创建新对话（待回复状态）
     */
    public static Conversation createPending(Long userId1, Long userId2, Long initiatorId) {
        return Conversation.builder()
                .userId1(Math.min(userId1, userId2))
                .userId2(Math.max(userId1, userId2))
                .initiatorId(initiatorId)
                .status(STATUS_PENDING)
                .lastMessageTime(LocalDateTime.now())
                .unreadCount1(0)
                .unreadCount2(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建已建立的对话（互相关注用户）
     */
    public static Conversation createEstablished(Long userId1, Long userId2, Long initiatorId) {
        return Conversation.builder()
                .userId1(Math.min(userId1, userId2))
                .userId2(Math.max(userId1, userId2))
                .initiatorId(initiatorId)
                .status(STATUS_ESTABLISHED)
                .lastMessageTime(LocalDateTime.now())
                .unreadCount1(0)
                .unreadCount2(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 判断用户是否参与对话
     */
    public boolean isParticipant(Long userId) {
        return userId != null && (userId.equals(userId1) || userId.equals(userId2));
    }
    
    /**
     * 获取对话的另一方用户ID
     */
    public Long getOtherUserId(Long currentUserId) {
        if (currentUserId == null) return null;
        if (currentUserId.equals(userId1)) return userId2;
        if (currentUserId.equals(userId2)) return userId1;
        return null;
    }
    
    /**
     * 是否是待回复状态
     */
    public boolean isPending() {
        return status != null && status == STATUS_PENDING;
    }
    
    /**
     * 是否已建立对话
     */
    public boolean isEstablished() {
        return status != null && status == STATUS_ESTABLISHED;
    }
    
    /**
     * 判断用户是否是发起者
     */
    public boolean isInitiator(Long userId) {
        return userId != null && userId.equals(initiatorId);
    }
    
    /**
     * 建立对话（从待回复变为已建立）
     */
    public void establish() {
        this.status = STATUS_ESTABLISHED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 更新最后消息时间
     */
    public void updateLastMessageTime() {
        this.lastMessageTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
}

