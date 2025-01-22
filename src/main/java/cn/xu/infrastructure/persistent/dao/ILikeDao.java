package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Set;

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
                                        @Param("targetId") Long targetId,
                                        @Param("type") Integer type);

    /**
     * 统计目标的点赞数量
     */
    Long countByTargetIdAndType(@Param("targetId") Long targetId,
                                @Param("type") Integer type);

    /**
     * 获取点赞用户ID列表
     */
    Set<Long> getLikedUserIds(@Param("targetId") Long targetId,
                              @Param("type") Integer type);

    /**
     * 分页获取指定类型的点赞记录
     */
    Set<Like> getPageByType(@Param("type") Integer type,
                            @Param("offset") Integer offset,
                            @Param("limit") Integer limit);

    /**
     * 获取指定类型的点赞记录总数
     */
    Long countByType(@Param("type") Integer type);
}
