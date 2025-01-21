package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.model.entity.MessageType;
import cn.xu.domain.message.repository.IMessageRepository;
import cn.xu.infrastructure.persistent.dao.IMessageDao;
import cn.xu.infrastructure.persistent.po.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class MessageRepository implements IMessageRepository {

    private final IMessageDao messageDao;

    @Override
    public void save(MessageEntity entity) {
        Message po = convertToPO(entity);
        if (po.getId() == null) {
            messageDao.insert(po);
        } else {
            messageDao.update(po);
        }
    }

    @Override
    public MessageEntity findById(Long id) {
        Message po = messageDao.selectById(id);
        return po != null ? convertToEntity(po) : null;
    }

    @Override
    public List<MessageEntity> findByUserId(Long userId, MessageType type, int offset, int limit) {
        List<Message> messages = messageDao.selectByUserId(userId, type != null ? type.getCode() : null, offset, limit);
        return messages.stream()
                .map(this::convertToEntity)
                .collect(Collectors.toList());
    }

    @Override
    public long countUnreadMessages(Long userId) {
        return messageDao.countUnreadMessages(userId);
    }

    @Override
    public void markAsRead(Long messageId) {
        messageDao.markAsRead(messageId);
    }

    @Override
    public void markAllAsRead(Long userId) {
        messageDao.markAllAsRead(userId);
    }

    @Override
    public void deleteById(Long id) {
        messageDao.deleteById(id);
    }

    @Override
    public boolean exists(Long id) {
        return messageDao.exists(id);
    }

    private Message convertToPO(MessageEntity entity) {
        return Message.builder()
                .id(entity.getId())
                .type(entity.getType().getCode())
                .senderId(entity.getSenderId())
                .receiverId(entity.getReceiverId())
                .title(entity.getTitle())
                .content(entity.getContent())
                .targetId(entity.getTargetId())
                .isRead(entity.getIsRead() ? 1 : 0)
                .createTime(entity.getCreateTime())
                .updateTime(entity.getUpdateTime())
                .build();
    }

    private MessageEntity convertToEntity(Message po) {
        return MessageEntity.builder()
                .id(po.getId())
                .type(MessageType.fromCode(po.getType()))
                .senderId(po.getSenderId())
                .receiverId(po.getReceiverId())
                .title(po.getTitle())
                .content(po.getContent())
                .targetId(po.getTargetId())
                .isRead(po.getIsRead() == 1)
                .createTime(po.getCreateTime())
                .updateTime(po.getUpdateTime())
                .build();
    }
} 