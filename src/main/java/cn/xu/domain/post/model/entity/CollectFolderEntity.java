package cn.xu.domain.post.model.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹领域实体（已废弃）
 * 
 * @deprecated 请使用 {@link cn.xu.domain.collect.model.entity.CollectFolderEntity} 替代
 */
@Data
@Builder
@Deprecated
public class CollectFolderEntity {

    /**
     * 收藏夹ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 收藏夹名称
     */
    private String name;

    /**
     * 收藏夹描述
     */
    private String description;

    /**
     * 是否为默认收藏夹
     */
    private Boolean isDefault;

    /**
     * 收藏帖子数量
     */
    private Integer postCount;

    /**
     * 是否公开
     */
    private Boolean isPublic;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 设置收藏夹名称
     * 
     * @param name 收藏夹名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 设置收藏夹描述
     * 
     * @param description 收藏夹描述
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 设置是否公开
     * 
     * @param isPublic 是否公开
     */
    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }
    
    /**
     * 设置更新时间
     * 
     * @param updateTime 更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
    
    /**
     * 设置收藏帖子数量
     * 
     * @param postCount 收藏帖子数量
     */
    public void setPostCount(Integer postCount) {
        this.postCount = postCount;
    }
    
    /**
     * 增加收藏帖子数量
     */
    public void incrementPostCount() {
        this.postCount = (this.postCount == null ? 0 : this.postCount) + 1;
    }
    
    /**
     * 减少收藏帖子数量
     */
    public void decrementPostCount() {
        this.postCount = Math.max(0, (this.postCount == null ? 0 : this.postCount) - 1);
    }
}