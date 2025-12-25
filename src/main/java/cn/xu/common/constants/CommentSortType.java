package cn.xu.common.constants;

import lombok.Getter;

/**
 * 评论排序类型枚举
 * 用于指定评论的排序方式
 *
 *
 */
@Getter
public enum CommentSortType {
    NEW("create_time", "创建时间"),
    HOT("like_count", "点赞数"),
    TIME("create_time", "时间排序");

    /**
     * 排序字段
     */
    private String value;

    /**
     * 排序类型描述
     */
    private String desc;

    CommentSortType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
