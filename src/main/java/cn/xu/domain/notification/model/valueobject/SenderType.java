package cn.xu.domain.notification.model.valueobject;

/**
 * 发送者类型枚举
 */
public enum SenderType {
    /**
     * 系统
     */
    SYSTEM(1),

    /**
     * 用户
     */
    USER(2);

    private final Integer value;

    // 私有构造函数
    SenderType(Integer value) {
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
