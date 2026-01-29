package cn.xu.model.vo.message;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * 私信VO
 */
@Data
public class PrivateMessageVO {
    
    private Long messageId;
    private Long senderId;
    private String senderName;
    private String senderAvatar;
    private Long receiverId;
    private String receiverName;
    private String receiverAvatar;
    private String content;
    private Integer status; // 1-正常送达，2-待送达
    private Boolean isRead;
    private LocalDateTime createTime;
}
