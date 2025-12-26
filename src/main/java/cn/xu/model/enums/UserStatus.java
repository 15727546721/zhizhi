package cn.xu.model.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 用户状态枚举
 */
@Getter
@AllArgsConstructor
public enum UserStatus {
    NORMAL(0, "正常"),
    LOCKED(1, "锁定");

    private final Integer code;
    private final String message;
}
