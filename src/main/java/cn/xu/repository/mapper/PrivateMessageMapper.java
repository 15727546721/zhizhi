package cn.xu.repository.mapper;

import cn.xu.model.entity.PrivateMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 私信消息Mapper接口
 * <p>处理私信消息的数据库操作</p>
 
 */
@Mapper
public interface PrivateMessageMapper {
    
    int insert(PrivateMessage message);
    
    int update(PrivateMessage message);
    
    PrivateMessage selectById(Long id);
    
    /**
     * 查询两个用户之间的消息
     * <p>根据查看者过滤已删除的消息</p>
     */
    List<PrivateMessage> selectMessagesBetweenUsers(@Param("userId1") Long userId1,
                                                       @Param("userId2") Long userId2,
                                                       @Param("viewerId") Long viewerId,
                                                       @Param("offset") int offset,
                                                       @Param("limit") int limit);
    
    int countUnread(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
    
    int markAsRead(@Param("receiverId") Long receiverId, @Param("senderId") Long senderId);
    
    int withdraw(@Param("messageId") Long messageId, @Param("senderId") Long senderId);
    
    boolean hasMessageFrom(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
    
    boolean hasReplyFrom(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
    
    int deleteBySender(@Param("messageId") Long messageId, @Param("senderId") Long senderId);
    
    int deleteByReceiver(@Param("messageId") Long messageId, @Param("receiverId") Long receiverId);
    
    /**
     * 物理删除双方都已删除的消息
     * @return 删除的记录数
     */
    int deleteBothDeletedMessages();
    
    /**
     * 物理删除某会话的所有消息
     */
    int deleteMessagesByConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * 将打招呼消息状态从 PENDING 更新为 DELIVERED
     * @param senderId 原发送者ID
     * @param receiverId 原接收者ID（现在回复的人）
     */
    int updatePendingToDelivered(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
    
    /**
     * 搜索消息内容
     */
    List<PrivateMessage> searchMessages(@Param("userId") Long userId, 
                                        @Param("keyword") String keyword,
                                        @Param("offset") int offset, 
                                        @Param("limit") int limit);
}
