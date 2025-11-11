package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.FirstMessage;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface FirstMessageMapper {
    /**
     * 插入首次消息记录
     */
    int insert(FirstMessage firstMessage);
    
    /**
     * 更新首次消息记录
     */
    int update(FirstMessage firstMessage);
    
    /**
     * 根据发送者和接收者查询首次消息记录
     */
    FirstMessage selectBySenderAndReceiver(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
    
    /**
     * 更新回复状态
     */
    int updateHasReplied(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId, @Param("hasReplied") Integer hasReplied);
    
    /**
     * 检查是否存在首次消息记录
     */
    boolean existsBySenderAndReceiver(@Param("senderId") Long senderId, @Param("receiverId") Long receiverId);
}

