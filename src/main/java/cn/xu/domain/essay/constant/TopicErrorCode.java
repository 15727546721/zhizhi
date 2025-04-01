package cn.xu.domain.essay.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum TopicErrorCode {
    TITLE_EMPTY(40001, "话题标题不能为空"),
    CONTENT_EMPTY(40002, "话题内容不能为空"),
    USER_ID_EMPTY(40003, "用户ID不能为空"),
    CATEGORY_NOT_FOUND(40004, "话题分类不存在"),
    TOPIC_NOT_FOUND(40005, "话题不存在"),
    NO_PERMISSION(40006, "无权操作该话题"),
    CATEGORY_NAME_EMPTY(40007, "分类名称不能为空");

    private final Integer code;
    private final String message;
} 