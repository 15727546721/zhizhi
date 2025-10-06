package cn.xu.infrastructure.persistent.dao;

import cn.xu.infrastructure.persistent.po.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 点赞数据访问接口
 */
@Mapper
public interface LikeMapper {
    /**
     * 插入点赞记录
     */
    void save(Like po);
    
    /**
     * 更新点赞记录
     */
    void update(Like po);
    
    /**
     * 根据ID删除点赞记录
     */
    void deleteById(Long id);

    /**
     * 批量删除点赞记录
     */
    void deleteByIds(List<Long> ids);

    /**
     * 根据ID查询点赞记录
     */
    Like findById(Long id);

    void delete(@Param("targetId") Long targetId, @Param("type") Integer type);

    /**
     * 更新点赞状态
     * @param userId
     * @param type
     * @param targetId
     * @param status
     */
    void updateStatus(@Param("userId") Long userId,
                      @Param("type") Integer type,
                      @Param("targetId") Long targetId,
                      @Param("status") Integer status);

    /**
     * 根据用户ID、目标ID和类型查询点赞记录
     */
    Like findByUserIdAndTypeAndTargetId(@Param("userId") Long userId,
                                        @Param("type") Integer type,
                                        @Param("targetId") Long targetId);

    /**
     * 根据用户ID、目标ID和类型查询点赞状态
     * @param userId
     * @param type
     * @param targetId
     * @return
     */
    Integer checkStatus(@Param("userId") Long userId,
                       @Param("type") Integer type,
                       @Param("targetId") Long targetId);
                       
    /**
     * 统计目标的点赞数
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    long countByTargetIdAndType(@Param("targetId") Long targetId, @Param("type") Integer type);
}