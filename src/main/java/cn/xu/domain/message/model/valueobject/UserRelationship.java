package cn.xu.domain.message.model.valueobject;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户关系值对象
 */
@Getter
@AllArgsConstructor
public enum UserRelationship {
    /**
     * 互相关注
     */
    MUTUAL_FOLLOW("互相关注"),
    
    /**
     * 非互相关注（陌生人/单方面关注）
     */
    NON_MUTUAL_FOLLOW("非互相关注");
    
    private final String description;
    
    /**
     * 判断是否为互相关注
     */
    public boolean isMutualFollow() {
        return this == MUTUAL_FOLLOW;
    }
    
    /**
     * 判断是否为非互相关注
     */
    public boolean isNonMutualFollow() {
        return this == NON_MUTUAL_FOLLOW;
    }
}

