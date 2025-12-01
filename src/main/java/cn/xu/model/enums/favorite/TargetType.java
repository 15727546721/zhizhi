package cn.xu.model.enums.favorite;

import cn.xu.support.exception.BusinessException;
import lombok.Getter;

/**
 * 收藏目标类型枚举
 * 对应数据库表名，表示收藏的目标属于哪个表
 */
@Getter
public enum TargetType {
    
    /**
     * 帖子表（包括所有类型的帖子：普通帖子、文章、讨论、问答、资源等）
     */
    POST("POST", "帖子"),
    
    /**
     * 评论表
     */
    COMMENT("COMMENT", "评论");
    
    private final String code;
    private final String description;
    
    TargetType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据代码获取目标类型（不区分大小写）
     * @param code 类型代码
     * @return TargetType枚举
     */
    public static TargetType fromCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            throw new BusinessException("目标类型代码不能为空");
        }
        
        String upperCode = code.toUpperCase().trim();
        
        for (TargetType type : values()) {
            if (type.code.equals(upperCode)) {
                return type;
            }
        }
        
        throw new BusinessException("无效的目标类型: " + code);
    }
    
    /**
     * 根据代码获取目标类型（不区分大小写，返回null而不是抛出异常）
     * @param code 类型代码
     * @return TargetType枚举，如果不存在返回null
     */
    public static TargetType fromCodeOrNull(String code) {
        if (code == null || code.trim().isEmpty()) {
            return null;
        }
        
        try {
            return fromCode(code);
        } catch (BusinessException e) {
            return null;
        }
    }
    
    /**
     * 检查是否为帖子类型
     * @return 是否为帖子类型
     */
    public boolean isPost() {
        return this == POST;
    }
    
    /**
     * 检查是否为评论类型
     * @return 是否为评论类型
     */
    public boolean isComment() {
        return this == COMMENT;
    }
    
    /**
     * 获取用于数据库存储的代码（小写）
     * @return 小写的代码
     */
    public String getDbCode() {
        return this.code.toLowerCase();
    }
    
    /**
     * 获取用于API的代码（大写）
     * @return 大写的代码
     */
    public String getApiCode() {
        return this.code;
    }
    
    @Override
    public String toString() {
        return code;
    }
}
