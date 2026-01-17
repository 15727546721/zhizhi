package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏夹实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FavoriteFolder implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 收藏夹名称 */
    private String name;
    
    /** 描述 */
    private String description;
    
    /** 是否公开: 0-私密 1-公开 */
    private Integer isPublic;
    
    /** 是否默认收藏夹: 0-否 1-是 */
    private Integer isDefault;
    
    /** 收藏数量 */
    private Integer itemCount;
    
    /** 封面图URL */
    private String coverUrl;
    
    /** 排序值 */
    private Integer sort;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ==================== 常量 ====================
    
    public static final int PUBLIC_YES = 1;
    public static final int PUBLIC_NO = 0;
    public static final int DEFAULT_YES = 1;
    public static final int DEFAULT_NO = 0;
    
    // ==================== 工厂方法 ====================
    
    /**
     * 创建默认收藏夹
     */
    public static FavoriteFolder createDefault(Long userId) {
        return FavoriteFolder.builder()
                .userId(userId)
                .name("默认收藏夹")
                .isPublic(PUBLIC_NO)
                .isDefault(DEFAULT_YES)
                .itemCount(0)
                .sort(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建普通收藏夹
     */
    public static FavoriteFolder create(Long userId, String name, String description, boolean isPublic) {
        return FavoriteFolder.builder()
                .userId(userId)
                .name(name)
                .description(description)
                .isPublic(isPublic ? PUBLIC_YES : PUBLIC_NO)
                .isDefault(DEFAULT_NO)
                .itemCount(0)
                .sort(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    // ==================== 业务方法 ====================
    
    public boolean isPublicFolder() {
        return PUBLIC_YES == isPublic;
    }
    
    public boolean isDefaultFolder() {
        return DEFAULT_YES == isDefault;
    }
    
    public void incrementItemCount() {
        this.itemCount = (this.itemCount == null ? 0 : this.itemCount) + 1;
        this.updateTime = LocalDateTime.now();
    }
    
    public void decrementItemCount() {
        this.itemCount = Math.max(0, (this.itemCount == null ? 0 : this.itemCount) - 1);
        this.updateTime = LocalDateTime.now();
    }
}
