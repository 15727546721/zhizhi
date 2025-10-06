package cn.xu.domain.user.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 用户等级领域实体
 * 封装用户等级相关的业务逻辑和规则
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserLevelEntity {
    private Long id;
    private Long userId;
    private Integer level; // 等级
    private String levelName; // 等级名称
    private Long currentExp; // 当前经验值
    private Long nextLevelExp; // 下一级所需经验值
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建新用户等级记录
     */
    public static UserLevelEntity createNewUserLevel(Long userId) {
        return UserLevelEntity.builder()
                .userId(userId)
                .level(1)
                .levelName("新手")
                .currentExp(0L)
                .nextLevelExp(100L) // 初始升级所需经验值
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 增加经验值
     */
    public void addExp(Long exp) {
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