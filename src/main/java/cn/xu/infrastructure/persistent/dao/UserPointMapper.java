package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.UserPoint;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户积分数据访问接口
 */
@Mapper
public interface UserPointMapper {
    
    /**
     * 根据用户ID查询积分记录
     */
    UserPoint findByUserId(@Param("userId") Long userId);
    
    /**
     * 插入积分记录
     */
    int insert(UserPoint userPoint);
    
    /**
     * 更新积分记录
     */
    int update(UserPoint userPoint);
    
    /**
     * 查询积分排行榜
     */
    List<UserPoint> findRanking(@Param("limit") int limit);
}