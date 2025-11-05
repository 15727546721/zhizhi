package cn.xu.domain.favorite.model.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏领域实体
 */
@Data
@Builder
public class FavoriteEntity {

    /**
     * 收藏ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 被收藏内容ID（帖子ID、文章ID等）
     */
    private Long targetId;

    /**
     * 所属收藏夹ID，如果为空，表示是普通收藏
     */
    private Long folderId;

    /**
     * 收藏内容类型（post、essay等）
     */
    private String targetType;

    /**
     * 收藏状态：1-收藏，0-未收藏
     */
    private Integer status;

    /**
     * 收藏时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 设置收藏状态
     * 
     * @param status 收藏状态
     */
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    /**
     * 设置ID
     * 
     * @param id ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * 设置类型
     * 
     * @param targetType 类型
     */
    public void setTargetType(String targetType) {
        this.targetType = targetType;
    }
    
    /**
     * 设置收藏夹ID
     * 
     * @param folderId 收藏夹ID
     */
    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }
    
    /**
     * 设置更新时间
     * 
     * @param updateTime 更新时间
     */
    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }
}