package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 对话关系表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Conversation implements Serializable {
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
}

