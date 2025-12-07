package cn.xu.common;

import lombok.Getter;

/**
 * 响应码枚举
 * 定义系统中使用的标准响应码
 *
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

    // 通用错误码
    NOT_FOUND(20404, "资源不存在"),

    // 用户相关错误码
    USER_NOT_FOUND(40001, "用户不存在"),
    USER_DISABLED(40002, "用户已被禁用"),
    USER_ALREADY_EXISTS(40003, "用户已存在"),
    PASSWORD_ERROR(40004, "密码错误"),

    // 认证相关错误码（30xxx - 前端会触发重新登录）
    NOT_LOGIN(30001, "未登录"),
    TOKEN_INVALID(30002, "token无效"),
    TOKEN_EXPIRED(30003, "token已过期"),
    TOKEN_KICKED_OUT(30004, "token已被踢下线"),
    TOKEN_REPLACED(30005, "token已被顶下线"),

    // 权限相关错误码
    FORBIDDEN(40301, "无权限访问"),
    ;

    private final int code;
    private final String message;

    ResponseCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
