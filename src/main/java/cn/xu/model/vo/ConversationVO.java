package cn.xu.model.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 对话VO
 */
@Data
public class ConversationVO {
    
    private Long userId;
    private String userName;
    private String userAvatar;
    private String lastMessage;
    private LocalDateTime lastMessageTime;
    private Long unreadCount;
    private Integer lastMessageStatus; // 最后一条消息的状态（1-正常，2-待送达）
}

