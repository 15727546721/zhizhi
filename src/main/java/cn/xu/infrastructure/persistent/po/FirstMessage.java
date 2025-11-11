package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 首次消息记录表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FirstMessage implements Serializable {
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
    private Integer hasReplied;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

