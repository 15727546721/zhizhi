package cn.xu.domain.message.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 首次消息记录实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirstMessageEntity {
    /**
     * 记录ID
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
     * 消息ID
     */
    private Long messageId;
    
    /**
     * 是否已回复
     */
    private Boolean hasReplied;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 标记为已回复
     */
    public void markAsReplied() {
        this.hasReplied = true;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 判断是否未回复
     */
    public boolean isNotReplied() {
        return !hasReplied;
    }
    
    /**
     * 创建首次消息记录
     */
    public static FirstMessageEntity create(Long senderId, Long receiverId, Long messageId) {
        validateFirstMessage(senderId, receiverId, messageId);
        LocalDateTime now = LocalDateTime.now();
        return FirstMessageEntity.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .messageId(messageId)
                .hasReplied(false)
                .createTime(now)
                .updateTime(now)
                .build();
    }
    
    /**
     * 验证首次消息
     */
    private static void validateFirstMessage(Long senderId, Long receiverId, Long messageId) {
        if (senderId == null || senderId <= 0) {
            throw new IllegalArgumentException("Sender ID cannot be null or zero");
        }
        if (receiverId == null || receiverId <= 0) {
            throw new IllegalArgumentException("Receiver ID cannot be null or zero");
        }
        if (senderId.equals(receiverId)) {
            throw new IllegalArgumentException("Sender and receiver cannot be the same");
        }
        if (messageId == null || messageId <= 0) {
            throw new IllegalArgumentException("Message ID cannot be null or zero");
        }
    }
}

