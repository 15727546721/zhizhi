package cn.xu.model.enums;

import cn.xu.common.ResponseCode;
import cn.xu.support.exception.BusinessException;
import lombok.Getter;

/**
 * 评论类型枚举
 */
@Getter
public enum CommentType {
    POST(1, "帖子评论"),
    COMMENT(3, "评论回复");

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
        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论类型不存在");
    }
}
