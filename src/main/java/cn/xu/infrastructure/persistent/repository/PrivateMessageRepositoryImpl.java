package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.message.model.aggregate.PrivateMessageAggregate;
import cn.xu.domain.message.model.valueobject.MessageStatus;
import cn.xu.domain.message.repository.IPrivateMessageRepository;
import cn.xu.infrastructure.persistent.converter.PrivateMessageConverter;
import cn.xu.infrastructure.persistent.dao.MessageMapper;
import cn.xu.infrastructure.persistent.po.Message;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 私信聚合根仓储实现类
 * 通过PrivateMessageConverter进行领域实体与持久化对象的转换，遵循DDD防腐层模式
 */
@Repository
@RequiredArgsConstructor
public class PrivateMessageRepositoryImpl implements IPrivateMessageRepository {
    
    private final MessageMapper messageMapper;
    private final PrivateMessageConverter privateMessageConverter;
    
    @Override
    public Long save(PrivateMessageAggregate aggregate) {
        Message po = privateMessageConverter.toDataObject(aggregate);
        if (po.getId() == null) {
            messageMapper.insert(po);
            aggregate.getPrivateMessage().setId(po.getId());
            return po.getId();
        } else {
            messageMapper.update(po);
            return po.getId();
        }
    }
    
    @Override
    public void update(PrivateMessageAggregate aggregate) {
        Message po = privateMessageConverter.toDataObject(aggregate);
        messageMapper.update(po);
    }
    
    @Override
    public Optional<PrivateMessageAggregate> findById(Long id) {
        Message po = messageMapper.selectById(id);
        if (po == null || po.getType() != 2) {
            return Optional.empty();
        }
        return Optional.ofNullable(privateMessageConverter.toDomainAggregate(po));
    }
    
    @Override
    public List<PrivateMessageAggregate> findMessagesByReceiver(Long receiverId, Long senderId, Integer offset, Integer limit) {
        // 接收者只能看到status=1的消息
        List<Message> messages = messageMapper.selectPrivateMessagesByReceiver(
                receiverId, senderId, MessageStatus.DELIVERED.getCode(), offset, limit);
        return privateMessageConverter.toDomainAggregates(messages);
    }
    
    @Override
    public List<PrivateMessageAggregate> findMessagesBySender(Long senderId, Long receiverId, Integer offset, Integer limit) {
        List<Message> messages = messageMapper.selectPrivateMessagesBySender(senderId, receiverId, offset, limit);
        return privateMessageConverter.toDomainAggregates(messages);
    }
    
    @Override
    public List<PrivateMessageAggregate> findMessagesBetweenUsers(Long userId1, Long userId2, Integer offset, Integer limit) {
        List<Message> messages = messageMapper.selectPrivateMessagesBetweenUsers(userId1, userId2, offset, limit);
        List<PrivateMessageAggregate> aggregates = privateMessageConverter.toDomainAggregates(messages);
        // 反转列表，使最新的消息在最后（前端显示需要）
        Collections.reverse(aggregates);
        return aggregates;
    }
    
    @Override
    public long countUnreadMessages(Long receiverId, Long senderId) {
        return messageMapper.countUnreadPrivateMessages(receiverId, senderId);
    }
    
    @Override
    public void markAsRead(Long messageId, Long receiverId) {
        messageMapper.markAsRead(messageId);
    }
    
    @Override
    public void markAsReadBySender(Long receiverId, Long senderId) {
        messageMapper.markPrivateMessagesAsRead(receiverId, senderId);
    }
    
    @Override
    public void updateStatusBySenderAndReceiver(Long senderId, Long receiverId, MessageStatus oldStatus, MessageStatus newStatus) {
        messageMapper.updateMessageStatus(senderId, receiverId, oldStatus.getCode(), newStatus.getCode());
    }
    
    @Override
    public List<PrivateMessageAggregate> findLastMessagesByUser(Long userId, Integer offset, Integer limit) {
        List<Message> messages = messageMapper.selectLastPrivateMessagesByUser(userId, offset, limit);
        return privateMessageConverter.toDomainAggregates(messages);
    }
}

