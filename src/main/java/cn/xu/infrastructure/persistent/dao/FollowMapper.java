package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface FollowMapper {
    /**
     * 新增关注关系
     */
    int insert(Follow userFollow);

    /**
     * 更新关注状态
     */
    int updateStatus(@Param("followerId") Long followerId, @Param("followedId") Long followedId, @Param("status") Integer status);

    /**
     * 获取关注关系
     */
    Follow getByFollowerAndFollowed(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    /**
     * 获取用户的关注列表
     */
    List<Follow> listByFollowerId(@Param("followerId") Long followerId);

    /**
     * 获取用户的粉丝列表
     */
    List<Follow> listByFollowedId(@Param("followedId") Long followedId);

    /**
     * 统计用户关注数
     */
    int countFollowing(@Param("followerId") Long followerId);

    /**
     * 统计用户粉丝数
     */
    int countFollowers(@Param("followedId") Long followedId);

    /**
     * 根据followerId和followedId获取关注状态
     *
     * @param followerId
     * @param followedId
     * @return
     */
    Integer findStatus(@Param("followerId") long followerId, @Param("followedId") long followedId);
    
    /**
     * 根据ID查询关注关系
     * 
     * @param id 关注关系ID
     * @return 关注关系
     */
    Follow findById(@Param("id") Long id);
    
    /**
     * 根据ID删除关注关系
     * 
     * @param id 关注关系ID
     * @return 删除记录数
     */
    int deleteById(@Param("id") Long id);
    
    /**
     * 批量删除关注关系
     * 
     * @param ids 关注关系ID列表
     * @return 删除记录数
     */
    int deleteByIds(@Param("ids") List<Long> ids);
    
    /**
     * 分页查询用户的关注列表
     * 
     * @param followerId 关注者ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 关注关系列表
     */
    List<Follow> listFollowingByPage(@Param("followerId") Long followerId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 分页查询用户的粉丝列表
     * 
     * @param followedId 被关注者ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 关注关系列表
     */
    List<Follow> listFollowersByPage(@Param("followedId") Long followedId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 查询互相关注的用户ID列表
     * 
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 互相关注的用户ID列表
     */
    List<Long> findMutualFollows(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
}