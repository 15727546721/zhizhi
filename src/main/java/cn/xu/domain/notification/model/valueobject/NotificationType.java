package cn.xu.domain.notification.model.valueobject;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 通知类型值对象
 * 定义所有可能的通知类型及其与业务类型的关联
 */
@Getter
@RequiredArgsConstructor
public enum NotificationType {
    /**
     * 系统通知
     */
    SYSTEM("系统通知", null),
    
    /**
     * 点赞通知
     */
    LIKE("点赞通知", createBusinessTypeSet(BusinessType.ARTICLE, BusinessType.COMMENT, BusinessType.REPLY, BusinessType.TOPIC)),
    
    /**
     * 评论通知
     */
    COMMENT("评论通知", createBusinessTypeSet(BusinessType.ARTICLE, BusinessType.TOPIC)),
    
    /**
     * 回复通知
     */
    REPLY("回复通知", createBusinessTypeSet(BusinessType.COMMENT)),
    
    /**
     * 收藏通知
     */
    FAVORITE("收藏通知", createBusinessTypeSet(BusinessType.ARTICLE, BusinessType.TOPIC)),
    
    /**
     * 关注通知
     */
    FOLLOW("关注通知", createBusinessTypeSet(BusinessType.USER));

    private final String description;
    private final Set<BusinessType> supportedBusinessTypes;

    /**
     * 创建不可变的BusinessType集合
     */
    private static Set<BusinessType> createBusinessTypeSet(BusinessType... types) {
        Set<BusinessType> set = new HashSet<>();
        Collections.addAll(set, types);
        return Collections.unmodifiableSet(set);
    }

    /**
     * 检查是否支持指定的业务类型
     */
    public boolean supportsBusinessType(BusinessType businessType) {
        return supportedBusinessTypes.contains(businessType);
    }

    /**
     * 获取支持此通知类型的所有业务类型
     */
    public Set<BusinessType> getSupportedBusinessTypes() {
        return supportedBusinessTypes;
    }

    /**
     * 验证通知类型和业务类型的组合是否有效
     */
    public void validateBusinessType(BusinessType businessType) {
        if (businessType != null && !supportsBusinessType(businessType)) {
            throw new IllegalArgumentException(String.format(
                "通知类型 %s 不支持业务类型 %s。支持的业务类型: %s",
                this.description,
                businessType.getDescription(),
                supportedBusinessTypes.stream()
                    .map(BusinessType::getDescription)
                    .collect(Collectors.joining(", "))
            ));
        }
    }

    /**
     * 获取通知内容模板
     */
    public String getContentTemplate(BusinessType businessType) {
        validateBusinessType(businessType);
        return businessType.getActionDescription(this);
    }

    /**
     * 检查是否需要业务ID
     */
    public boolean requiresBusinessId() {
        return this != SYSTEM && this != FOLLOW;
    }

    /**
     * 检查是否是用户触发的通知
     */
    public boolean isUserTriggered() {
        return this != SYSTEM;
    }

    /**
     * 获取通知的优先级（可用于排序）
     */
    public int getPriority() {
        switch (this) {
            case SYSTEM:
                return 0;  // 系统通知最高优先级
            case COMMENT:
            case REPLY:
                return 1;  // 评论和回复次之
            case LIKE:
            case FAVORITE:
                return 2;  // 点赞和收藏再次之
            case FOLLOW:
                return 3;  // 关注最低优先级
            default:
                throw new IllegalStateException("未知的通知类型: " + this);
        }
    }

    /**
     * 获取通知类型的值（用于数据库存储）
     */
    public int getValue() {
        return this.ordinal();
    }

    /**
     * 根据值获取通知类型
     */
    public static NotificationType fromValue(int value) {
        for (NotificationType type : values()) {
            if (type.ordinal() == value) {
                return type;
            }
        }
        throw new IllegalArgumentException("无效的通知类型值: " + value);
    }
} 