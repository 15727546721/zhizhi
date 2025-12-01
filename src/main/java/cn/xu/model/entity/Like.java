package cn.xu.model.entity;

import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 点赞实体
 *
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Like {
    
    // ==================== 状态常量 ====================
    
    /** 点赞状态 */
    public static final int STATUS_LIKED = 1;
    /** 取消点赞状态 */
    public static final int STATUS_UNLIKED = 0;
    
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
     * 目标ID
     */
    private Long targetId;

    /**
     * 点赞类型：1-帖子，2-评论
     */
    private Integer type;

    /**
     * 是否点赞，1-点赞，0-取消点赞
     */
    private Integer status;

    /**
     * 点赞时间
     */
    private LocalDateTime createTime;
    
    // ==================== 点赞类型枚举 ====================
    
    public enum LikeType {
        POST(1, "帖子"),
        ESSAY(2, "随笔"),
        COMMENT(3, "评论");
        
        private final int code;
        private final String desc;
        
        LikeType(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getDesc() {
            return desc;
        }
        
        /**
         * 获取用于Redis键的名称（小写）
         */
        public String getRedisKeyName() {
            return this.name().toLowerCase();
        }
        
        /**
         * 检查是否为帖子类型
         */
        public boolean isPost() {
            return this == POST;
        }
        
        /**
         * 检查是否为随笔类型
         */
        public boolean isEssay() {
            return this == ESSAY;
        }
        
        /**
         * 检查是否为评论类型
         */
        public boolean isComment() {
            return this == COMMENT;
        }
        
        /**
         * 从code获取类型
         */
        public static LikeType fromCode(Integer code) {
            if (code == null) {
                throw new BusinessException("点赞类型不能为空");
            }
            for (LikeType type : values()) {
                if (type.code == code) {
                    return type;
                }
            }
            throw new BusinessException("无效的点赞类型: " + code);
        }
        
        /**
         * 从名称获取类型（兼容方法）
         */
        public static LikeType fromName(String name) {
            if (name == null) {
                return null;
            }
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException e) {
                return null;
            }
        }
        
        /**
         * 通过整数值获取类型（兼容方法）
         */
        public static LikeType valueOf(Integer code) {
            return fromCode(code);
        }
    }
    
    // ==================== 业务方法 ====================
    
    /**
     * 创建新的点赞记录
     */
    public static Like createLike(Long userId, Long targetId, Integer type) {
        validateParams(userId, targetId, type);
        
        return Like.builder()
                .userId(userId)
                .targetId(targetId)
                .type(type)
                .status(STATUS_LIKED)
                .createTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 点赞操作
     */
    public void like() {
        if (isLiked()) {
            throw new BusinessException("已经点赞，无需重复操作");
        }
        this.status = STATUS_LIKED;
        this.createTime = LocalDateTime.now();
    }
    
    /**
     * 取消点赞操作
     */
    public void unlike() {
        if (!isLiked()) {
            throw new BusinessException("尚未点赞，无法取消");
        }
        this.status = STATUS_UNLIKED;
    }
    
    /**
     * 判断是否已点赞
     */
    public boolean isLiked() {
        return STATUS_LIKED == this.status;
    }
    
    /**
     * 验证点赞参数
     */
    private static void validateParams(Long userId, Long targetId, Integer type) {
        if (userId == null || userId <= 0) {
            throw new BusinessException("用户ID不能为空");
        }
        if (targetId == null || targetId <= 0) {
            throw new BusinessException("目标ID不能为空");
        }
        if (type == null) {
            throw new BusinessException("点赞类型不能为空");
        }
        // 验证类型是否有效
        LikeType.fromCode(type);
    }
    
    /**
     * 验证业务规则
     */
    public void validate() {
        validateParams(this.userId, this.targetId, this.type);
    }
}