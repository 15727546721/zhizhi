package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IMessageDao {
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
}
