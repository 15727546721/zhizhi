package cn.xu.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 用户会话实体
 * 
 * <p>设计理念：每个用户独立一条会话记录，查询更简单
 * <p>对应数据库表：user_conversation
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserConversation implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    // ========== 关系类型常量 ==========
    
    /** 陌生人 */
    public static final int RELATION_STRANGER = 0;
    /** 我关注对方 */
    public static final int RELATION_FOLLOWING = 1;
    /** 互相关注 */
    public static final int RELATION_MUTUAL = 2;
    
    // ========== 会话状态常量 ==========
    
    /** 待对方回复（防骚扰） */
    public static final int STATUS_PENDING = 0;
    /** 已建立会话 */
    public static final int STATUS_ESTABLISHED = 1;
    
    // ========== 数据库字段 ==========
    
    /** 会话ID */
    private Long id;
    
    /** 会话所有者ID（当前用户） */
    private Long ownerId;
    
    /** 对方用户ID */
    private Long otherUserId;
    
    /** 对方昵称（冗余） */
    private String otherNickname;
    
    /** 对方头像（冗余） */
    private String otherAvatar;
    
    /** 关系类型: 0-陌生人 1-我关注对方 2-互相关注 */
    private Integer relationType;
    
    /** 会话状态: 0-待对方回复 1-已建立会话 */
    private Integer conversationStatus;
    
    /** 是否是发起人 */
    private Integer isInitiator;
    
    /** 是否被对方屏蔽 */
    private Integer isBlocked;
    
    /** 是否被对方拉黑 */
    private Integer isBlockedBy;
    
    /** 未读消息数 */
    private Integer unreadCount;
    
    /** 最后一条消息内容 */
    private String lastMessage;
    
    /** 最后一条消息发送时间 */
    private LocalDateTime lastMessageTime;
    
    /** 最后一条消息是否是我发送的 */
    private Integer lastMessageIsMine;
    
    /** 是否置顶 */
    private Integer isPinned;
    
    /** 是否静音 */
    private Integer isMuted;
    
    /** 是否删除 */
    private Integer isDeleted;
    
    /** 创建时间 */
    private LocalDateTime createTime;
    
    /** 更新时间 */
    private LocalDateTime updateTime;
    
    // ========== 构造方法 ==========
    
    /** 
     * 创建一个新的会话记录（发送方）
     */
    public static UserConversation createForSender(Long ownerId, Long otherUserId, 
            String otherNickname, String otherAvatar, boolean isMutualFollow) {
        return UserConversation.builder()
                .ownerId(ownerId)
                .otherUserId(otherUserId)
                .otherNickname(otherNickname)
                .otherAvatar(otherAvatar)
                .relationType(isMutualFollow ? RELATION_MUTUAL : RELATION_STRANGER)
                .conversationStatus(STATUS_ESTABLISHED)
                .isInitiator(1)
                .isBlocked(0)
                .isBlockedBy(0)
                .unreadCount(0)
                .lastMessageIsMine(1)
                .isPinned(0)
                .isMuted(0)
                .isDeleted(0)
                .lastMessageTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    /** 
     * 创建一个新的会话记录（接收方）
     */
    public static UserConversation createForReceiver(Long ownerId, Long otherUserId, 
            String otherNickname, String otherAvatar, boolean isMutualFollow) {
        return UserConversation.builder()
                .ownerId(ownerId)
                .otherUserId(otherUserId)
                .otherNickname(otherNickname)
                .otherAvatar(otherAvatar)
                .relationType(isMutualFollow ? RELATION_MUTUAL : RELATION_STRANGER)
                .conversationStatus(STATUS_ESTABLISHED)
                .isInitiator(0)
                .isBlocked(0)
                .isBlockedBy(0)
                .unreadCount(1) // 对方发送消息，未读数+1
                .lastMessageIsMine(0)
                .isPinned(0)
                .isMuted(0)
                .isDeleted(0)
                .lastMessageTime(LocalDateTime.now())
                .createTime(LocalDateTime.now())
                .updateTime(LocalDateTime.now())
                .build();
    }
    
    // ========== 方法 ==========
    
    /** 
     * 更新最后一条消息
     */
    public void updateLastMessage(String message, boolean isMine) {
        this.lastMessage = message.length() > 100 ? message.substring(0, 100) : message;
        this.lastMessageTime = LocalDateTime.now();
        this.lastMessageIsMine = isMine ? 1 : 0;
        this.isDeleted = 0; // 更新最后一条消息时，会话不被删除
        this.updateTime = LocalDateTime.now();
    }
    
    /** 
     * 增加未读消息数
     */
    public void incrementUnread() {
        this.unreadCount = (this.unreadCount == null ? 0 : this.unreadCount) + 1;
        this.updateTime = LocalDateTime.now();
    }
    
    /** 
     * 清除未读消息数
     */
    public void clearUnread() {
        this.unreadCount = 0;
        this.updateTime = LocalDateTime.now();
    }
    
    /** 
     * 是否是互相关注
     */
    public boolean isMutualFollow() {
        return relationType != null && relationType == RELATION_MUTUAL;
    }
    
    /** 
     * 是否是待对方回复
     */
    public boolean isPending() {
        return conversationStatus != null && conversationStatus == STATUS_PENDING;
    }
    
    /** 
     * 建立会话
     */
    public void establish() {
        this.conversationStatus = STATUS_ESTABLISHED;
        this.updateTime = LocalDateTime.now();
    }
}