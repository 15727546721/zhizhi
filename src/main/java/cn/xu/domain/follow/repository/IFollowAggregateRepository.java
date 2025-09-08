package cn.xu.domain.follow.repository;

import cn.xu.domain.follow.model.aggregate.FollowAggregate;

import java.util.List;
import java.util.Optional;

/**
 * 关注关系聚合根仓储接口
 * 遵循DDD原则，只处理聚合根的操作
 */
public interface IFollowAggregateRepository {
    
    /**
     * 保存关注关系聚合根
     * @param aggregate 关注关系聚合根
     * @return 聚合根ID
     */
    Long save(FollowAggregate aggregate);
    
    /**
     * 更新关注关系聚合根
     * @param aggregate 关注关系聚合根
     */
    void update(FollowAggregate aggregate);
    
    /**
     * 根据ID查找关注关系聚合根
     * @param id 关注关系ID
     * @return 关注关系聚合根
     */
    Optional<FollowAggregate> findById(Long id);
    
    /**
     * 根据关注者和被关注者查找关注关系聚合根
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 关注关系聚合根
     */
    Optional<FollowAggregate> findByFollowerAndFollowed(Long followerId, Long followedId);
    
    /**
     * 获取用户的关注列表
     * @param followerId 关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系聚合根列表
     */
    List<FollowAggregate> findFollowingList(Long followerId, Integer pageNo, Integer pageSize);
    
    /**
     * 获取用户的粉丝列表
     * @param followedId 被关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系聚合根列表
     */
    List<FollowAggregate> findFollowersList(Long followedId, Integer pageNo, Integer pageSize);
    
    /**
     * 统计用户关注数
     * @param followerId 关注者ID
     * @return 关注数
     */
    int countFollowing(Long followerId);
    
    /**
     * 统计用户粉丝数
     * @param followedId 被关注者ID
     * @return 粉丝数
     */
    int countFollowers(Long followedId);
    
    /**
     * 删除关注关系聚合根
     * @param id 关注关系ID
     */
    void deleteById(Long id);
    
    /**
     * 批量删除关注关系聚合根
     * @param ids 关注关系ID列表
     */
    void deleteByIds(List<Long> ids);
    
    /**
     * 检查关注关系是否存在
     * @param followerId 关注者ID
     * @param followedId 被关注者ID
     * @return 是否存在
     */
    boolean existsByFollowerAndFollowed(Long followerId, Long followedId);
    
    /**
     * 获取互相关注列表
     * @param userId 用户ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 互相关注的用户ID列表
     */
    List<Long> findMutualFollows(Long userId, Integer pageNo, Integer pageSize);
}