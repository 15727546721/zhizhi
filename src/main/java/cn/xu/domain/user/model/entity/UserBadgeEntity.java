package cn.xu.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户勋章领域实体
 * 封装用户勋章相关的业务逻辑和规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserBadgeEntity {
    private Long id;
    private Long userId;
    private Long badgeId; // 勋章ID
    private String badgeName; // 勋章名称
    private String badgeDescription; // 勋章描述
    private String badgeIcon; // 勋章图标
    private Integer status; // 状态：0-未获得，1-已获得，2-已失效
    private LocalDateTime obtainTime; // 获得时间
    private LocalDateTime expireTime; // 过期时间
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建新用户勋章记录
     */
    public static UserBadgeEntity createNewUserBadge(Long userId, Long badgeId, String badgeName, String badgeDescription, String badgeIcon) {
        return UserBadgeEntity.builder()
                .userId(userId)
                .badgeId(badgeId)
                .badgeName(badgeName)
                .badgeDescription(badgeDescription)
                .badgeIcon(badgeIcon)
                .status(0) // 初始状态为未获得
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 获得勋章
     */
    public void obtainBadge() {
        this.status = 1; // 已获得
        this.obtainTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 使勋章失效
     */
    public void invalidateBadge() {
        this.status = 2; // 已失效
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 设置过期时间
     */
    public void setExpireTime(LocalDateTime expireTime) {
        this.expireTime = expireTime;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 判断勋章是否已获得
     */
    public boolean isObtained() {
        return this.status != null && this.status == 1;
    }
    
    /**
     * 判断勋章是否已失效
     */
    public boolean isInvalidated() {
        return this.status != null && this.status == 2;
    }
    
    /**
     * 判断勋章是否已过期
     */
    public boolean isExpired() {
        if (this.expireTime == null) {
            return false;
        }
        return LocalDateTime.now().isAfter(this.expireTime);
    }
    
    /**
     * 判断勋章是否有效（已获得且未失效未过期）
     */
    public boolean isValid() {
        return isObtained() && !isInvalidated() && !isExpired();
    }
}