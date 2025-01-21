package cn.xu.domain.message.service;

import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.model.entity.MessageType;

import java.util.List;

public interface IMessageService {
    /**
     * 发送消息
     */
    void sendMessage(MessageEntity message);

    /**
     * 发送系统消息
     */
    void sendSystemMessage(String title, String content, Long targetId);

    /**
     * 标记消息为已读
     */
    void markAsRead(Long messageId);

    /**
     * 批量标记消息为已读
     */
    void markAllAsRead(Long userId);

    /**
     * 获取用户未读消息数量
     */
    long getUnreadCount(Long userId);

    /**
     * 获取用户消息列表
     */
    List<MessageEntity> getUserMessages(Long userId, MessageType type, int page, int size);

    /**
     * 删除消息
     */
    void deleteMessage(Long messageId, Long userId);
} 