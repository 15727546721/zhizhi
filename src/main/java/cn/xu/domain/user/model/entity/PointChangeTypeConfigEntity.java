package cn.xu.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 积分变动类型配置实体
 * 存储积分变动类型的配置信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointChangeTypeConfigEntity {
    private Long id;
    private String changeType; // 变动类型编码
    private String changeName; // 变动类型名称
    private Integer pointValue; // 默认积分值
    private Integer dailyLimit; // 每日限制次数，-1表示无限制
    private String description; // 描述
    private Integer isActive; // 是否启用：1-启用，0-禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // 状态常量
    public static final Integer ACTIVE = 1;
    public static final Integer INACTIVE = 0;
    public static final Integer NO_LIMIT = -1;
    
    /**
     * 检查是否启用
     */
    public boolean isActive() {
        return isActive != null && isActive.equals(ACTIVE);
    }
    
    /**
     * 检查是否无限制
     */
    public boolean isNoLimit() {
        return dailyLimit != null && dailyLimit.equals(NO_LIMIT);
    }
}