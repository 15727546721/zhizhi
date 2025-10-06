package cn.xu.common;

import lombok.Getter;

/**
 * 响应码枚举
 * 定义系统中使用的标准响应码
 */
@Getter
public enum ResponseCode {
    SUCCESS(20000, "成功"),
    UN_ERROR(20001, "未知失败"),
    ILLEGAL_PARAMETER(20002, "非法参数"),
    PARAM_ERROR(20003, "参数错误"),
    NULL_PARAMETER(20004, "请求参数为空"),
    NULL_RESPONSE(20005, "响应参数为空"),
    DUPLICATE_KEY(20006, "重复的键"),
    SYSTEM_ERROR(20007, "系统内部错误"),
    ;

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}