package cn.xu.repository.impl;

import cn.xu.model.entity.Follow;
import cn.xu.repository.FollowRepository;
import cn.xu.repository.mapper.FollowMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 关注关系仓储实现
 * <p>负责关注关系的持久化操作</p>
 */
@Slf4j
@Repository
@RequiredArgsConstructor
public class FollowRepositoryImpl implements FollowRepository {

    private final FollowMapper followMapper;

    /**
     * 根据关注者和被关注者查询关注关系
     */
    public Optional<Follow> findByFollowerIdAndFollowedId(Long followerId, Long followedId) {
        Follow follow = followMapper.getByFollowerAndFollowed(followerId, followedId);
        return Optional.ofNullable(follow);
    }
    
    /**
     * 保存关注关系（新增或更新）
     */
    public void save(Follow follow) {
        if (follow.getId() == null) {
            followMapper.insert(follow);
            log.debug("[关注仓储] 新增关注关系 - {}", follow.getSimpleInfo());
        } else {
            followMapper.update(follow);
            log.debug("[关注仓储] 更新关注关系 - {}", follow.getSimpleInfo());
        }
    }
    
    /**
     * 更新关注状态
     */
    public void updateStatus(Long followerId, Long followedId, Integer status) {
        followMapper.updateStatus(followerId, followedId, status);
        log.debug("[关注仓储] 更新关注状态 - followerId: {}, followedId: {}, status: {}", 
            followerId, followedId, status);
    }
    
    /**
     * 查询关注列表（我关注的人）
     */
    public List<Follow> findFollowingList(Long followerId, int offset, int size) {
        return followMapper.findFollowingList(followerId, offset, size);
    }
    
    /**
     * 查询粉丝列表（关注我的人）
     */
    public List<Follow> findFollowersList(Long followedId, int offset, int size) {
        return followMapper.findFollowersList(followedId, offset, size);
    }
    
    /**
     * 统计关注数
     */
    public Long countFollowing(Long followerId) {
        return (long) followMapper.countFollowing(followerId);
    }
    
    /**
     * 统计粉丝数
     */
    public Long countFollowers(Long followedId) {
        return (long) followMapper.countFollowers(followedId);
    }
    
    /**
     * 查询关注状态
     */
    public Integer findStatus(Long followerId, Long followedId) {
        return followMapper.findStatus(followerId, followedId);
    }
}
