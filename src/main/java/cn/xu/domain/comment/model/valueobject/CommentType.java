package cn.xu.domain.comment.model.valueobject;

import cn.xu.exception.BusinessException;
import cn.xu.infrastructure.common.ResponseCode;
import lombok.Getter;

/**
 * 评论类型值对象
 */
@Getter
public enum CommentType {
    ARTICLE(1, "文章评论"),
    TOPIC(2, "话题评论");

    private final Integer value;
    private final String description;

    CommentType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    public static CommentType of(Integer value) {
        if (value == null) {
            throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "评论类型不能为空");
        }

        for (CommentType type : CommentType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new BusinessException(ResponseCode.UN_ERROR.getCode(), "无效的评论类型：" + value);
    }

    public static boolean isValid(Integer value) {
        if (value == null) {
            return false;
        }
        for (CommentType type : CommentType.values()) {
            if (type.getValue().equals(value)) {
                return true;
            }
        }
        return false;
    }
} 