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
    
    /**
     * 获取用户点赞的目标ID列表
     * @param userId 用户ID
     * @param type 点赞类型
     * @return 目标ID列表
     */
    List<Long> selectLikedTargetIdsByUserId(@Param("userId") Long userId, @Param("type") Integer type);
    
    /**
     * 获取点赞某个目标的用户ID列表
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 用户ID列表
     */
    List<Long> selectUserIdsByTargetId(@Param("targetId") Long targetId, @Param("type") Integer type);
    
    /**
     * 分页查询用户的点赞列表
     * @param userId 用户ID
     * @param offset 偏移量
     * @param limit 限制数量
     * @return 点赞列表
     */
    List<Like> findByUserId(@Param("userId") Long userId, @Param("offset") int offset, @Param("limit") int limit);
    
    /**
     * 统计用户的点赞总数
     * @param userId 用户ID
     * @return 点赞总数
     */
    long countByUserId(@Param("userId") Long userId);
    
    /**
     * 批量查询用户对多个目标的点赞记录
     * @param userId 用户ID
     * @param type 点赞类型
     * @param targetIds 目标ID列表
     * @return 点赞记录列表
     */
    List<Like> findByUserIdAndTargetIds(@Param("userId") Long userId, 
                                        @Param("type") Integer type, 
                                        @Param("targetIds") List<Long> targetIds);
}