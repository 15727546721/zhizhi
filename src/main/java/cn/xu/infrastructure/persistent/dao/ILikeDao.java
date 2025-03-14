package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 点赞数据访问接口
 */
@Mapper
public interface ILikeDao {
    /**
     * 插入点赞记录
     */
    void insert(Like po);

    /**
     * 更新点赞记录
     */
    void update(Like po);

    /**
     * 根据用户ID、目标ID和类型查询点赞记录
     */
    Like findByUserIdAndTargetIdAndType(@Param("userId") Long userId,
                                        @Param("type") Integer type,
                                        @Param("targetId") Long targetId);

    Integer findStatus(@Param("userId") Long userId,
                       @Param("type") Integer type,
                       @Param("targetId") Long targetId);
}
