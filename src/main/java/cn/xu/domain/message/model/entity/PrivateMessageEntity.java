package cn.xu.domain.message.model.entity;

import cn.xu.domain.message.model.valueobject.MessageStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 私信实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessageEntity {
    /**
     * 消息ID
     */
    private Long id;
    
    /**
     * 发送者ID
     */
    private Long senderId;
    
    /**
     * 接收者ID
     */
    private Long receiverId;
    
    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 是否已读
     */
    private Boolean isRead;
    
    /**
     * 消息状态
     */
    private MessageStatus status;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = true;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 判断是否未读
     */
    public boolean isUnread() {
        return !isRead;
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
     * 判断是否为正常送达
     */
    public boolean isDelivered() {
        return status != null && status.isDelivered();
    }
    
    /**
     * 判断是否为未送达
     */
    public boolean isPending() {
        return status != null && status.isPending();
    }
}

