package cn.xu.repository.mapper;

import cn.xu.model.entity.UserConversation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户会话Mapper接口
 * <p>SQL定义在 mybatis/mapper/UserConversationMapper.xml</p>
 
 */
@Mapper
public interface UserConversationMapper {
    
    int insert(UserConversation conversation);
    
    int update(UserConversation conversation);
    
    UserConversation selectById(Long id);
    
    UserConversation selectByOwnerAndOther(@Param("ownerId") Long ownerId, 
                                           @Param("otherUserId") Long otherUserId);
    
    List<UserConversation> selectActiveByOwnerId(@Param("ownerId") Long ownerId,
                                                  @Param("offset") int offset,
                                                  @Param("limit") int limit);
    
    int countTotalUnread(Long ownerId);
    
    int clearUnreadCount(@Param("ownerId") Long ownerId, @Param("otherUserId") Long otherUserId);
    
    int incrementUnreadCount(@Param("ownerId") Long ownerId, @Param("otherUserId") Long otherUserId);
    
    int updateLastMessage(@Param("ownerId") Long ownerId,
                          @Param("otherUserId") Long otherUserId,
                          @Param("message") String message,
                          @Param("isMine") int isMine);
    
    int softDelete(@Param("ownerId") Long ownerId, @Param("otherUserId") Long otherUserId);
    
    int syncUserInfo(@Param("userId") Long userId, 
                     @Param("nickname") String nickname, 
                     @Param("avatar") String avatar);
    
    int updateBlockStatus(@Param("ownerId") Long ownerId,
                          @Param("otherUserId") Long otherUserId,
                          @Param("isBlocked") int isBlocked);
    
    int updateBlockedByStatus(@Param("ownerId") Long ownerId,
                              @Param("otherUserId") Long otherUserId,
                              @Param("isBlockedBy") int isBlockedBy);
    
    int updateRelationType(@Param("ownerId") Long ownerId,
                           @Param("otherUserId") Long otherUserId,
                           @Param("relationType") int relationType);
    
    /**
     * 统计用户的活跃会话数量
     */
    int countActiveByOwnerId(Long ownerId);
}
