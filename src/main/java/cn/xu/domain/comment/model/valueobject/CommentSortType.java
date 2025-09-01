package cn.xu.domain.comment.model.valueobject;

import lombok.Getter;

@Getter
public enum CommentSortType {
    NEW("create_time", "最新"),
    HOT("like_count", "最热");

    private String value;
    private String desc;

    CommentSortType(String value, String desc) {
        this.value = value;
        this.desc = desc;
    }
}
