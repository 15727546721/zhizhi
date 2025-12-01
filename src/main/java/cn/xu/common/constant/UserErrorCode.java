package cn.xu.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserErrorCode {
    ILLEGAL_TOKEN(30001, "非法 token"),
    TOKEN_EXPIRED(30002, "token 过期"),
    USER_NOT_FOUND(30003, "用户不存在"),
    USER_ALREADY_EXIST(30004, "用户已存在"),
    USER_PASSWORD_ERROR(30005, "密码错误");

    private final Integer code;
    private final String message;
} 