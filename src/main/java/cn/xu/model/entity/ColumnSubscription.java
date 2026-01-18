package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专栏订阅实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColumnSubscription implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    private Long id;
    
    /** 订阅者ID */
    private Long userId;
    
    /** 专栏ID */
    private Long columnId;
    
    /** 状态: 0-取消 1-订阅 */
    private Integer status;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ==================== 常量 ====================
    
    public static final int STATUS_UNSUBSCRIBED = 0;
    public static final int STATUS_SUBSCRIBED = 1;
    
    // ==================== 工厂方法 ====================
    
    /**
     * 创建订阅
     */
    public static ColumnSubscription create(Long userId, Long columnId) {
        return ColumnSubscription.builder()
                .userId(userId)
                .columnId(columnId)
                .status(STATUS_SUBSCRIBED)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    // ==================== 业务方法 ====================
    
    public boolean isSubscribed() {
        return STATUS_SUBSCRIBED == status;
    }
    
    public void subscribe() {
        this.status = STATUS_SUBSCRIBED;
        this.updateTime = LocalDateTime.now();
    }
    
    public void unsubscribe() {
        this.status = STATUS_UNSUBSCRIBED;
        this.updateTime = LocalDateTime.now();
    }
}
