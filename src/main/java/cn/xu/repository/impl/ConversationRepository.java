package cn.xu.repository.impl;

import cn.xu.model.entity.Conversation;
import cn.xu.repository.IConversationRepository;
import cn.xu.repository.mapper.ConversationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 对话关系仓储实现类
 * 
 * <p>直接操作Conversation PO，无Entity转换
 *
 * @author xu
 * @since 2025-11-26
 */
@Repository
@RequiredArgsConstructor
public class ConversationRepository implements IConversationRepository {

    private final ConversationMapper conversationMapper;

    @Override
    public Long save(Conversation conversation) {
        if (conversation.getId() == null) {
            conversationMapper.insert(conversation);
            return conversation.getId();
        } else {
            conversationMapper.update(conversation);
            return conversation.getId();
        }
    }

    @Override
    public void update(Conversation conversation) {
        conversationMapper.update(conversation);
    }

    @Override
    public Optional<Conversation> findById(Long id) {
        Conversation conversation = conversationMapper.selectById(id);
        return Optional.ofNullable(conversation);
    }

    @Override
    public Optional<Conversation> findByUserPair(Long userId1, Long userId2) {
        // 确保userId1 <= userId2
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        Conversation conversation = conversationMapper.selectByUserPair(smallerId, largerId);
        return Optional.ofNullable(conversation);
    }

    @Override
    public boolean existsConversation(Long userId1, Long userId2) {
        Long smallerId = Math.min(userId1, userId2);
        Long largerId = Math.max(userId1, userId2);
        return conversationMapper.existsConversation(smallerId, largerId);
    }

    @Override
    public List<Conversation> findByUserId(Long userId, Integer offset, Integer limit) {
        return conversationMapper.selectByUserId(userId, offset, limit);
    }

    @Override
    public void deleteById(Long id) {
        conversationMapper.deleteById(id);
    }
}

