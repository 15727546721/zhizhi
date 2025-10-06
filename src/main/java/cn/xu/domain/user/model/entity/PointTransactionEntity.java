package cn.xu.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 积分流水领域实体
 * 记录每一次积分变动，方便审计和回滚
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PointTransactionEntity {
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
    
    // 状态常量
    public static final Integer STATUS_SUCCESS = 1;
    public static final Integer STATUS_FAILED = 2;
    public static final Integer STATUS_COMPENSATING = 3;
    
    /**
     * 创建积分增加记录
     */
    public static PointTransactionEntity createAddTransaction(Long userId, Long changeAmount, 
            String changeType, String description, Long balanceAfter) {
        return PointTransactionEntity.builder()
                .userId(userId)
                .changeAmount(changeAmount)
                .changeType(changeType)
                .description(description)
                .balanceAfter(balanceAfter)
                .status(STATUS_SUCCESS)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建积分消费记录
     */
    public static PointTransactionEntity createConsumeTransaction(Long userId, Long changeAmount, 
            String changeType, String description, Long balanceAfter) {
        return PointTransactionEntity.builder()
                .userId(userId)
                .changeAmount(-changeAmount)
                .changeType(changeType)
                .description(description)
                .balanceAfter(balanceAfter)
                .status(STATUS_SUCCESS)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
}