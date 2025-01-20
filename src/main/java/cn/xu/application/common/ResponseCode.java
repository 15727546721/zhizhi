package cn.xu.application.common;

import lombok.Getter;

@Getter
public enum ResponseCode {
    SUCCESS(20000, "成功"),
    UN_ERROR(20001, "未知失败"),
    ILLEGAL_PARAMETER(20002, "非法参数"),
    NULL_PARAMETER(20003, "请求参数为空"),
    NULL_RESPONSE(20004, "响应参数为空"),
    DUPLICATE_KEY(20005, "重复的键");

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
} 