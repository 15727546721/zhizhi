package cn.xu.common.constant;

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

    /**
     * 类型值
     */
    private final Integer value;

    /**
     * 类型描述
     */
    private final String description;

    CommentType(Integer value, String description) {
        this.value = value;
        this.description = description;
    }

    /**
     * 根据类型值获取对应的评论类型
     *
     * @param value 评论类型的值
     * @return 对应的CommentType枚举
     * @throws BusinessException 如果传入的值不匹配任何评论类型，抛出异常
     */
    public static CommentType valueOf(Integer value) {
        for (CommentType type : CommentType.values()) {
            if (type.getValue().equals(value)) {
                return type;
            }
        }
        throw new BusinessException(ResponseCode.ILLEGAL_PARAMETER.getCode(), "评论类型不存在");
    }

}
