package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 专栏实体
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Column implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    /** 主键ID */
    private Long id;
    
    /** 所有者ID */
    private Long userId;
    
    /** 专栏名称 */
    private String name;
    
    /** 描述 */
    private String description;
    
    /** 封面图URL */
    private String coverUrl;
    
    /** 状态: 0-草稿 1-已发布 2-已归档 */
    private Integer status;
    
    /** 文章数 */
    private Integer postCount;
    
    /** 订阅数 */
    private Integer subscribeCount;
    
    /** 是否推荐: 0-否 1-是 */
    private Integer isRecommended;
    
    /** 排序值 */
    private Integer sort;
    
    /** 最后发文时间 */
    private LocalDateTime lastPostTime;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ==================== 常量 ====================
    
    public static final int STATUS_DRAFT = 0;
    public static final int STATUS_PUBLISHED = 1;
    public static final int STATUS_ARCHIVED = 2;
    
    public static final int RECOMMENDED_NO = 0;
    public static final int RECOMMENDED_YES = 1;
    
    // ==================== 工厂方法 ====================
    
    /**
     * 创建专栏
     */
    public static Column create(Long userId, String name, String description, 
                                String coverUrl, Integer status) {
        return Column.builder()
                .userId(userId)
                .name(name)
                .description(description)
                .coverUrl(coverUrl)
                .status(status != null ? status : STATUS_DRAFT)
                .postCount(0)
                .subscribeCount(0)
                .isRecommended(RECOMMENDED_NO)
                .sort(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    // ==================== 业务方法 ====================
    
    public boolean isDraft() {
        return STATUS_DRAFT == status;
    }
    
    public boolean isPublished() {
        return STATUS_PUBLISHED == status;
    }
    
    public boolean isArchived() {
        return STATUS_ARCHIVED == status;
    }
    
    public boolean isRecommendedColumn() {
        return RECOMMENDED_YES == isRecommended;
    }
    
    public void incrementPostCount() {
        this.postCount = (this.postCount == null ? 0 : this.postCount) + 1;
        this.updateTime = LocalDateTime.now();
    }
    
    public void decrementPostCount() {
        this.postCount = Math.max(0, (this.postCount == null ? 0 : this.postCount) - 1);
        this.updateTime = LocalDateTime.now();
    }
    
    public void incrementSubscribeCount() {
        this.subscribeCount = (this.subscribeCount == null ? 0 : this.subscribeCount) + 1;
        this.updateTime = LocalDateTime.now();
    }
    
    public void decrementSubscribeCount() {
        this.subscribeCount = Math.max(0, (this.subscribeCount == null ? 0 : this.subscribeCount) - 1);
        this.updateTime = LocalDateTime.now();
    }
    
    public void updateLastPostTime(LocalDateTime time) {
        this.lastPostTime = time;
        this.updateTime = LocalDateTime.now();
    }
}
