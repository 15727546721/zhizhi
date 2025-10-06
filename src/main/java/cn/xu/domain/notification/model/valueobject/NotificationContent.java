package cn.xu.domain.notification.model.valueobject;

import cn.xu.common.exception.BusinessException;
import lombok.Getter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 通知内容值对象
 * 确保内容的不可变性和封装性
 * 
 * @author xu
 */
@Getter
public class NotificationContent {
    private final String title;
    private final String content;
    private final Map<String, Object> extra;

    private NotificationContent(String title, String content, Map<String, Object> extra) {
        // 验证参数
        if (title == null || title.trim().isEmpty()) {
            throw new BusinessException("通知标题不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("通知内容不能为空");
        }
        
        this.title = title.trim();
        this.content = content.trim();
        // 使用不可变Map确保值对象的不可变性
        this.extra = extra != null ? Collections.unmodifiableMap(new HashMap<>(extra)) : Collections.emptyMap();
    }

    public static NotificationContent of(String title, String content) {
        return new NotificationContent(title, content, null);
    }

    public static NotificationContent of(String title, String content, Map<String, Object> extra) {
        return new NotificationContent(title, content, extra);
    }
    
    /**
     * 获取额外信息中的特定值
     */
    public Object getExtraValue(String key) {
        return extra.get(key);
    }
    
    /**
     * 判断是否包含特定的额外信息
     */
    public boolean hasExtra(String key) {
        return extra.containsKey(key);
    }
    
    /**
     * 判断是否为空内容
     */
    public boolean isEmpty() {
        return title.isEmpty() && content.isEmpty();
    }
    
    /**
     * 获取内容长度
     */
    public int getContentLength() {
        return content.length();
    }
    
    /**
     * 获取简化的内容（用于显示）
     */
    public String getBriefContent(int maxLength) {
        if (content.length() <= maxLength) {
            return content;
        }
        return content.substring(0, maxLength) + "...";
    }
    
    /**
     * 验证内容是否合法
     */
    public boolean isValid() {
        return title != null && !title.trim().isEmpty() && 
               content != null && !content.trim().isEmpty();
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        NotificationContent that = (NotificationContent) obj;
        return Objects.equals(title, that.title) &&
               Objects.equals(content, that.content) &&
               Objects.equals(extra, that.extra);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(title, content, extra);
    }
    
    @Override
    public String toString() {
        return String.format("NotificationContent{title='%s', content='%s', extraSize=%d}", 
                           title, content, extra.size());
    }
} 