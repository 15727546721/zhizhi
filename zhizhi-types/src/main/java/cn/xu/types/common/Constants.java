package cn.xu.types.common;

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
        ;

        private Integer code;
        private String info;

    }

}
