package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.UserFollow;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface IUserFollowDao {
    /**
     * 新增关注关系
     */
    int insert(UserFollow userFollow);

    /**
     * 更新关注状态
     */
    int updateStatus(@Param("followerId") Long followerId, @Param("followedId") Long followedId, @Param("status") Integer status);

    /**
     * 获取关注关系
     */
    UserFollow getByFollowerAndFollowed(@Param("followerId") Long followerId, @Param("followedId") Long followedId);

    /**
     * 获取用户的关注列表
     */
    List<UserFollow> listByFollowerId(@Param("followerId") Long followerId);

    /**
     * 获取用户的粉丝列表
     */
    List<UserFollow> listByFollowedId(@Param("followedId") Long followedId);

    /**
     * 统计用户关注数
     */
    int countFollowing(@Param("followerId") Long followerId);

    /**
     * 统计用户粉丝数
     */
    int countFollowers(@Param("followedId") Long followedId);
} 