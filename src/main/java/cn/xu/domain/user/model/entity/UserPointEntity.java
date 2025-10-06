package cn.xu.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户积分领域实体
 * 封装用户积分相关的业务逻辑和规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointEntity {
    private Long id;
    private Long userId;
    private Long totalPoints; // 总积分
    private Long availablePoints; // 可用积分
    private Long consumedPoints; // 已消费积分
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建新用户积分记录
     */
    public static UserPointEntity createNewUserPoint(Long userId) {
        return UserPointEntity.builder()
                .userId(userId)
                .totalPoints(0L)
                .availablePoints(0L)
                .consumedPoints(0L)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 增加积分
     */
    public void addPoints(Long points) {
        if (points <= 0) {
            throw new IllegalArgumentException("积分必须大于0");
        }
        
        this.totalPoints = (this.totalPoints == null ? 0L : this.totalPoints) + points;
        this.availablePoints = (this.availablePoints == null ? 0L : this.availablePoints) + points;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 消费积分
     */
    public void consumePoints(Long points) {
        if (points <= 0) {
            throw new IllegalArgumentException("消费积分必须大于0");
        }
        
        Long currentAvailable = this.availablePoints == null ? 0L : this.availablePoints;
        if (currentAvailable < points) {
            throw new IllegalStateException("可用积分不足，当前可用积分：" + currentAvailable);
        }
        
        this.availablePoints = currentAvailable - points;
        this.consumedPoints = (this.consumedPoints == null ? 0L : this.consumedPoints) + points;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 获取总积分
     */
    public Long getTotalPoints() {
        return totalPoints == null ? 0L : totalPoints;
    }
    
    /**
     * 获取可用积分
     */
    public Long getAvailablePoints() {
        return availablePoints == null ? 0L : availablePoints;
    }
    
    /**
     * 获取已消费积分
     */
    public Long getConsumedPoints() {
        return consumedPoints == null ? 0L : consumedPoints;
    }
}