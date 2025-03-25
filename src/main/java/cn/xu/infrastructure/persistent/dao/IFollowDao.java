package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Follow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IFollowDao {
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
}