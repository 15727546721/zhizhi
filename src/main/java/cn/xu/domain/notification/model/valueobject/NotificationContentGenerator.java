package cn.xu.domain.notification.model.valueobject;

import cn.xu.domain.notification.model.entity.NotificationContent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * 通知内容生成器
 * 根据不同的业务场景生成通知内容
 */
@Component
@RequiredArgsConstructor
public class NotificationContentGenerator {

    /**
     * 生成点赞通知内容
     */
    public NotificationContent generateLikeContent(String senderName, BusinessType businessType, Long businessId, String targetTitle) {
        String content = String.format("%s赞了你的%s《%s》", senderName, businessType.getDescription(), targetTitle);
        
        Map<String, Object> extra = new HashMap<>();
        extra.put("senderName", senderName);
        extra.put("targetTitle", targetTitle);
        
        return NotificationContent.of(
            "收到新的点赞",
            content,
            NotificationType.LIKE,
            businessType,
            businessId,
            extra
        );
    }

    /**
     * 生成评论通知内容
     */
    public NotificationContent generateCommentContent(String senderName, BusinessType businessType, Long businessId, 
            String targetTitle, String commentContent) {
        String content = String.format("%s评论了你的%s《%s》：%s", 
            senderName, businessType.getDescription(), targetTitle, commentContent);
        
        Map<String, Object> extra = new HashMap<>();
        extra.put("senderName", senderName);
        extra.put("targetTitle", targetTitle);
        extra.put("commentContent", commentContent);
        
        return NotificationContent.of(
            "收到新的评论",
            content,
            NotificationType.COMMENT,
            businessType,
            businessId,
            extra
        );
    }

    /**
     * 生成回复通知内容
     */
    public NotificationContent generateReplyContent(String senderName, Long commentId,
                                                    String originalComment, String replyContent) {
        String content = String.format("%s回复了你的评论\"%s\"：%s", 
            senderName, originalComment, replyContent);
        
        Map<String, Object> extra = new HashMap<>();
        extra.put("senderName", senderName);
        extra.put("originalComment", originalComment);
        extra.put("replyContent", replyContent);
        
        return NotificationContent.of(
            "收到新的回复",
            content,
            NotificationType.REPLY,
            BusinessType.COMMENT,
            commentId,
            extra
        );
    }

    /**
     * 生成收藏通知内容
     */
    public NotificationContent generateFavoriteContent(String senderName, BusinessType businessType, 
            Long businessId, String targetTitle) {
        String content = String.format("%s收藏了你的%s《%s》", 
            senderName, businessType.getDescription(), targetTitle);
        
        Map<String, Object> extra = new HashMap<>();
        extra.put("senderName", senderName);
        extra.put("targetTitle", targetTitle);
        
        return NotificationContent.of(
            "收到新的收藏",
            content,
            NotificationType.FAVORITE,
            businessType,
            businessId,
            extra
        );
    }

    /**
     * 生成关注通知内容
     */
    public NotificationContent generateFollowContent(String senderName) {
        String content = String.format("%s关注了你", senderName);
        
        Map<String, Object> extra = new HashMap<>();
        extra.put("senderName", senderName);
        
        return NotificationContent.of(
            "收到新的关注",
            content,
            NotificationType.FOLLOW,
            BusinessType.USER,
            null,
            extra
        );
    }

    /**
     * 生成系统通知内容
     */
    public NotificationContent generateSystemContent(String title, String content) {
        return NotificationContent.of(
            title,
            content,
            NotificationType.SYSTEM,
            null,
            null,
            new HashMap<>()
        );
    }
} 