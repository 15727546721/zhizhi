package cn.xu.domain.notification.strategy;

import cn.xu.domain.notification.model.aggregate.NotificationAggregate;

/**
 * 通知发送策略接口
 */
public interface NotificationSendStrategy {
    /**
     * 发送通知
     */
    void send(NotificationAggregate notification);

    /**
     * 获取策略类型
     */
    NotificationSendStrategyType getType();

    /**
     * 策略类型枚举
     */
    enum NotificationSendStrategyType {
        DATABASE,   // 数据库存储
        EMAIL,      // 邮件通知
        SMS,        // 短信通知
        PUSH,       // APP推送
        SITE        // 站内信
    }
} 