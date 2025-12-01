package cn.xu.repository.mapper;

import cn.xu.model.entity.UserLevel;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户等级数据访问接口
 */
@Mapper
public interface UserLevelMapper {
    
    /**
     * 根据用户ID查询等级记录
     */
    UserLevel findByUserId(@Param("userId") Long userId);
    
    /**
     * 插入等级记录
     */
    int insert(UserLevel userLevel);
    
    /**
     * 更新等级记录
     */
    int update(UserLevel userLevel);
    
    /**
     * 查询等级排行榜
     */
    List<UserLevel> findRanking(@Param("limit") int limit);
}