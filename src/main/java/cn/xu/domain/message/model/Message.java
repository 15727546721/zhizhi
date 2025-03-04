package cn.xu.domain.message.model;

import lombok.Data;
import lombok.experimental.Accessors;

import java.time.LocalDateTime;

/**
 * 消息领域模型
 */
@Data
@Accessors(chain = true)
public class Message {
    /**
     * 消息ID
     */
    private Long id;

    /**
     * 消息类型：1-系统消息 2-私信消息 3-点赞消息 4-收藏消息 5-评论消息 6-关注消息
     */
    private Integer type;

    /**
     * 发送者ID
     */
    private Long senderId;

    /**
     * 接收者ID
     */
    private Long receiverId;

    /**
     * 消息标题
     */
    private String title;

    /**
     * 消息内容
     */
    private String content;

    /**
     * 目标ID
     */
    private Long targetId;

    /**
     * 是否已读：0-未读 1-已读
     */
    private Boolean isRead;

    /**
     * 创建时间
     */
    private LocalDateTime createTime;

    /**
     * 更新时间
     */
    private LocalDateTime updateTime;

    /**
     * 标记消息为已读
     */
    public void markAsRead() {
        this.isRead = true;
        this.updateTime = LocalDateTime.now();
    }

    /**
     * 检查消息是否未读
     */
    public boolean isUnread() {
        return !isRead;
    }

    /**
     * 检查用户是否是消息的参与者
     */
    public boolean isParticipant(Long userId) {
        return userId.equals(senderId) || userId.equals(receiverId);
    }

    /**
     * 获取对话的另一方用户ID
     */
    public Long getOtherParticipant(Long userId) {
        return userId.equals(senderId) ? receiverId : senderId;
    }
} 