package cn.xu.domain.favorite.model.entity;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹领域实体
 */
@Data
@Builder
public class FavoriteFolderEntity {

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
     * 是否公开（1-公开，0-不公开）
     */
    private Integer isPublic;

    /**
     * 内容数量
     */
    private Integer contentCount;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;
    
    /**
     * 更新时间
     */
    private LocalDateTime updateTime;
    
    /**
     * 设置ID
     * 
     * @param id ID
     */
    public void setId(Long id) {
        this.id = id;
    }
    
    /**
     * 设置名称
     * 
     * @param name 名称
     */
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * 设置描述
     * 
     * @param description 描述
     */
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * 设置内容数量
     * 
     * @param contentCount 内容数量
     */
    public void setContentCount(Integer contentCount) {
        this.contentCount = contentCount;
    }
}