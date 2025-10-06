package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 积分流水持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransaction {
    private Long id;
    private Long userId;
    private Long changeAmount; // 积分变动数量，正负数
    private String changeType; // 变动类型：签到、下单、退款、手动调整等
    private Long orderId; // 关联订单ID，可空
    private String description; // 变动描述
    private Long balanceAfter; // 变动后余额
    private Integer status; // 状态：1-成功，2-失败，3-补偿中
    private Long relatedId; // 关联ID（如评论ID、文章ID等）
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}