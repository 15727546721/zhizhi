package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户私信设置表
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserMessageSettings implements Serializable {
    /**
     * 设置ID
     */
    private Long id;
    
    /**
     * 用户ID
     */
    private Long userId;
    
    /**
     * 是否允许陌生人私信：0-不允许 1-允许
     */
    private Integer allowStrangerMessage;
    
    /**
     * 是否允许非互相关注用户私信：0-不允许 1-允许
     */
    private Integer allowNonMutualFollowMessage;
    
    /**
     * 是否开启私信通知：0-关闭 1-开启
     */
    private Integer messageNotificationEnabled;
    
    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
}

