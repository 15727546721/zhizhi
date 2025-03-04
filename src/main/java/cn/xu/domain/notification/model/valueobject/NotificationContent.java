package cn.xu.domain.notification.model.valueobject;

import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知内容值对象
 */
@Getter
public class NotificationContent {
    private final String title;
    private final String content;
    private final Map<String, Object> extra;

    private NotificationContent(String title, String content, Map<String, Object> extra) {
        this.title = title;
        this.content = content;
        this.extra = extra != null ? extra : new HashMap<>();
    }

    public static NotificationContent of(String title, String content) {
        return new NotificationContent(title, content, null);
    }

    public static NotificationContent of(String title, String content, Map<String, Object> extra) {
        return new NotificationContent(title, content, extra);
    }
} 