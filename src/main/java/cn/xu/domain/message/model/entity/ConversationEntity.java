package cn.xu.domain.message.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 对话关系实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationEntity {
    /**
     * 对话关系ID
     */
    private Long id;
    
    /**
     * 用户1 ID（较小的ID）
     */
    private Long userId1;
    
    /**
     * 用户2 ID（较大的ID）
     */
    private Long userId2;
    
    /**
     * 创建者ID（首次发送消息的用户）
     */
    private Long createdBy;
    
    /**
     * 最后一条消息时间
     */
    private LocalDateTime lastMessageTime;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 判断用户是否参与对话
     */
    public boolean isParticipant(Long userId) {
        return userId.equals(userId1) || userId.equals(userId2);
    }
    
    /**
     * 获取对话的另一方用户ID
     */
    public Long getOtherParticipant(Long userId) {
        if (userId.equals(userId1)) {
            return userId2;
        } else if (userId.equals(userId2)) {
            return userId1;
        }
        throw new IllegalArgumentException("User is not a participant of this conversation");
    }
    
    /**
     * 更新最后消息时间
     */
    public void updateLastMessageTime() {
        this.lastMessageTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
}

