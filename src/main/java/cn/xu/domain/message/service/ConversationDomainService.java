package cn.xu.domain.message.service;

import cn.xu.domain.message.model.entity.ConversationEntity;
import cn.xu.domain.message.repository.IConversationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * 对话关系领域服务
 * 负责处理对话关系的核心业务逻辑
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ConversationDomainService {
    
    private final IConversationRepository conversationRepository;
    
    /**
     * 获取用户的对话列表
     */
    public List<ConversationEntity> getConversationList(Long userId, Integer pageNo, Integer pageSize) {
        try {
            int offset = (pageNo - 1) * pageSize;
            return conversationRepository.findByUserId(userId, offset, pageSize);
        } catch (Exception e) {
            log.error("[对话关系领域服务] 获取对话列表失败 - 用户: {}", userId, e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 获取两个用户之间的对话关系
     */
    public Optional<ConversationEntity> getConversation(Long userId1, Long userId2) {
        try {
            List<Long> key = getConversationKey(userId1, userId2);
            return conversationRepository.findByUserPair(key.get(0), key.get(1));
        } catch (Exception e) {
            log.error("[对话关系领域服务] 获取对话关系失败 - 用户1: {}, 用户2: {}", userId1, userId2, e);
            return Optional.empty();
        }
    }
    
    /**
     * 检查对话关系是否存在
     */
    public boolean existsConversation(Long userId1, Long userId2) {
        try {
            List<Long> key = getConversationKey(userId1, userId2);
            return conversationRepository.existsConversation(key.get(0), key.get(1));
        } catch (Exception e) {
            log.error("[对话关系领域服务] 检查对话关系失败 - 用户1: {}, 用户2: {}", userId1, userId2, e);
            return false;
        }
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

