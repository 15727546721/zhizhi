package cn.xu.domain.follow.service.impl;

import cn.xu.domain.follow.event.FollowEvent;
import cn.xu.domain.follow.event.FollowEventPublisher;
import cn.xu.domain.follow.model.aggregate.FollowAggregate;
import cn.xu.domain.follow.model.entity.FollowRelationEntity;
import cn.xu.domain.follow.repository.IFollowRepository;
import cn.xu.domain.follow.service.FollowApplicationService;
import cn.xu.domain.follow.service.IFollowService;
import cn.xu.domain.user.repository.IUserAggregateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FollowService implements IFollowService {

    private final IFollowRepository userFollowRepository;
    private final FollowEventPublisher followEventPublisher;
    private final IUserAggregateRepository userAggregateRepository;
    private final FollowApplicationService followApplicationService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void follow(Long followerId, Long followedId) {
        followApplicationService.followUser(followerId, followedId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void unfollow(Long followerId, Long followedId) {
        followApplicationService.unfollowUser(followerId, followedId);
    }

    @Override
    public boolean isFollowing(Long followerId, Long followedId) {
        return followApplicationService.isFollowing(followerId, followedId);
    }

    @Override
    public List<FollowRelationEntity> getFollowingList(Long followerId) {
        // 这个方法在新的设计中将逐步废弃，使用分页版本
        return getFollowingList(followerId, 1, 100);
    }

    @Override
    public List<FollowRelationEntity> getFollowersList(Long followedId) {
        // 这个方法在新的设计中将逐步废弃，使用分页版本
        return getFollowersList(followedId, 1, 100);
    }
    
    /**
     * 获取用户的关注列表（分页）
     * 
     * @param followerId 关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系实体列表
     */
    public List<FollowRelationEntity> getFollowingList(Long followerId, Integer pageNo, Integer pageSize) {
        // 调用应用服务获取关注聚合根列表
        List<FollowAggregate> aggregates = followApplicationService.getFollowingList(followerId, pageNo, pageSize);
        
        // 将聚合根转换为实体列表
        return aggregates.stream()
                .map(FollowAggregate::getFollowRelation)
                .collect(Collectors.toList());
    }
    
    /**
     * 获取用户的粉丝列表（分页）
     * 
     * @param followedId 被关注者ID
     * @param pageNo 页码
     * @param pageSize 页面大小
     * @return 关注关系实体列表
     */
    public List<FollowRelationEntity> getFollowersList(Long followedId, Integer pageNo, Integer pageSize) {
        // 调用应用服务获取粉丝聚合根列表
        List<FollowAggregate> aggregates = followApplicationService.getFollowersList(followedId, pageNo, pageSize);
        
        // 将聚合根转换为实体列表
        return aggregates.stream()
                .map(FollowAggregate::getFollowRelation)
                .collect(Collectors.toList());
    }

    @Override
    public int getFollowingCount(Long followerId) {
        return followApplicationService.getFollowingCount(followerId);
    }

    @Override
    public int getFollowersCount(Long followedId) {
        return followApplicationService.getFollowersCount(followedId);
    }

    @Override
    public boolean checkStatus(long followerId, long followedId) {
        if (followerId == 0 || followedId == 0) {
            // 未登录用户默认未关注
            return false;
        }
        
        // 验证用户ID的有效性
        FollowRelationEntity.validateFollowRelation(followerId, followedId);
         
        return followApplicationService.isFollowing(followerId, followedId);
    }
    
    private void pushFollowEvent(FollowEvent followEvent) {
        followEventPublisher.publish(followEvent);
    }
}