package cn.xu.repository;

import cn.xu.model.entity.PrivateMessage;

import java.util.List;
import java.util.Optional;

/**
 * 私信消息仓储接口
 */
public interface PrivateMessageRepository {
    
    /**
     * 保存消息
     */
    Long save(PrivateMessage message);
    
    /**
     * 更新消息
     */
    void update(PrivateMessage message);
    
    /**
     * 根据ID查询
     */
    Optional<PrivateMessage> findById(Long id);
    
    /**
     * 查询两个用户之间的消息列表
     */
    List<PrivateMessage> findMessagesBetweenUsers(Long userId1, Long userId2, Long viewerId, int offset, int limit);
    
    /**
     * 统计两个用户之间的未读消息数
     */
    int countUnread(Long receiverId, Long senderId);
    
    /**
     * 标记消息为已读
     */
    void markAsRead(Long receiverId, Long senderId);
    
    /**
     * 撤回消息
     */
    void withdraw(Long messageId, Long senderId);
    
    /**
     * 检查是否有发送过消息
     */
    boolean hasMessageFrom(Long senderId, Long receiverId);
    
    /**
     * 检查对方是否回复过
     */
    boolean hasReplyFrom(Long senderId, Long receiverId);
    
    /**
     * 删除消息（发送方软删除）
     */
    void deleteBySender(Long messageId, Long senderId);
    
    /**
     * 删除消息（接收方软删除）
     */
    void deleteByReceiver(Long messageId, Long receiverId);
    
    /**
     * 删除消息（根据用户角色自动判断）
     */
    void softDeleteForUser(Long messageId, Long userId);
}
