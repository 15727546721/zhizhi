package cn.xu.repository.mapper;

import cn.xu.model.entity.Conversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ConversationMapper {
    /**
     * 插入对话关系
     */
    int insert(Conversation conversation);
    
    /**
     * 更新对话关系
     */
    int update(Conversation conversation);
    
    /**
     * 根据ID查询对话关系
     */
    Conversation selectById(@Param("id") Long id);
    
    /**
     * 根据两个用户ID查询对话关系
     */
    Conversation selectByUserPair(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * 检查对话关系是否存在
     */
    boolean existsConversation(@Param("userId1") Long userId1, @Param("userId2") Long userId2);
    
    /**
     * 分页查询用户的所有对话列表
     */
    List<Conversation> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 根据ID删除对话关系
     */
    int deleteById(@Param("id") Long id);
}

