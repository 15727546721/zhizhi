package cn.xu.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

public class Constants {

    public final static String SPLIT = ",";

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum ResponseCode {

        SUCCESS(20000, "成功"),
        UN_ERROR(20001, "未知失败"),
        ILLEGAL_PARAMETER(20002, "非法参数"),
        NULL_PARAMETER(20003, "请求参数为空"),
        NULL_RESPONSE(20004, "响应参数为空"),
        DUPLICATE_KEY(20005, "重复的键");

        private Integer code;
        private String info;

    }

    @AllArgsConstructor
    @NoArgsConstructor
    @Getter
    public enum UserErrorCode {

        ILLEGAL_TOKEN(30001, "非法 token"),
        TOKEN_EXPIRED(30002, "token 过期"),
        USER_NOT_FOUND(30003, "用户不存在"),
        USER_ALREADY_EXIST(30004, "用户已存在"),
        USER_PASSWORD_ERROR(30005, "密码错误"),
        ;

        private Integer code;
        private String info;

    }
}
