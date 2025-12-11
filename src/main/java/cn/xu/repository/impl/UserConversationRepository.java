package cn.xu.repository.impl;

import cn.xu.model.entity.UserConversation;
import cn.xu.repository.IUserConversationRepository;
import cn.xu.repository.mapper.UserConversationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户会话仓储实现
 */
@Repository
@RequiredArgsConstructor
public class UserConversationRepository implements IUserConversationRepository {
    
    private final UserConversationMapper mapper;
    
    @Override
    public Long save(UserConversation conversation) {
        mapper.insert(conversation);
        return conversation.getId();
    }
    
    @Override
    public void update(UserConversation conversation) {
        mapper.update(conversation);
    }
    
    @Override
    public Optional<UserConversation> findById(Long id) {
        return Optional.ofNullable(mapper.selectById(id));
    }
    
    @Override
    public Optional<UserConversation> findByOwnerAndOther(Long ownerId, Long otherUserId) {
        return Optional.ofNullable(mapper.selectByOwnerAndOther(ownerId, otherUserId));
    }
    
    @Override
    public List<UserConversation> findByOwnerId(Long ownerId, int offset, int limit) {
        return mapper.selectActiveByOwnerId(ownerId, offset, limit);
    }
    
    @Override
    public List<UserConversation> findActiveByOwnerId(Long ownerId, int offset, int limit) {
        return mapper.selectActiveByOwnerId(ownerId, offset, limit);
    }
    
    @Override
    public int countTotalUnread(Long ownerId) {
        return mapper.countTotalUnread(ownerId);
    }
    
    @Override
    public void clearUnreadCount(Long ownerId, Long otherUserId) {
        mapper.clearUnreadCount(ownerId, otherUserId);
    }
    
    @Override
    public void incrementUnreadCount(Long ownerId, Long otherUserId) {
        mapper.incrementUnreadCount(ownerId, otherUserId);
    }
    
    @Override
    public void updateLastMessage(Long ownerId, Long otherUserId, String message, boolean isMine) {
        mapper.updateLastMessage(ownerId, otherUserId, message, isMine ? 1 : 0);
    }
    
    @Override
    public void softDelete(Long ownerId, Long otherUserId) {
        mapper.softDelete(ownerId, otherUserId);
    }
    
    @Override
    public void syncUserInfo(Long userId, String nickname, String avatar) {
        mapper.syncUserInfo(userId, nickname, avatar);
    }
    
    @Override
    public void updateBlockStatus(Long ownerId, Long otherUserId, boolean isBlocked) {
        mapper.updateBlockStatus(ownerId, otherUserId, isBlocked ? 1 : 0);
    }
    
    @Override
    public void updateBlockedByStatus(Long ownerId, Long otherUserId, boolean isBlockedBy) {
        mapper.updateBlockedByStatus(ownerId, otherUserId, isBlockedBy ? 1 : 0);
    }
    
    @Override
    public void updateRelationType(Long ownerId, Long otherUserId, int relationType) {
        mapper.updateRelationType(ownerId, otherUserId, relationType);
    }
    
    @Override
    public int getTotalUnreadCount(Long userId) {
        return mapper.countTotalUnread(userId);
    }
    
    @Override
    public int getUnreadCount(Long ownerId, Long otherUserId) {
        UserConversation conv = mapper.selectByOwnerAndOther(ownerId, otherUserId);
        return conv != null ? conv.getUnreadCount() : 0;
    }
}
