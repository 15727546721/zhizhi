package cn.xu.repository.mapper;

import cn.xu.model.entity.UserBadge;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 用户勋章数据访问接口
 */
@Mapper
public interface UserBadgeMapper {
    
    /**
     * 根据用户ID查询勋章列表
     */
    List<UserBadge> findByUserId(@Param("userId") Long userId);
    
    /**
     * 插入勋章记录
     */
    int insert(UserBadge userBadge);
    
    /**
     * 更新勋章记录
     */
    int update(UserBadge userBadge);
    
    /**
     * 检查用户是否拥有指定勋章
     */
    boolean existsByUserIdAndBadgeId(@Param("userId") Long userId, @Param("badgeId") Long badgeId);
}