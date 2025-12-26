package cn.xu.model.enums;

import lombok.Getter;

/**
 * 评论排序类型枚举
 */
@Getter
public enum CommentSortType {
    NEW("create_time", "创建时间"),
    HOT("like_count", "点赞数"),
    TIME("create_time", "时间排序");

    private final String value;
    private final String desc;

    CommentSortType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
