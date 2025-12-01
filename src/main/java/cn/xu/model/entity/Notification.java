package cn.xu.model.entity;

import cn.xu.support.exception.BusinessException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 通知实体
 *
 * @author xu
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Notification {
    
    // ==================== 常量定义 ====================
    
    /** 通知类型：系统通知 */
    public static final int TYPE_SYSTEM = 0;
    /** 通知类型：点赞通知 */
    public static final int TYPE_LIKE = 1;
    /** 通知类型：收藏通知 */
    public static final int TYPE_FAVORITE = 2;
    /** 通知类型：评论通知 */
    public static final int TYPE_COMMENT = 3;
    /** 通知类型：回复通知 */
    public static final int TYPE_REPLY = 4;
    /** 通知类型：关注通知 */
    public static final int TYPE_FOLLOW = 5;
    /** 通知类型：@提及通知 */
    public static final int TYPE_MENTION = 6;
    
    /** 业务类型：系统 */
    public static final int BUSINESS_SYSTEM = 0;
    /** 业务类型：帖子 */
    public static final int BUSINESS_POST = 1;
    /** 业务类型：评论 */
    public static final int BUSINESS_COMMENT = 2;
    /** 业务类型：用户 */
    public static final int BUSINESS_USER = 3;
    
    /** 状态：有效 */
    public static final int STATUS_VALID = 1;
    /** 状态：已删除 */
    public static final int STATUS_DELETED = 0;
    
    /** 已读：未读 */
    public static final int READ_NO = 0;
    /** 已读：已读 */
    public static final int READ_YES = 1;
    
    // ==================== 数据库字段 ====================
    
    /** 主键ID */
    private Long id;

    /** 通知类型：0-系统 1-点赞 2-收藏 3-评论 4-回复 5-关注 6-@提及 */
    private Integer type;

    /** 发送者ID */
    private Long senderId;

    /** 接收者ID */
    private Long receiverId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 业务类型：0-系统 1-帖子 2-评论 3-用户 */
    private Integer businessType;

    /** 业务ID */
    private Long businessId;

    /** 是否已读：0-未读 1-已读 */
    private Integer isRead;

    /** 状态：1-有效 0-已删除 */
    private Integer status;

    /** 阅读时间 */
    private LocalDateTime readTime;

    /** 创建时间 */
    private LocalDateTime createTime;

    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ==================== 工厂方法 ====================
    
    /**
     * 创建系统通知
     */
    public static Notification createSystemNotification(Long receiverId, String title, String content) {
        return Notification.builder()
                .type(TYPE_SYSTEM)
                .receiverId(receiverId)
                .title(title)
                .content(content)
                .businessType(BUSINESS_SYSTEM)
                .isRead(READ_NO)
                .status(STATUS_VALID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建点赞通知
     */
    public static Notification createLikeNotification(Long senderId, Long receiverId, Long businessId, int businessType) {
        String content = businessType == BUSINESS_POST ? "赞了你的帖子" : "赞了你的评论";
        return Notification.builder()
                .type(TYPE_LIKE)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .businessType(businessType)
                .businessId(businessId)
                .isRead(READ_NO)
                .status(STATUS_VALID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建评论通知
     */
    public static Notification createCommentNotification(Long senderId, Long receiverId, String content, Long postId) {
        return Notification.builder()
                .type(TYPE_COMMENT)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .businessType(BUSINESS_POST)
                .businessId(postId)
                .isRead(READ_NO)
                .status(STATUS_VALID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建关注通知
     */
    public static Notification createFollowNotification(Long senderId, Long receiverId) {
        return Notification.builder()
                .type(TYPE_FOLLOW)
                .senderId(senderId)
                .receiverId(receiverId)
                .content("关注了你")
                .businessType(BUSINESS_USER)
                .businessId(receiverId)
                .isRead(READ_NO)
                .status(STATUS_VALID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /**
     * 创建@提及通知
     */
    public static Notification createMentionNotification(Long senderId, Long receiverId, Long postId, String commentContent) {
        // 截取评论内容作为通知内容（最多50字）
        String content = "在评论中@了你";
        if (commentContent != null && !commentContent.isEmpty()) {
            String preview = commentContent.length() > 50 ? commentContent.substring(0, 50) + "..." : commentContent;
            content = "在评论中@了你：" + preview;
        }
        return Notification.builder()
                .type(TYPE_MENTION)
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .businessType(BUSINESS_POST)
                .businessId(postId)
                .isRead(READ_NO)
                .status(STATUS_VALID)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    // ==================== 业务方法 ====================
    
    /**
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = READ_YES;
        this.readTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 标记为删除
     */
    public void markAsDeleted() {
        this.status = STATUS_DELETED;
        this.updateTime = LocalDateTime.now();
    }
    
    /**
     * 是否已读
     */
    public boolean isReadStatus() {
        return READ_YES == this.isRead;
    }
    
    /**
     * 是否有效
     */
    public boolean isValid() {
        return STATUS_VALID == this.status;
    }
    
    /**
     * 验证通知有效性
     */
    public void validate() {
        if (receiverId == null) {
            throw new BusinessException("接收者ID不能为空");
        }
        if (type == null) {
            throw new BusinessException("通知类型不能为空");
        }
        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException("通知内容不能为空");
        }
        if (type == TYPE_SYSTEM && (title == null || title.trim().isEmpty())) {
            throw new BusinessException("系统通知的标题不能为空");
        }
    }
    
    /**
     * 检查是否属于指定用户
     */
    public boolean belongsToUser(Long userId) {
        return this.receiverId != null && this.receiverId.equals(userId);
    }
}