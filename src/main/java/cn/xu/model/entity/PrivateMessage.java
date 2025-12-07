package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 私信消息实体
 * 
 * <p>设计理念：简化设计，移除外键依赖
 * <p>对应数据库表：private_message
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PrivateMessage implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ========== 消息状态常量 ==========
    
    /** 已送达（对方可见） */
    public static final int STATUS_DELIVERED = 1;
    /** 待回复（防骚扰，仅发送方可见） */
    public static final int STATUS_PENDING = 2;
    /** 被屏蔽（仅发送方可见） */
    public static final int STATUS_BLOCKED = 3;
    /** 已撤回 */
    public static final int STATUS_WITHDRAWN = 4;
    
    // ========== 消息类型常量 ==========
    
    /** 文本消息 */
    public static final int TYPE_TEXT = 1;
    /** 图片消息 */
    public static final int TYPE_IMAGE = 2;
    /** 链接消息 */
    public static final int TYPE_LINK = 3;
    /** 系统消息 */
    public static final int TYPE_SYSTEM = 4;
    
    // ========== 数据库字段 ==========
    
    /** 消息ID */
    private Long id;
    
    /** 发送者ID */
    private Long senderId;
    
    /** 接收者ID */
    private Long receiverId;
    
    /** 消息内容 */
    private String content;
    
    /** 消息类型: 1-文本 2-图片 3-链接 4-系统消息 */
    private Integer messageType;
    
    /** 媒体URL（图片/文件等） */
    private String mediaUrl;
    
    /** 消息状态: 1-已送达 2-待回复 3-被屏蔽 4-已撤回 */
    private Integer status;
    
    /** 是否已读: 0-未读 1-已读 */
    private Integer isRead;
    
    /** 阅读时间 */
    private LocalDateTime readTime;
    
    /** 发送者是否删除 */
    private Integer senderDeleted;
    
    /** 接收者是否删除 */
    private Integer receiverDeleted;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ========== 运行时字段（用于前端显示，非数据库字段） ==========
    
    /** 发送者头像 */
    private String senderAvatar;
    
    /** 发送者昵称 */
    private String senderNickname;
    
    /** 接收者头像 */
    private String receiverAvatar;
    
    /** 接收者昵称 */
    private String receiverNickname;
    
    // ========== 工厂方法 ==========
    
    /** 
     * 创建文本消息
     */
    public static PrivateMessage createText(Long senderId, Long receiverId, String content, int status) {
        return PrivateMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content(content)
                .messageType(TYPE_TEXT)
                .status(status)
                .isRead(0)
                .senderDeleted(0)
                .receiverDeleted(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /** 
     * 创建图片消息
     */
    public static PrivateMessage createImage(Long senderId, Long receiverId, String mediaUrl, int status) {
        return PrivateMessage.builder()
                .senderId(senderId)
                .receiverId(receiverId)
                .content("[图片]")
                .messageType(TYPE_IMAGE)
                .mediaUrl(mediaUrl)
                .status(status)
                .isRead(0)
                .senderDeleted(0)
                .receiverDeleted(0)
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /** 
     * 创建已送达消息
     */
    public static PrivateMessage createDelivered(Long senderId, Long receiverId, String content) {
        return createText(senderId, receiverId, content, STATUS_DELIVERED);
    }
    
    /** 
     * 创建待回复消息
     */
    public static PrivateMessage createPending(Long senderId, Long receiverId, String content) {
        return createText(senderId, receiverId, content, STATUS_PENDING);
    }
    
    /** 
     * 创建被屏蔽消息
     */
    public static PrivateMessage createBlocked(Long senderId, Long receiverId, String content) {
        return createText(senderId, receiverId, content, STATUS_BLOCKED);
    }
    
    // ========== 业务方法 ==========
    
    /** 
     * 标记为已读
     */
    public void markAsRead() {
        this.isRead = 1;
        this.readTime = LocalDateTime.now();
        this.updateTime = LocalDateTime.now();
    }
    
    /** 
     * 撤回消息
     */
    public void withdraw() {
        this.status = STATUS_WITHDRAWN;
        this.content = "[消息已撤回]";
        this.updateTime = LocalDateTime.now();
    }
    
    /** 
     * 发送者删除消息
     */
    public void deleteBySender() {
        this.senderDeleted = 1;
        this.updateTime = LocalDateTime.now();
    }
    
    /** 
     * 接收者删除消息
     */
    public void deleteByReceiver() {
        this.receiverDeleted = 1;
        this.updateTime = LocalDateTime.now();
    }
    
    /** 
     * 是否已送达
     */
    public boolean isDelivered() {
        return status != null && status == STATUS_DELIVERED;
    }
    
    /** 
     * 是否未读
     */
    public boolean isUnread() {
        return isRead == null || isRead == 0;
    }
    
    /** 
     * 获取消息预览
     */
    public String getPreview() {
        if (content == null) return "";
        return content.length() > 100 ? content.substring(0, 100) + "..." : content;
    }
}