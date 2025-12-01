package cn.xu.repository.mapper;

import cn.xu.model.entity.UserInterestedTag;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户关注标签Mapper
 * 
 * @author zhizhi
 * @version 1.0
 */
@Mapper
public interface UserInterestedTagMapper {
    
    /**
     * 插入用户关注标签
     * 
     * @param userInterestedTag 用户关注标签
     * @return 影响行数
     */
    int insert(UserInterestedTag userInterestedTag);
    
    /**
     * 删除用户关注标签
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 影响行数
     */
    int delete(@Param("userId") Long userId, @Param("tagId") Long tagId);
    
    /**
     * 查询用户是否关注某标签
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 用户关注标签对象，不存在返回null
     */
    UserInterestedTag findByUserIdAndTagId(@Param("userId") Long userId, @Param("tagId") Long tagId);
    
    /**
     * 查询用户关注的所有标签ID列表
     * 
     * @param userId 用户ID
     * @return 标签ID列表
     */
    List<Long> findTagIdsByUserId(@Param("userId") Long userId);
    
    /**
     * 查询用户关注的标签列表（分页）
     * 
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 用户关注标签列表
     */
    List<UserInterestedTag> findByUserIdWithPage(@Param("userId") Long userId, 
                                                   @Param("offset") int offset, 
                                                   @Param("limit") int limit);
    
    /**
     * 查询关注该标签的用户ID列表
     * 
     * @param tagId 标签ID
     * @return 用户ID列表
     */
    List<Long> findUserIdsByTagId(@Param("tagId") Long tagId);
    
    /**
     * 查询关注该标签的用户列表（分页）
     * 
     * @param tagId 标签ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 用户关注标签列表
     */
    List<UserInterestedTag> findByTagIdWithPage(@Param("tagId") Long tagId, 
                                                  @Param("offset") int offset, 
                                                  @Param("limit") int limit);
    
    /**
     * 统计用户关注的标签数量
     * 
     * @param userId 用户ID
     * @return 关注的标签数量
     */
    int countByUserId(@Param("userId") Long userId);
    
    /**
     * 统计关注该标签的用户数量
     * 
     * @param tagId 标签ID
     * @return 关注的用户数量
     */
    int countByTagId(@Param("tagId") Long tagId);
    
    /**
     * 批量删除用户关注的所有标签
     * 
     * @param userId 用户ID
     * @return 影响行数
     */
    int deleteByUserId(@Param("userId") Long userId);
    
    /**
     * 批量删除关注某标签的所有记录
     * 
     * @param tagId 标签ID
     * @return 影响行数
     */
    int deleteByTagId(@Param("tagId") Long tagId);
    
    /**
     * 查询用户是否关注某标签（返回布尔值）
     * 
     * @param userId 用户ID
     * @param tagId 标签ID
     * @return 是否关注
     */
    boolean exists(@Param("userId") Long userId, @Param("tagId") Long tagId);
}
