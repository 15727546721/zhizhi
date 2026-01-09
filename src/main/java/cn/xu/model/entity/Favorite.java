package cn.xu.model.entity;

import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 收藏实体
 * <p>用户收藏帖子等内容</p>
 
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ==================== 状态常量 ====================
    
    /** 收藏状态 */
    public static final int STATUS_FAVORITED = 1;
    
    /** 取消收藏状态 */
    public static final int STATUS_UNFAVORITED = 0;
    
    // ==================== 字段 ====================
    
    /** 主键ID */
    private Long id;
    
    /** 用户ID */
    private Long userId;
    
    /** 被收藏内容ID */
    private Long targetId;
    
    /** 收藏内容类型：post-帖子 */
    private String targetType;
    
    /** 收藏状态：1-收藏 0-未收藏 */
    private Integer status;
    
    /** 收藏时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ==================== 收藏类型枚举 ====================
    
    public enum FavoriteType {
        POST("post", "帖子");
        
        private final String code;
        private final String desc;
        
        FavoriteType(String code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public String getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        public static FavoriteType fromCode(String code) {
            if (code == null || code.trim().isEmpty()) {
                throw new BusinessException("收藏类型不能为空");
            }
            for (FavoriteType type : values()) {
                if (type.code.equalsIgnoreCase(code)) {
                    return type;
                }
            }
            throw new BusinessException("不支持的收藏类型: " + code);
        }
    }
    
    // ==================== 工厂方法 ====================
    
    /**
     * 创建新收藏
     */
    public static Favorite createFavorite(Long userId, Long targetId, String targetType) {
        return Favorite.builder()
                .userId(userId)
                .targetId(targetId)
                .targetType(targetType)
                .status(STATUS_FAVORITED)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    // ==================== 业务方法 ====================
    
    /**
     * 是否已收藏
     */
    public boolean isFavorited() {
        return STATUS_FAVORITED == status;
    }
    
    /**
     * 执行收藏
     */
    public void favorite() {
        this.status = STATUS_FAVORITED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 执行取消收藏
     */
    public void unfavorite() {
        this.status = STATUS_UNFAVORITED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 标记为已收藏
     */
    public void markAsFavorited() {
        this.status = STATUS_FAVORITED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 标记为取消收藏
     */
    public void markAsUnfavorited() {
        this.status = STATUS_UNFAVORITED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 切换收藏状态
     */
    public void toggleFavorite() {
        if (isFavorited()) {
            markAsUnfavorited();
        } else {
            markAsFavorited();
        }
    }
}