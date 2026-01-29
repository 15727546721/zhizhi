package cn.xu.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户关系枚举
 */
@Getter
@AllArgsConstructor
public enum UserRelationship {

    MUTUAL_FOLLOW("相互关注"),
    NON_MUTUAL_FOLLOW("单向关注");

    private final String description;

    public boolean isMutualFollow() {
        return this == MUTUAL_FOLLOW;
    }

    public boolean isNonMutualFollow() {
        return this == NON_MUTUAL_FOLLOW;
    }
}
