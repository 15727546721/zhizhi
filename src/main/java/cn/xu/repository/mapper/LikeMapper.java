package cn.xu.repository.mapper;

import cn.xu.model.entity.Like;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 点赞Mapper接口
 * <p>处理点赞相关的数据库操作</p>

 */
@Mapper
public interface LikeMapper {
    /**
     * 插入点赞记录
     */
    void save(Like po);
    
    /**
     * 原子性保存或更新点赞记录（解决并发竞态条件）
     * <p>使用 INSERT ON DUPLICATE KEY UPDATE 确保高并发下不会创建重复记录</p>
     * 
     * @param po 点赞记录
     */
    void saveOrUpdate(Like po);
    
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
    
    /**
     * 统计用户发布的帖子收到的点赞数
     * @param userId 用户ID
     * @return 帖子收到的点赞总数
     */
    Long countReceivedLikesByUserPosts(@Param("userId") Long userId);
    
    /**
     * 统计用户发布的评论收到的点赞数
     * @param userId 用户ID
     * @return 评论收到的点赞总数
     */
    Long countReceivedLikesByUserComments(@Param("userId") Long userId);
    
    /**
     * 统计所有点赞数
     */
    Long countAll();
    
    /**
     * 按类型统计点赞数
     */
    Long countByType(@Param("type") Integer type);
    
    /**
     * 批量检查点赞状态
     * @param userId 用户ID
     * @param type 点赞类型
     * @param targetIds 目标ID列表
     * @return 已点赞的目标ID集合
     */
    java.util.Set<Long> batchCheckStatus(@Param("userId") Long userId, 
                                          @Param("type") Integer type, 
                                          @Param("targetIds") java.util.List<Long> targetIds);
    
    /**
     * 获取目标的点赞数
     * @param targetId 目标ID
     * @param type 点赞类型
     * @return 点赞数
     */
    Long getLikeCount(@Param("targetId") Long targetId, @Param("type") Integer type);
}
