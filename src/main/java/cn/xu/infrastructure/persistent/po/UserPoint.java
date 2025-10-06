package cn.xu.infrastructure.persistent.po;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户积分持久化对象
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPoint {
    private Long id;
    private Long userId;
    private Long totalPoints; // 总积分
    private Long availablePoints; // 可用积分
    private Long consumedPoints; // 已消费积分
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}