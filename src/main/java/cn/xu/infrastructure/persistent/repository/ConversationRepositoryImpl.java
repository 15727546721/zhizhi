package cn.xu.infrastructure.persistent.repository;

import cn.xu.domain.message.model.entity.ConversationEntity;
import cn.xu.domain.message.repository.IConversationRepository;
import cn.xu.infrastructure.persistent.converter.ConversationConverter;
import cn.xu.infrastructure.persistent.dao.ConversationMapper;
import cn.xu.infrastructure.persistent.po.Conversation;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 对话关系仓储实现类
 */
@Repository
@RequiredArgsConstructor
public class ConversationRepositoryImpl implements IConversationRepository {
    
    private final ConversationMapper conversationMapper;
    private final ConversationConverter conversationConverter;
    
    @Override
    public Long save(ConversationEntity conversation) {
        Conversation po = conversationConverter.toDataObject(conversation);
        if (po.getId() == null) {
            conversationMapper.insert(po);
            conversation.setId(po.getId());
            return po.getId();
        } else {
            conversationMapper.update(po);
            return po.getId();
        }
    }
    
    @Override
    public void update(ConversationEntity conversation) {
        Conversation po = conversationConverter.toDataObject(conversation);
        conversationMapper.update(po);
    }
    
    @Override
    public Optional<ConversationEntity> findById(Long id) {
        Conversation po = conversationMapper.selectById(id);
        return Optional.ofNullable(conversationConverter.toDomainEntity(po));
    }
    
    @Override
    public Optional<ConversationEntity> findByUserPair(Long userId1, Long userId2) {
        List<Long> key = getConversationKey(userId1, userId2);
        Conversation po = conversationMapper.selectByUserPair(key.get(0), key.get(1));
        return Optional.ofNullable(conversationConverter.toDomainEntity(po));
    }
    
    @Override
    public boolean existsConversation(Long userId1, Long userId2) {
        List<Long> key = getConversationKey(userId1, userId2);
        return conversationMapper.existsConversation(key.get(0), key.get(1));
    }
    
    @Override
    public List<ConversationEntity> findByUserId(Long userId, Integer offset, Integer limit) {
        List<Conversation> conversations = conversationMapper.selectByUserId(userId, offset, limit);
        return conversationConverter.toDomainEntities(conversations);
    }
    
    @Override
    public void deleteById(Long id) {
        conversationMapper.deleteById(id);
    }
    
    /**
     * 获取对话键（确保唯一性）
     */
    private List<Long> getConversationKey(Long userId1, Long userId2) {
        List<Long> key = new ArrayList<>();
        if (userId1 < userId2) {
            key.add(userId1);
            key.add(userId2);
        } else {
            key.add(userId2);
            key.add(userId1);
        }
        return key;
    }
}

