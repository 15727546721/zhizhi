package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.message.model.entity.FirstMessageEntity;
import cn.xu.domain.message.repository.IFirstMessageRepository;
import cn.xu.infrastructure.persistent.converter.FirstMessageConverter;
import cn.xu.infrastructure.persistent.dao.FirstMessageMapper;
import cn.xu.infrastructure.persistent.po.FirstMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 首次消息记录仓储实现类
 */
@Repository
@RequiredArgsConstructor
public class FirstMessageRepositoryImpl implements IFirstMessageRepository {
    
    private final FirstMessageMapper firstMessageMapper;
    private final FirstMessageConverter firstMessageConverter;
    
    @Override
    public Long save(FirstMessageEntity firstMessage) {
        FirstMessage po = firstMessageConverter.toDataObject(firstMessage);
        if (po.getId() == null) {
            int result = firstMessageMapper.insert(po);
            if (result <= 0 || po.getId() == null) {
                throw new RuntimeException("首次消息记录插入失败 - 发送者: " + firstMessage.getSenderId() + ", 接收者: " + firstMessage.getReceiverId());
            }
            firstMessage.setId(po.getId());
            return po.getId();
        } else {
            int result = firstMessageMapper.update(po);
            if (result <= 0) {
                throw new RuntimeException("首次消息记录更新失败 - ID: " + po.getId());
            }
            return po.getId();
        }
    }
    
    @Override
    public void update(FirstMessageEntity firstMessage) {
        FirstMessage po = firstMessageConverter.toDataObject(firstMessage);
        firstMessageMapper.update(po);
    }
    
    @Override
    public Optional<FirstMessageEntity> findBySenderAndReceiver(Long senderId, Long receiverId) {
        FirstMessage po = firstMessageMapper.selectBySenderAndReceiver(senderId, receiverId);
        return Optional.ofNullable(firstMessageConverter.toDomainEntity(po));
    }
    
    @Override
    public void updateHasReplied(Long senderId, Long receiverId, boolean hasReplied) {
        firstMessageMapper.updateHasReplied(senderId, receiverId, hasReplied ? 1 : 0);
    }
    
    @Override
    public boolean existsBySenderAndReceiver(Long senderId, Long receiverId) {
        return firstMessageMapper.existsBySenderAndReceiver(senderId, receiverId);
    }
}

