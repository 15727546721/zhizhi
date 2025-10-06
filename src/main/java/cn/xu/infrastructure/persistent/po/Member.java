package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会员持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member {
    private Long id;
    private Long userId;
    private Long currentPoints; // 当前积分余额
    private Long totalEarnedPoints; // 历史总获得积分
    private Integer level; // 会员等级
    private String levelName; // 等级名称
    private Long currentExp; // 当前经验值
    private Long nextLevelExp; // 下一级所需经验值
    private Integer status; // 状态：1-正常，2-冻结
    private LocalDateTime levelUpdatedAt; // 等级更新时间
    private LocalDateTime lastEarnedAt; // 最后获得积分时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}