package cn.xu.domain.notification.model.entity;

import cn.xu.domain.notification.model.valueobject.BusinessType;
import cn.xu.domain.notification.model.valueobject.NotificationType;
import lombok.Builder;
import lombok.Getter;

import java.util.Map;

/**
 * 通知内容值对象
 * 封装通知的内容相关属性，确保内容的不可变性
 */
@Getter
@Builder
public class NotificationContent {
    
    private final String title;
    private final String content;
    private final NotificationType type;
    private final BusinessType notificationBusinessType;
    private final Long businessId;
    private final Map<String, Object> extra;

    public static NotificationContent of(String title, String content, NotificationType type,
                                         BusinessType notificationBusinessType, Long businessId, Map<String, Object> extra) {
        return NotificationContent.builder()
                .title(title)
                .content(content)
                .type(type)
                .notificationBusinessType(notificationBusinessType)
                .businessId(businessId)
                .extra(extra)
                .build();
    }

    /**
     * 验证通知内容是否有效
     * @return 是否有效
     */
    public boolean isValid() {
        return title != null && !title.isEmpty() 
                && content != null && !content.isEmpty()
                && type != null;
    }
} 