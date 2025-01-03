package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.LikePO;
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
    void insert(LikePO likePO);

    /**
     * 更新点赞记录
     */
    void update(LikePO likePO);

    /**
     * 根据用户ID、目标ID和类型查询点赞记录
     */
    LikePO findByUserIdAndTargetIdAndType(@Param("userId") Long userId,
                                         @Param("targetId") Long targetId,
                                         @Param("type") Integer type);

    /**
     * 统计目标的点赞数
     */
    Long countByTargetIdAndType(@Param("targetId") Long targetId,
                               @Param("type") Integer type);

    /**
     * 更新点赞数
     */
    void updateLikeCount(@Param("targetId") Long targetId,
                        @Param("type") Integer type,
                        @Param("count") Long count);
}
