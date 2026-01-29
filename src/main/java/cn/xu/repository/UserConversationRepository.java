package cn.xu.repository;

import cn.xu.model.entity.UserConversation;

import java.util.List;
import java.util.Optional;

/**
 * 用户会话仓储接口
 */
public interface UserConversationRepository {
    
    /**
     * 保存会话
     */
    Long save(UserConversation conversation);
    
    /**
     * 更新会话
     */
    void update(UserConversation conversation);
    
    /**
     * 根据ID查询
     */
    Optional<UserConversation> findById(Long id);
    
    /**
     * 根据所有者和对方用户查询
     */
    Optional<UserConversation> findByOwnerAndOther(Long ownerId, Long otherUserId);
    
    /**
     * 查询用户的会话列表
     */
    List<UserConversation> findByOwnerId(Long ownerId, int offset, int limit);
    
    /**
     * 查询用户的未删除会话列表
     */
    List<UserConversation> findActiveByOwnerId(Long ownerId, int offset, int limit);
    
    /**
     * 统计用户总未读数
     */
    int countTotalUnread(Long ownerId);
    
    /**
     * 清空指定会话的未读数
     */
    void clearUnreadCount(Long ownerId, Long otherUserId);
    
    /**
     * 增加未读数
     */
    void incrementUnreadCount(Long ownerId, Long otherUserId);
    
    /**
     * 更新最后消息
     */
    void updateLastMessage(Long ownerId, Long otherUserId, String message, boolean isMine);
    
    /**
     * 删除会话（软删除）
     */
    void softDelete(Long ownerId, Long otherUserId);
    
    /**
     * 更新用户信息（冗余字段同步）
     */
    void syncUserInfo(Long userId, String nickname, String avatar);
    
    /**
     * 更新屏蔽状态
     */
    void updateBlockStatus(Long ownerId, Long otherUserId, boolean isBlocked);
    
    /**
     * 更新被屏蔽状态
     */
    void updateBlockedByStatus(Long ownerId, Long otherUserId, boolean isBlockedBy);
    
    /**
     * 更新关系类型
     */
    void updateRelationType(Long ownerId, Long otherUserId, int relationType);
    
    /**
     * 统计用户的活跃会话数量
     */
    int countActiveByOwnerId(Long ownerId);
    
    /**
     * 获取用户总未读数
     */
    int getTotalUnreadCount(Long userId);
    
    /**
     * 获取与某用户的未读数
     */
    int getUnreadCount(Long ownerId, Long otherUserId);
}
