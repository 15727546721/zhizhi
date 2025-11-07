package cn.xu.domain.like.model;

import cn.xu.common.exception.BusinessException;
import lombok.Getter;

/**
 * 点赞类型枚举
 * 对应数据库表名，表示点赞的目标属于哪个表
 */
@Getter
public enum LikeType {

    /**
     * 帖子表（包括所有类型的帖子：普通帖子、文章、讨论、问答、资源等）
     */
    POST(1, "帖子", "post"),
    
    /**
     * 随笔表
     */
    ESSAY(2, "随笔", "essay"),
    
    /**
     * 评论表
     */
    COMMENT(3, "评论", "comment"),
    ;

    private final int code;
    private final String description;
    /**
     * 对应的数据库表名（小写）
     */
    private final String tableName;

    LikeType(int code, String description, String tableName) {
        this.code = code;
        this.description = description;
        this.tableName = tableName;
    }

    /**
     * 根据代码获取LikeType
     * @param code 类型代码
     * @return LikeType枚举
     */
    public static LikeType valueOf(int code) {
        for (LikeType likeType : LikeType.values()) {
            if (likeType.getCode() == code) {
                return likeType;
            }
        }
        return null;
    }
    
    /**
     * 根据名称获取LikeType
     * @param name 枚举名称
     * @return LikeType枚举
     */
    public static LikeType fromName(String name) {
        try {
            return LikeType.valueOf(name.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * 根据表名获取LikeType（不区分大小写）
     * @param tableName 数据库表名
     * @return LikeType枚举
     */
    public static LikeType fromTableName(String tableName) {
        if (tableName == null || tableName.trim().isEmpty()) {
            throw new BusinessException("表名不能为空");
        }
        
        String lowerTableName = tableName.toLowerCase().trim();
        
        for (LikeType type : values()) {
            if (type.tableName.equals(lowerTableName)) {
                return type;
            }
        }
        
        throw new BusinessException("无效的表名: " + tableName);
    }
    
    /**
     * 获取用于Redis键的名称（小写）
     * @return Redis键名
     */
    public String getRedisKeyName() {
        return this.name().toLowerCase();
    }
    
    /**
     * 检查是否为帖子类型
     * @return 是否为帖子类型
     */
    public boolean isPost() {
        return this == POST;
    }
    
    /**
     * 检查是否为随笔类型
     * @return 是否为随笔类型
     */
    public boolean isEssay() {
        return this == ESSAY;
    }
    
    /**
     * 检查是否为评论类型
     * @return 是否为评论类型
     */
    public boolean isComment() {
        return this == COMMENT;
    }
    
    @Override
    public String toString() {
        return name();
    }
}