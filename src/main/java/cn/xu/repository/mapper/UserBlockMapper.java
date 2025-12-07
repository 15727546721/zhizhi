package cn.xu.repository.mapper;

import cn.xu.model.entity.UserBlock;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户屏蔽Mapper接口
 * <p>处理用户屏蔽关系的数据库操作</p>
 
 */
@Mapper
public interface UserBlockMapper {
    /**
     * 插入屏蔽关系
     */
    int insert(UserBlock userBlock);
    
    /**
     * 根据ID删除屏蔽关系
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 根据ID查询屏蔽关系
     */
    UserBlock selectById(@Param("id") Long id);
    
    /**
     * 检查用户是否屏蔽了另一个用户
     */
    boolean existsBlock(@Param("userId") Long userId, @Param("blockedUserId") Long blockedUserId);
    
    /**
     * 根据用户ID和被屏蔽用户ID查询屏蔽关系
     */
    UserBlock selectByUserAndBlockedUser(@Param("userId") Long userId, @Param("blockedUserId") Long blockedUserId);
    
    /**
     * 分页查询用户的屏蔽列表
     */
    List<UserBlock> selectByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计用户的屏蔽数量
     */
    int countByUserId(@Param("userId") Long userId);
    
    /**
     * 根据用户ID和被屏蔽用户ID删除屏蔽关系
     */
    int deleteByUserAndBlockedUser(@Param("userId") Long userId, @Param("blockedUserId") Long blockedUserId);
}
