package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 积分变动类型配置持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointChangeTypeConfig {
    private Long id;
    private String changeType; // 变动类型编码
    private String changeName; // 变动类型名称
    private Integer pointValue; // 默认积分值
    private Integer dailyLimit; // 每日限制次数，-1表示无限制
    private String description; // 描述
    private Integer isActive; // 是否启用：1-启用，0-禁用
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}