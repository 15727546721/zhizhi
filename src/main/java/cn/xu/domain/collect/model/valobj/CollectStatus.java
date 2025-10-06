package cn.xu.domain.collect.model.valobj;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 收藏状态枚举
 */
@Getter
@AllArgsConstructor
public enum CollectStatus {
    UNCOLLECTED(0, "未收藏"),
    COLLECTED(1, "已收藏");

    private final Integer code;
    private final String desc;

    public static CollectStatus fromCode(Integer code) {
        for (CollectStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        // 默认返回未收藏状态
        return UNCOLLECTED;
    }
}