package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户屏蔽表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBlock implements Serializable {
    /**
     * 屏蔽关系ID
     */
    private Long id;
    
    /**
     * 用户ID（屏蔽发起者）
     */
    private Long userId;
    
    /**
     * 被屏蔽用户ID
     */
    private Long blockedUserId;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
}

