package cn.xu.domain.notification.model.valueobject;

import cn.xu.infrastructure.common.exception.BusinessException;
import lombok.Getter;

/**
 * 业务类型枚举（文章、话题）
 */
@Getter
public enum BusinessType {
    SYSTEM(0, "系统"),
    ARTICLE(1, "文章"),
    TOPIC(2, "话题"),
    USER(3, "用户");

    private final Integer value;
    private final String description;

    BusinessType(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public static BusinessType getType(int type) {
        for (BusinessType businessType : BusinessType.values()) {
            if (businessType.getValue() == type) {
                return businessType;
            }
        }
        throw new BusinessException("[通知服务]: 不存在的业务类型");
    }

}
