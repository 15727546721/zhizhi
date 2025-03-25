package cn.xu.domain.comment.model.valueobject;

import cn.xu.application.common.ResponseCode;
import cn.xu.infrastructure.common.exception.BusinessException;
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

    public static CommentType valueOf(Integer value) {
        for (CommentType type : CommentType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论类型不正确");
    }
} 