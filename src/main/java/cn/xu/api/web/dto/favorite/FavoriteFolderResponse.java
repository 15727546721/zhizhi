package cn.xu.api.web.dto.favorite;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 收藏夹响应数据
 */
@Data
public class FavoriteFolderResponse {
    
    /**
     * 收藏夹ID
     */
    private Long id;
    
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
}