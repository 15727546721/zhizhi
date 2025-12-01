package cn.xu.model.entity;

import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 收藏实体
 *
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Favorite {
    
    // ==================== 状态常量 ====================
    
    /** 收藏状态 */
    public static final int STATUS_FAVORITED = 1;
    /** 取消收藏状态 */
    public static final int STATUS_UNFAVORITED = 0;
    
    // ==================== 字段 ====================

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 被收藏内容ID
     */
    private Long targetId;

    /**
     * 所属收藏夹ID，如果为空，表示是普通收藏
     */
    private Long folderId;

    /**
     * 收藏内容类型：post-帖子
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
                if (type.code.equals(code.toLowerCase())) {
                    return type;
                }
            }
            throw new BusinessException("无效的收藏类型: " + code);
        }
    }
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建新的收藏记录
     */
    public static Favorite createFavorite(Long userId, Long targetId, String targetType, Long folderId) {
        validateParams(userId, targetId, targetType);
        
        LocalDateTime now = LocalDateTime.now();
        return Favorite.builder()
                .userId(userId)
                .targetId(targetId)
                .targetType(targetType)
                .folderId(folderId)
                .status(STATUS_FAVORITED)
                .createTime(now)
                .updateTime(now)
                .build();
    }
    
    /**
     * 收藏操作
     */
    public void favorite() {
        if (isFavorited()) {
            throw new BusinessException("已经收藏，无需重复操作");
        }
        this.status = STATUS_FAVORITED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 取消收藏操作
     */
    public void unfavorite() {
        if (!isFavorited()) {
            throw new BusinessException("尚未收藏，无法取消");
        }
        this.status = STATUS_UNFAVORITED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 判断是否已收藏
     */
    public boolean isFavorited() {
        return STATUS_FAVORITED == this.status;
    }
    
    /**
     * 移动到指定收藏夹
     */
    public void moveToFolder(Long targetFolderId) {
        if (targetFolderId == null) {
            throw new BusinessException("目标收藏夹ID不能为空");
        }
        if (!isFavorited()) {
            throw new BusinessException("只能移动已收藏的内容");
        }
        this.folderId = targetFolderId;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 验证收藏参数
     */
    private static void validateParams(Long userId, Long targetId, String targetType) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("目标ID不能为空");
        }
        if (targetType == null || targetType.trim().isEmpty()) {
            throw new BusinessException("收藏类型不能为空");
        }
        // 验证类型是否有效
        FavoriteType.fromCode(targetType);
    }
    
    /**
     * 验证业务规则
     */
    public void validate() {
        validateParams(this.userId, this.targetId, this.targetType);
    }
}