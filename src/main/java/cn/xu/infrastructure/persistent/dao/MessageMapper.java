package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {
    void insert(Message message);
    
    void update(Message message);
    
    Message selectById(@Param("id") Long id);
    
    List<Message> selectByUserId(@Param("userId") Long userId, @Param("type") Integer type,
                                @Param("offset") int offset, @Param("limit") int limit);
    
    long countUnreadMessages(@Param("userId") Long userId);
    
    void markAsRead(@Param("messageId") Long messageId);
    
    void markAllAsRead(@Param("userId") Long userId);
    
    void deleteById(@Param("id") Long id);
    
    boolean exists(@Param("id") Long id);
    
    // ==================== 私信相关查询 ====================
    
    /**
     * 查询接收者的私信（只查询status=1的消息）
     */
    List<Message> selectPrivateMessagesByReceiver(@Param("receiverId") Long receiverId,
                                                   @Param("senderId") Long senderId,
                                                   @Param("status") Integer status,
                                                   @Param("offset") int offset,
                                                   @Param("limit") int limit);
    
    /**
     * 查询发送者的私信（查询所有消息，包括status=2）
     */
    List<Message> selectPrivateMessagesBySender(@Param("senderId") Long senderId,
                                                 @Param("receiverId") Long receiverId,
                                                 @Param("offset") int offset,
                                                 @Param("limit") int limit);
    
    /**
     * 查询两个用户之间的所有私信（合并发送和接收）
     */
    List<Message> selectPrivateMessagesBetweenUsers(@Param("userId1") Long userId1,
                                                     @Param("userId2") Long userId2,
                                                     @Param("offset") int offset,
                                                     @Param("limit") int limit);
    
    /**
     * 统计未读私信数（只统计status=1的未读消息）
     */
    long countUnreadPrivateMessages(@Param("receiverId") Long receiverId,
                                    @Param("senderId") Long senderId);
    
    /**
     * 标记私信为已读
     */
    void markPrivateMessagesAsRead(@Param("receiverId") Long receiverId,
                                   @Param("senderId") Long senderId);
    
    /**
     * 更新消息状态
     */
    void updateMessageStatus(@Param("senderId") Long senderId,
                            @Param("receiverId") Long receiverId,
                            @Param("oldStatus") Integer oldStatus,
                            @Param("newStatus") Integer newStatus);
    
    /**
     * 查询用户的所有对话的最后一条消息
     */
    List<Message> selectLastPrivateMessagesByUser(@Param("userId") Long userId,
                                                   @Param("offset") int offset,
                                                   @Param("limit") int limit);
}
