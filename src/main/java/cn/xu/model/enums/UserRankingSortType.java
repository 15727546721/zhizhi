package cn.xu.model.enums;

import lombok.Getter;

/**
 * 用户排行榜排序类型枚举
 */
@Getter
public enum UserRankingSortType {
    
    FANS("fans", "粉丝数"),
    LIKES("likes", "获赞数"),
    POSTS("posts", "帖子数"),
    COMPREHENSIVE("comprehensive", "综合排序");
    
    private final String code;
    private final String description;
    
    UserRankingSortType(String code, String description) {
        this.code = code;
        this.description = description;
    }
    
    /**
     * 根据code获取枚举
     */
    public static UserRankingSortType fromCode(String code) {
        if (code == null) {
            return COMPREHENSIVE;
        }
        for (UserRankingSortType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        return COMPREHENSIVE;
    }
}
