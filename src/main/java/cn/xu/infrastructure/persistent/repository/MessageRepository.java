package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.message.model.entity.MessageEntity;
import cn.xu.domain.message.model.entity.MessageType;
import cn.xu.domain.message.repository.IMessageRepository;
import cn.xu.infrastructure.persistent.converter.MessageConverter;
import cn.xu.infrastructure.persistent.dao.MessageMapper;
import cn.xu.infrastructure.persistent.po.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 消息仓储实现类
 * 通过MessageConverter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 * 
 * @author xu
 */
@Repository
@RequiredArgsConstructor
public class MessageRepository implements IMessageRepository {

    private final MessageMapper messageDao;
    private final MessageConverter messageConverter;

    @Override
    public void save(MessageEntity entity) {
        Message po = messageConverter.toDataObject(entity);
        if (po.getId() == null) {
            messageDao.insert(po);
            entity.setId(po.getId());
        } else {
            messageDao.update(po);
        }
    }

    @Override
    public MessageEntity findById(Long id) {
        Message po = messageDao.selectById(id);
        return messageConverter.toDomainEntity(po);
    }

    @Override
    public List<MessageEntity> findByUserId(Long userId, MessageType type, int offset, int limit) {
        List<Message> messages = messageDao.selectByUserId(userId, type != null ? type.getCode() : null, offset, limit);
        return messageConverter.toDomainEntities(messages);
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


} 