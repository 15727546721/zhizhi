package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 打招呼消息记录实体
 * 
 * <p>设计说明：
 * <ul>
 *   <li>用于记录单向关注场景下的"敲门消息"</li>
 *   <li>A关注B后，A只能发送1条打招呼消息</li>
 *   <li>B回复后，或A与B互关后，可自由发送</li>
 * </ul>
 * 
 * <p>对应数据库表：greeting_record
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GreetingRecord implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 记录ID */
    private Long id;
    
    /** 发送者ID（关注方） */
    private Long userId;
    
    /** 接收者ID（被关注方） */
    private Long targetId;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    // ========== 工厂方法 ==========
    
    /**
     * 创建打招呼记录
     *
     * @param userId 发送者ID
     * @param targetId 接收者ID
     * @return 打招呼记录
     */
    public static GreetingRecord create(Long userId, Long targetId) {
        return GreetingRecord.builder()
                .userId(userId)
                .targetId(targetId)
                .createTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 获取简单信息（用于日志）
     */
    public String getSimpleInfo() {
        return String.format("GreetingRecord[userId=%d, targetId=%d]", userId, targetId);
    }
}