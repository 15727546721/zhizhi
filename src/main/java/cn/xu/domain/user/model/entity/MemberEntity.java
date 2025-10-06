package cn.xu.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 会员领域实体
 * 存储用户当前积分余额和等级
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberEntity {
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
    
    // 状态常量
    public static final Integer STATUS_NORMAL = 1;
    public static final Integer STATUS_FROZEN = 2;
    
    /**
     * 创建新会员记录
     */
    public static MemberEntity createNewMember(Long userId) {
        return MemberEntity.builder()
                .userId(userId)
                .currentPoints(0L)
                .totalEarnedPoints(0L)
                .level(1)
                .levelName("新手")
                .currentExp(0L)
                .nextLevelExp(100L)
                .status(STATUS_NORMAL)
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
        
        this.currentPoints = (this.currentPoints == null ? 0L : this.currentPoints) + points;
        this.totalEarnedPoints = (this.totalEarnedPoints == null ? 0L : this.totalEarnedPoints) + points;
        this.lastEarnedAt = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 消费积分
     */
    public void consumePoints(Long points) {
        if (points <= 0) {
            throw new IllegalArgumentException("消费积分必须大于0");
        }
        
        Long currentAvailable = this.currentPoints == null ? 0L : this.currentPoints;
        if (currentAvailable < points) {
            throw new IllegalStateException("可用积分不足，当前可用积分：" + currentAvailable);
        }
        
        this.currentPoints = currentAvailable - points;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 增加经验值并检查升级
     */
    public void addExpAndCheckLevelUp(Long exp) {
        if (exp <= 0) {
            throw new IllegalArgumentException("经验值必须大于0");
        }
        
        this.currentExp = (this.currentExp == null ? 0L : this.currentExp) + exp;
        this.updateTime = LocalDateTime.now();
        
        // 检查是否升级
        checkLevelUp();
    }
    
    /**
     * 检查是否升级
     */
    private void checkLevelUp() {
        while (this.currentExp >= this.nextLevelExp) {
            levelUp();
        }
    }
    
    /**
     * 升级
     */
    private void levelUp() {
        this.level = (this.level == null ? 1 : this.level) + 1;
        this.currentExp = this.currentExp - this.nextLevelExp;
        this.nextLevelExp = calculateNextLevelExp(this.level);
        this.levelName = getLevelNameByLevel(this.level);
        this.levelUpdatedAt = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 计算下一级所需经验值
     */
    private Long calculateNextLevelExp(Integer level) {
        // 简单的升级公式：每级所需经验 = 当前等级 * 100
        return (level == null ? 1 : level) * 100L;
    }
    
    /**
     * 根据等级获取等级名称
     */
    private String getLevelNameByLevel(Integer level) {
        if (level == null) return "新手";
        
        switch (level) {
            case 1: return "新手";
            case 2: return "入门";
            case 3: return "熟练";
            case 4: return "专家";
            case 5: return "大师";
            default: return "宗师";
        }
    }
    
    /**
     * 冻结账户
     */
    public void freeze() {
        this.status = STATUS_FROZEN;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 解冻账户
     */
    public void unfreeze() {
        this.status = STATUS_NORMAL;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 获取当前积分
     */
    public Long getCurrentPoints() {
        return currentPoints == null ? 0L : currentPoints;
    }
    
    /**
     * 获取历史总获得积分
     */
    public Long getTotalEarnedPoints() {
        return totalEarnedPoints == null ? 0L : totalEarnedPoints;
    }
    
    /**
     * 获取当前等级
     */
    public Integer getLevel() {
        return level == null ? 1 : level;
    }
    
    /**
     * 获取当前经验值
     */
    public Long getCurrentExp() {
        return currentExp == null ? 0L : currentExp;
    }
    
    /**
     * 获取下一级所需经验值
     */
    public Long getNextLevelExp() {
        return nextLevelExp == null ? 100L : nextLevelExp;
    }
    
    /**
     * 获取等级名称
     */
    public String getLevelName() {
        return levelName == null ? "新手" : levelName;
    }
}