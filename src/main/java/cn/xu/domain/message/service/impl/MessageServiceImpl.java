package cn.xu.domain.message.service.impl;

import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.model.entity.MessageType;
import cn.xu.domain.message.repository.IMessageRepository;
import cn.xu.domain.message.service.IMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements IMessageService {

    private final IMessageRepository messageRepository;

    @Override
    @Transactional
    public void sendMessage(MessageEntity message) {
        message.validate();
        message.setCreateTime(new Date());
        message.setUpdateTime(new Date());
        message.setIsRead(false);
        messageRepository.save(message);
    }

    @Override
    @Transactional
    public void sendSystemMessage(String title, String content, Long targetId) {
        MessageEntity message = MessageEntity.builder()
                .type(MessageType.SYSTEM)
                .title(title)
                .content(content)
                .targetId(targetId)
                .build();
        sendMessage(message);
    }

    @Override
    @Transactional
    public void markAsRead(Long messageId) {
        messageRepository.markAsRead(messageId);
    }

    @Override
    @Transactional
    public void markAllAsRead(Long userId) {
        messageRepository.markAllAsRead(userId);
    }

    @Override
    public long getUnreadCount(Long userId) {
        return messageRepository.countUnreadMessages(userId);
    }

    @Override
    public List<MessageEntity> getUserMessages(Long userId, MessageType type, int page, int size) {
        int offset = (page - 1) * size;
        return messageRepository.findByUserId(userId, type, offset, size);
    }

    @Override
    @Transactional
    public void deleteMessage(Long messageId, Long userId) {
        MessageEntity message = messageRepository.findById(messageId);
        if (message != null && message.getReceiverId().equals(userId)) {
            messageRepository.deleteById(messageId);
        }
    }
} 