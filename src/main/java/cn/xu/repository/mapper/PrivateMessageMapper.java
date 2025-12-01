package cn.xu.repository.mapper;

import cn.xu.model.entity.PrivateMessage;
import cn.xu.repository.impl.PrivateMessageRepository.UnreadCountDto;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私信消息 Mapper
 * 
 * @author xu
 * @since 2025-11-28
 */
@Mapper
public interface PrivateMessageMapper {
    
    /**
     * 插入私信
     */
    int insert(PrivateMessage message);
    
    /**
     * 更新私信
     */
    int update(PrivateMessage message);
    
    /**
     * 根据ID查询
     */
    PrivateMessage selectById(@Param("id") Long id);
    
    /**
     * 查询两个用户之间的私信列表
     */
    List<PrivateMessage> selectPrivateMessagesBetweenUsers(
            @Param("userId1") Long userId1,
            @Param("userId2") Long userId2,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 查询接收者收到的私信（只显示已送达的）
     */
    List<PrivateMessage> selectPrivateMessagesByReceiver(
            @Param("receiverId") Long receiverId,
            @Param("senderId") Long senderId,
            @Param("status") int status,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 查询发送者发送的私信
     */
    List<PrivateMessage> selectPrivateMessagesBySender(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 查询用户的最近私信列表（用于对话列表）
     */
    List<PrivateMessage> selectLastPrivateMessagesByUser(
            @Param("userId") Long userId,
            @Param("offset") int offset,
            @Param("limit") int limit);
    
    /**
     * 统计未读私信数量
     */
    Long countUnreadPrivateMessages(
            @Param("receiverId") Long receiverId,
            @Param("senderId") Long senderId);
    
    /**
     * 标记单条消息为已读
     */
    int markAsRead(@Param("messageId") Long messageId);
    
    /**
     * 批量标记私信为已读
     */
    int markPrivateMessagesAsRead(
            @Param("receiverId") Long receiverId,
            @Param("senderId") Long senderId);
    
    /**
     * 更新消息状态
     */
    int updateMessageStatus(
            @Param("senderId") Long senderId,
            @Param("receiverId") Long receiverId,
            @Param("oldStatus") int oldStatus,
            @Param("newStatus") int newStatus);
    
    /**
     * 批量查询对话的最后一条消息
     */
    List<PrivateMessage> selectLastMessagesForConversations(
            @Param("currentUserId") Long currentUserId,
            @Param("otherUserIds") List<Long> otherUserIds);
    
    /**
     * 批量统计未读消息数
     */
    List<UnreadCountDto> countUnreadMessagesForConversations(
            @Param("currentUserId") Long currentUserId,
            @Param("otherUserIds") List<Long> otherUserIds);
}
