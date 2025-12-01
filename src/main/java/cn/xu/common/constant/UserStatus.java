package cn.xu.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum UserStatus {
    NORMAL(0, "正常"),
    LOCKED(1, "锁定");

    private final Integer code;
    private final String message;
} 